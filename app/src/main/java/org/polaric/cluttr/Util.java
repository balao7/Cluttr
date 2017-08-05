package org.polaric.cluttr;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;

import org.polaric.cluttr.dialogs.ActionMessageDialog;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Util {

    public static final int PERMISSION_REQUEST_STORAGE = 43;
    public static final int PERMISSION_REQUEST_FINGERPRINT=35;
    public static final String FINGERPRINT_KEY="cluttr_fingerprint";
    public static final String PASSWORD_KEY="cluttr_password";
    public static final String LOG_TAG = "Cluttr";
    public static final String ALBUM_PASS_KEY="album_integer";
    public static final String MEDIA_PASS_KEY="media_integer";
    public static final String URI_PASS_KEY="uri_key";
    public static final String CACHE_NAME="/cluttr.cache";
    public static final int VIDEO_ACTIVITY_VIEW=5984;
    public static final String BILLING_KEY = "";
    public static final List<String> DEFAULT_EXCLUDED = Collections.singletonList("/storage/emulated/0/Android");

    public static class Fragment {
        public static final String FRAGMENT_ALBUM_TAG="fragment_album";
        public static final String FRAGMENT_ALBUM_DETAIL_TAG="fragment_album_detail";
        public static final String FRAGMENT_MEDIA_DETAIL_TAG="fragment_media_detail";
        public static final String FRAGMENT_PERM_REQUEST_TAG="fragment_grant";
    }

    public static class IO {
        public static String humanReadableByteCount(long bytes, boolean si) {
            int unit = si ? 1000 : 1024;
            if (bytes < unit) return bytes + " B";
            int exp = (int) (Math.log(bytes) / Math.log(unit));
            String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
            return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
        }
    }

    public static class ViewUtils{
        public static void colorizeToolbar(Toolbar toolbarView, int toolbarIconsColor) {
            final PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(toolbarIconsColor, PorterDuff.Mode.MULTIPLY);
            for(int i = 0; i < toolbarView.getChildCount(); i++) {
                final View v = toolbarView.getChildAt(i);
                if(v instanceof ImageButton) {
                    ((ImageButton)v).getDrawable().setColorFilter(colorFilter);
                }
                if(v instanceof ActionMenuView) {
                    for(int j = 0; j < ((ActionMenuView)v).getChildCount(); j++) {
                        final View innerView = ((ActionMenuView)v).getChildAt(j);
                        if(innerView instanceof ActionMenuItemView) {
                            int drawablesCount = ((ActionMenuItemView)innerView).getCompoundDrawables().length;
                            for(int k = 0; k < drawablesCount; k++) {
                                if(((ActionMenuItemView)innerView).getCompoundDrawables()[k] != null) {
                                    final int finalK = k;
                                    innerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ((ActionMenuItemView) innerView).getCompoundDrawables()[finalK].setColorFilter(colorFilter);
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            }
        }

        public static void showChangelogDialog(Context context) {
            ActionMessageDialog d = new ActionMessageDialog(context);
            d.setTitleText(context.getResources().getString(R.string.changelog));
            d.setMsgText(context.getResources().getString(R.string.changelog_text));
            d.setNegativeButtonMsg(context.getResources().getString(android.R.string.ok));
            d.show();
        }
    }

    public static class Animations {
        public static void createReveal(View view) {
            view.setVisibility(View.VISIBLE);
            if (Build.VERSION.SDK_INT>20) {
                int cx = view.getMeasuredWidth() / 2;
                int cy = view.getMeasuredHeight() / 2;
                int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;
                Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
                anim.start();
            } else {
                Animation anim = new AlphaAnimation(0,1);
                anim.setDuration(300);
                view.startAnimation(anim);
            }

        }

        public static void createHide(final View view) {
            if (Build.VERSION.SDK_INT>20) {
                int cx = view.getMeasuredWidth() / 2;
                int cy = view.getMeasuredHeight() / 2;
                int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;
                Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, finalRadius, 0);
                anim.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
                anim.start();
            } else {
                Animation anim = new AlphaAnimation(1,0);
                anim.setDuration(300);
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(anim);
            }

        }
    }

    public static class ContentResolverHelper {

        public static String getPath(final Context context, final Uri uri) {

            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

            // DocumentProvider
            if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + split[1];
                    }

                    // TODO handle non-primary volumes
                }
                // DownloadsProvider
                else if (isDownloadsDocument(uri)) {

                    final String id = DocumentsContract.getDocumentId(uri);
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                    return getDataColumn(context, contentUri, null, null);
                }
                // MediaProvider
                else if (isMediaDocument(uri)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];

                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }

                    final String selection = "_id=?";
                    final String[] selectionArgs = new String[]{
                            split[1]
                    };

                    return getDataColumn(context, contentUri, selection, selectionArgs);
                }
            }
            // MediaStore (and general)
            else if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumn(context, uri, null, null);
            }
            // File
            else if ("file".equalsIgnoreCase(uri.getScheme())) {
                return uri.getPath();
            }

            return null;
        }

        /**
         * Get the value of the data column for this Uri. This is useful for
         * MediaStore Uris, and other file-based ContentProviders.
         *
         * @param context       The context.
         * @param uri           The Uri to query.
         * @param selection     (Optional) Filter used in the query.
         * @param selectionArgs (Optional) Selection arguments used in the query.
         * @return The value of the _data column, which is typically a file path.
         */
        public static String getDataColumn(Context context, Uri uri, String selection,
                                           String[] selectionArgs) {

            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {
                    column
            };

            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                        null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int column_index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(column_index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return null;
        }


        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is ExternalStorageProvider.
         */
        public static boolean isExternalStorageDocument(Uri uri) {
            return "com.android.externalstorage.documents".equals(uri.getAuthority());
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is DownloadsProvider.
         */
        public static boolean isDownloadsDocument(Uri uri) {
            return "com.android.providers.downloads.documents".equals(uri.getAuthority());
        }

        /**
         * @param uri The Uri to check.
         * @return Whether the Uri authority is MediaProvider.
         */
        public static boolean isMediaDocument(Uri uri) {
            return "com.android.providers.media.documents".equals(uri.getAuthority());
        }
    }
}
