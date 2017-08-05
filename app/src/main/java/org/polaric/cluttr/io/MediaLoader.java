package org.polaric.cluttr.io;

import android.content.Intent;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.polaric.cluttr.Cluttr;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;
import org.polaric.cluttr.data.MediaItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.Sort;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class MediaLoader {
    private static List<String> excluded;

    public static Observable<Boolean> init() {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        long time = System.currentTimeMillis();

                        String prefString = PreferenceManager.getDefaultSharedPreferences(Cluttr.getApplication()).getString("excluded_folders","");
                        if (prefString.isEmpty()) {
                            excluded = new ArrayList<>();
                        } else {
                            excluded = new ArrayList<>(Arrays.asList(prefString.split(":")));
                        }


                        boolean result = true;
                        try {
                            for (File root : listStorages()) {
                                recursiveLoadAlbums(root);
                            }
                        } catch (Exception ex) {
                            Log.e(Util.LOG_TAG, "A fatal error has occured during media loading, cache will be wiped", ex);
                            //TODO: WIPE CACHE
                            result = false;
                        }
                        Log.d(Util.LOG_TAG, "Indexing all media took " + (System.currentTimeMillis() - time) + "ms");
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    private static List<File> listStorages() {
        List<File> roots = new ArrayList<>();
        roots.add(new File(FileUtil.getSdCardPath()));

        File sd = ExtSdHelper.getRootFile();
        if (sd==null) return roots;
        if (sd.exists()) {
            Cluttr.getApplication().getContentResolver().takePersistableUriPermission(ExtSdHelper.getSdUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            roots.add(sd);
        } else {
            Log.e(Util.LOG_TAG, "External SDCard nolonger found, clearing cache");
            //TODO: CLEAR CACHE
        }

        return roots;
    }

    private static void recursiveLoadAlbums(File dir) {
        final File[] contents = dir.listFiles(new MediaFilter());
        Realm realm = Realm.getDefaultInstance();
        final Album album = realm.where(Album.class).equalTo("path", dir.getPath()).findFirst();
        if (album!=null) {
            Log.d(Util.LOG_TAG, "Album " + album.getName() + " is already in cache");
            if (album.needsReload()) {
                final Album newAlbum = new Album(dir.getPath(), contents.length, dir.getName(), contents[0].getPath(), dir.lastModified());
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (File item : contents) {
                            newAlbum.addMediaItem(new MediaItem(item));
                        }
                        realm.copyToRealmOrUpdate(newAlbum);
                    }
                });
                updateSingleCover(newAlbum.getPath());
            } else {
                Log.d(Util.LOG_TAG, "Album is up to date, skipping index");
            }
        } else {
            if (contents.length>0) {
                Log.d(Util.LOG_TAG, "New album found: " + dir.getName() + ", adding to cache");
                final Album newAlbum = new Album(dir.getPath(), contents.length, dir.getName(), contents[0].getPath(), dir.lastModified());
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        for (File item : contents) {
                            newAlbum.addMediaItem(new MediaItem(item));
                        }
                        realm.copyToRealmOrUpdate(newAlbum);
                    }
                });
                updateSingleCover(newAlbum.getPath());
            }
        }
        realm.close();
        for (File child : dir.listFiles(new FolderFilter())) {
            if (Util.DEFAULT_EXCLUDED.contains(child.getPath()) || excluded.contains(child.getPath()) || child.isHidden()) {
                Log.d(Util.LOG_TAG, "Folder " + child.getPath() + " is excluded, skipping");
            } else {
                recursiveLoadAlbums(child);
            }
        }
    }

    public static Observable<Boolean> refreshAlbum(final String key) {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        refreshAlbumImp(key);
                        subscriber.onNext(true);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    private static void refreshAlbumImp(String key) {
        long time = System.currentTimeMillis();
        Realm realm = Realm.getDefaultInstance();
        Album album = realm.where(Album.class).equalTo("path", key).findFirst();
        File dir = new File(album.getPath());
        final File[] contents = dir.listFiles(new MediaFilter());
        final Album newAlbum = new Album(dir.getPath(), contents.length, dir.getName(), contents[0].getPath(), dir.lastModified());
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                for (File item : contents) {
                    newAlbum.addMediaItem(new MediaItem(item));
                }
                realm.copyToRealmOrUpdate(newAlbum);
            }
        });
        updateSingleCover(newAlbum.getPath());
        realm.close();
        Log.d(Util.LOG_TAG, "Album " + newAlbum.getName() + " refreshed in " + (System.currentTimeMillis()-time) + "ms");
    }

    private static boolean deleteFileImpl(final Album album, final MediaItem item) {
        File media = new File(item.getPath());
        Log.d(Util.LOG_TAG,"Deleting " + media.getAbsolutePath());
        if (FileUtil.deleteFile(media)) {
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    album.getMedia().where().equalTo("path", item.getPath()).findFirst().deleteFromRealm();
                }
            });
            realm.close();
            return true;
        }
        return false;
    }

    public static void updateCoverPhotos() {
        long time = System.currentTimeMillis();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        for (Album a : realm.where(Album.class).findAll()) {
            switch (Media.getMediaSortType()) {
                case ALPHABETICAL: {
                    a.setCoverPath(a.getMedia().sort("name", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING).first().getPath());
                    break;
                }
                case DATE: {
                    a.setCoverPath(a.getMedia().sort("date", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING).first().getPath());
                    break;
                }
            }
        }
        realm.commitTransaction();
        realm.close();
        Log.d(Util.LOG_TAG, "Reloading all cover photos took " + (System.currentTimeMillis() - time));
    }

    public static void updateSingleCover(String key) {
        long time = System.currentTimeMillis();
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Album a = realm.where(Album.class).equalTo("path",key).findFirst();
        switch (Media.getMediaSortType()) {
            case ALPHABETICAL: {
                a.setCoverPath(a.getMedia().sort("name", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING).first().getPath());
                break;
            }
            case DATE: {
                a.setCoverPath(a.getMedia().sort("date", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING).first().getPath());
                break;
            }
        }
        realm.commitTransaction();
        realm.close();
        Log.d(Util.LOG_TAG, "Reloading all cover photos took " + (System.currentTimeMillis() - time));
    }

    private static boolean moveFileImpl(Album sourceAlbum, MediaItem item, Album targetAlbum) {
        if (sourceAlbum.getPath().equals(targetAlbum.getPath())) return true;
        return copyFileImpl(sourceAlbum, item, targetAlbum) && deleteFileImpl(sourceAlbum, item);
    }

    private static boolean copyFileImpl(Album sourceAlbum, MediaItem item, final Album targetAlbum) {
        File dst;
        try {
            File src = new File(item.getPath());
            dst = new File(targetAlbum.getPath() + "/" + src.getName());
            if (dst.exists()) {
                Log.d(Util.LOG_TAG,dst.getName() + " already exists, finding a new name");
                String name = dst.getName().substring(0,dst.getName().indexOf("."));
                if (name.trim().endsWith(")")) {
                    name = name.substring(0,name.lastIndexOf("("));
                    name = name.trim();
                }
                String extension = dst.getName().substring(dst.getName().lastIndexOf("."));
                Log.d(Util.LOG_TAG, "Split name into " + name + " and " + extension);
                int nameInt=0;
                dst = new File(targetAlbum.getPath() + "/" + name + "(" + nameInt + ")" + extension);
                while (dst.exists()) {
                    nameInt++;
                    dst = new File(targetAlbum.getPath() + "/" + name + "(" + nameInt + ")" + extension);
                }
                Log.d(Util.LOG_TAG, "New file name is " + dst.getName());
            }
            Log.d(Util.LOG_TAG, "Copying " + src.getAbsolutePath() + " to " + dst.getAbsolutePath());

            boolean result = FileUtil.copyFile(src, dst);
            if (!result) {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        final File finalDst = dst;
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                targetAlbum.addMediaItem(new MediaItem(finalDst));
            }
        });
        realm.close();
        return true;
    }

    public static Observable<Boolean> deleteFiles(final String album) {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        boolean result = true;
                        Realm realm = Realm.getDefaultInstance();
                        final Album target = realm.where(Album.class).equalTo("path", album).findFirst();
                        for (MediaItem item : target.getSelected()) {
                            if (!deleteFileImpl(target, item)) {
                                result=false;
                            }
                        }
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                target.clearMediaSelection();
                                target.setLastModified(new File(target.getPath()).lastModified());
                                if (target.getMedia().size()<1) {
                                    realm.where(Album.class).equalTo("path", target.getPath()).findFirst().deleteFromRealm();
                                }
                            }
                        });
                        realm.close();
                        updateCoverPhotos();
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public static Observable<Boolean> moveFiles(final String sourceAlbum, final String targetAlbum) {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        boolean result = true;
                        Realm realm = Realm.getDefaultInstance();
                        final Album source = realm.where(Album.class).equalTo("path", sourceAlbum).findFirst();
                        final Album target = realm.where(Album.class).equalTo("path", targetAlbum).findFirst();
                        for (MediaItem item : source.getMedia().where().equalTo("isSelected",true).findAll()) {
                            if (!moveFileImpl(source,item,target)) {
                                result=false;
                            }
                        }
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                source.clearMediaSelection();
                                target.clearMediaSelection();
                                source.setLastModified(new File(source.getPath()).lastModified());
                                target.setLastModified(new File(target.getPath()).lastModified());
                                if (source.getSize()<1) {
                                    realm.where(Album.class).equalTo("path", source.getPath()).findFirst().deleteFromRealm();
                                }
                            }
                        });
                        realm.close();
                        updateCoverPhotos();
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public static Observable<Boolean> copyFiles(final String sourceAlbum, final String targetAlbum) {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        boolean result = true;
                        Realm realm = Realm.getDefaultInstance();
                        final Album source = realm.where(Album.class).equalTo("path", sourceAlbum).findFirst();
                        final Album target = realm.where(Album.class).equalTo("path", targetAlbum).findFirst();
                        for (MediaItem item : source.getMedia().where().equalTo("isSelected",true).findAll()) {
                            if (!copyFileImpl(source,item,target)) {
                                result=false;
                            }
                        }
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                source.clearMediaSelection();
                                target.clearMediaSelection();
                                source.setLastModified(new File(source.getPath()).lastModified());
                                target.setLastModified(new File(target.getPath()).lastModified());
                                if (source.getSize()<1) {
                                    realm.where(Album.class).equalTo("path", source.getPath()).findFirst().deleteFromRealm();
                                }
                                if (target.getSize()<1) {
                                    realm.where(Album.class).equalTo("path", target.getPath()).findFirst().deleteFromRealm();
                                }
                            }
                        });
                        realm.close();
                        updateCoverPhotos();
                        subscriber.onNext(result);
                        subscriber.onCompleted();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }
}
