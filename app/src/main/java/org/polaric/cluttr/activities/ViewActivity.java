package org.polaric.cluttr.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.BottomSheetBehavior;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

//import com.drew.imaging.ImageMetadataReader;
//import com.drew.metadata.Metadata;
//import com.drew.metadata.exif.ExifSubIFDDirectory;

import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.adapters.MediaViewAdapter;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;
import org.polaric.cluttr.data.MediaItem;
import org.polaric.cluttr.dialogs.AlbumPickerDialog;
import org.polaric.cluttr.fragments.AlbumPickerFragment;
import org.polaric.cluttr.io.MediaLoader;
import org.polaric.colorful.Colorful;
import org.polaric.colorful.ColorfulActivity;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.Sort;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class ViewActivity extends ColorfulActivity implements View.OnClickListener, Action1<Void>, Toolbar.OnMenuItemClickListener, View.OnSystemUiVisibilityChangeListener, ViewPager.OnPageChangeListener, MediaViewAdapter.SizeCallback {
    private MediaViewAdapter adapter;
    private String media;
    private Album album;
    private boolean showToolbar=true;
    private String path=null;
    private Subscription timeoutSubscription=null;
    private BottomSheetBehavior bottomSheetBehavior;
    private boolean operationInProgress=false;
    private List<MediaItem> results;

    @BindView(R.id.view_toolbar) Toolbar viewToolbar;
    @BindView(R.id.view_content_frame) View contentFrame;
    @BindView(R.id.view_pager) ViewPager pager;
    @BindView(R.id.media_item_toolbar) Toolbar toolbar;
    @BindView(R.id.media_item_toolbar_group) View toolbarGroup;
    @BindView(R.id.media_bottom_sheet) View bottomSheet;
    @BindView(R.id.media_tags_filename) TextView filename;
    @BindView(R.id.media_tags_location) TextView location;
    @BindView(R.id.media_tags_type) TextView type;
    @BindView(R.id.media_tags_exposure) TextView exposure;
    @BindView(R.id.media_tags_filesize) TextView filesize;
    @BindView(R.id.media_tags_date) TextView date;
    @BindView(R.id.media_tags_focal) TextView focal;
    @BindView(R.id.media_tags_resolution) TextView resolution;
    @BindView(R.id.media_tags_aperture) TextView aperture;
    @BindView(R.id.media_tags_iso) TextView iso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (getIntent().getAction()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            String p = getIntent().getDataString();
            path = Util.ContentResolverHelper.getPath(this, Uri.parse(p));
            Log.d(Util.LOG_TAG, "Starting on VIEW intent for " + getIntent().getDataString());

            adapter = new MediaViewAdapter(this, path, this);
            pager.setAdapter(adapter);

            setupMetadata();
        } else {
            Realm realm = Realm.getDefaultInstance();
            album = realm.where(Album.class).equalTo("path",getIntent().getExtras().getString(Util.ALBUM_PASS_KEY)).findFirst();
            media = getIntent().getExtras().getString(Util.MEDIA_PASS_KEY);
            results = getResults();
            int i = results.indexOf(album.getMedia().where().equalTo("path",media).findFirst());
            adapter = new MediaViewAdapter(this, results, this);
            pager.setAdapter(adapter);
            pager.setCurrentItem(i);

            viewToolbar.inflateMenu(R.menu.media_edit_toolbar);
            if (!Colorful.getThemeDelegate().isDark()) {
                Util.ViewUtils.colorizeToolbar(viewToolbar, Color.BLACK);
            }

            pager.addOnPageChangeListener(this);
            setupMetadata();
        }

        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);

        toolbar.setNavigationIcon(R.drawable.md_nav_back);
        toolbar.inflateMenu(R.menu.media_item_toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return false;
            }
        });
        toolbar.setNavigationOnClickListener(this);

        viewToolbar.setTitle(R.string.info);
        if (Colorful.getThemeDelegate().isDark()) {
            viewToolbar.setNavigationIcon(R.drawable.ic_close_white_48px);
        } else {
            viewToolbar.setNavigationIcon(R.drawable.ic_close_black_48px);
        }
        viewToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close();
            }
        });
        viewToolbar.setOnMenuItemClickListener(this);

        startTimeOut();

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        toolbar.setTitleTextColor(Color.WHITE);

        toolbar.setTitle(adapter.getPageTitle(pager.getCurrentItem()));

    }



    private List<MediaItem> getResults() {
        switch (Media.getMediaSortType()) {
            case ALPHABETICAL: {
                return album.getMedia().where().findAll().sort("name", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
            }
            case DATE: {
                return album.getMedia().where().findAll().sort("date", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
            }
        }
        return null;
    }

    @Override
    public void sizeChanged(int w, int h) {
        resolution.setText(getResources().getString(R.string.resolution) + ": " + w + "x" + h);
    }

    private void setupMetadata() {
        File media;

        if (path==null) {
            media = new File(results.get(pager.getCurrentItem()).getPath());
        } else {
            media = new File(path);
        }

        filename.setText(media.getName());
        String loc = media.getAbsolutePath();
        loc = loc.substring(0,loc.lastIndexOf("/"));
        location.setText(getResources().getString(R.string.location) + ": " + loc);
        type.setText(getResources().getString(R.string.type) + ": " + media.getName().substring(media.getName().lastIndexOf(".")+1));
        filesize.setText(getResources().getString(R.string.filesize) + ": " + Util.IO.humanReadableByteCount(media.length(),false));

        focal.setText(getResources().getString(R.string.focal) + ": " + getResources().getString(R.string.na));
        date.setText(getResources().getString(R.string.date) + ": " + getResources().getString(R.string.na));
        aperture.setText(getResources().getString(R.string.aperture) + ": " + getResources().getString(R.string.na));
        exposure.setText(getResources().getString(R.string.exposure) + ": " + getResources().getString(R.string.na));
        iso.setText(getResources().getString(R.string.iso) + ": " + getResources().getString(R.string.na));

        //Metadata loading
        try {
            ExifInterface metadata = new ExifInterface(media.getAbsolutePath());

            String tmp = metadata.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
            if (tmp != null) {
                focal.setText(getResources().getString(R.string.focal) + ": " + tmp);
            }

            tmp = metadata.getAttribute(ExifInterface.TAG_APERTURE_VALUE);
            if (tmp != null) {
                aperture.setText(getResources().getString(R.string.aperture) + ": " + tmp);
            }

            tmp = metadata.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            if (tmp != null) {
                date.setText(getString(R.string.date_taken) + ": " + tmp);
            }

            tmp = metadata.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            if (tmp != null) {
                exposure.setText(getResources().getString(R.string.exposure) + ": " + tmp);
            }

            tmp = metadata.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
            if (tmp != null) {
                iso.setText(getResources().getString(R.string.iso) + ": " + tmp);
            }
        }
        catch (Exception e){
            Log.e(Util.LOG_TAG, e.getMessage());
        }
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!operationInProgress) {

            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            album.getMedia().where().equalTo("path", results.get(pager.getCurrentItem()).getPath()).findFirst().setSelected(true);
            realm.commitTransaction();
            realm.close();

            switch (item.getItemId()) {
                case R.id.delete: {
                    operationInProgress = true;
                    final ProgressDialog pDialog = new ProgressDialog(this);
                    pDialog.show();
                    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    MediaLoader.deleteFiles(album.getPath())
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    operationInProgress = false;
                                    pDialog.dismiss();
                                    if (aBoolean) {
                                        Toast.makeText(ViewActivity.this, R.string.action_complete, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(ViewActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                    }
                                    finish();
                                }
                            });
                    break;
                }
                case R.id.move: {
                    operationInProgress=true;
                    AlbumPickerDialog dialog = new AlbumPickerDialog(this);
                    dialog.setOnItemSelectedListener(new AlbumPickerDialog.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(final Album target) {
                            if (target == null) {
                                operationInProgress=false;
                            } else {
                                final ProgressDialog pDialog = new ProgressDialog(ViewActivity.this);
                                pDialog.show();
                                MediaLoader.moveFiles(album.getPath(), target.getPath())
                                        .subscribe(new Action1<Boolean>() {
                                            @Override
                                            public void call(Boolean aBoolean) {
                                                operationInProgress = false;
                                                pDialog.dismiss();
                                                if (aBoolean) {
                                                    Toast.makeText(ViewActivity.this, R.string.action_complete, Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(ViewActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                                }
                                                finish();
                                            }
                                        });
                            }
                        }
                    });
                    dialog.show();
                    break;
                }
                case R.id.copy: {
                    operationInProgress=true;
                    AlbumPickerDialog dialog = new AlbumPickerDialog(this);
                    dialog.setOnItemSelectedListener(new AlbumPickerDialog.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(final Album target) {
                            if (target == null) {
                                operationInProgress=false;
                            } else {
                                final ProgressDialog pDialog = new ProgressDialog(ViewActivity.this);
                                pDialog.show();
                                MediaLoader.copyFiles(album.getPath(), target.getPath())
                                        .subscribe(new Action1<Boolean>() {
                                            @Override
                                            public void call(Boolean aBoolean) {
                                                operationInProgress = false;
                                                pDialog.dismiss();
                                                if (aBoolean) {
                                                    Toast.makeText(ViewActivity.this, R.string.action_complete, Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(ViewActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                                }
                                                finish();
                                            }
                                        });
                            }
                        }
                    });
                    dialog.show();
                    break;
                }
                case R.id.share: {
                    Media.share(this, album.getPath());
                    break;
                }case R.id.edit: {
                    startActivity(new Intent(this, EditActivity.class).putExtra(EditActivity.IMAGE_URI_KEY,results.get(pager.getCurrentItem()).getPath()));
                    break;
                }
            }
        }
        return false;
    }

    public void close() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onSystemUiVisibilityChange(int i) {
        if ((i & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
            toggleToolbar(true);
        } else {
            // The system bars are NOT visible.
        }
    }

    @Override
    public void onClick(View view) {
        finish();
        if (Media.useAnimations()) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            overridePendingTransition(0,0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timeoutSubscription!=null) {
            timeoutSubscription.unsubscribe();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Media.useAnimations()) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } else {
            overridePendingTransition(0,0);
        }
    }

    @Override
    public void call(Void aVoid) {
        Log.d(Util.LOG_TAG,"Auto hiding toolbar");
        toggleToolbar(false);
    }

    public void toggleToolbar(boolean show) {
        if (show==showToolbar || toolbarGroup==null) {
            return;
        }

        showToolbar=show;
        if (showToolbar) {
            startTimeOut();
            showSystemUI();
            toolbarGroup.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        } else {
            if (timeoutSubscription!=null) {
                timeoutSubscription.unsubscribe();
            }
            toolbarGroup.animate().translationY(-toolbarGroup.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
            hideSystemUI();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }
    @Override
    public void onPageScrollStateChanged(int state) {
    }
    @Override
    public void onPageSelected(int position) {
        toolbar.setTitle(adapter.getPageTitle(position));
        setupMetadata();
    }

    private Observable<Void> timeOut = Observable
            .create(new Observable.OnSubscribe<Void>() {
                @Override
                public void call(Subscriber<? super Void> subscriber) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            })
            .delay(4, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation());

    private void startTimeOut() {
        if (timeoutSubscription!=null) {
            timeoutSubscription.unsubscribe();
        }
        timeoutSubscription = timeOut.subscribe(this);
    }

    public void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    public void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
