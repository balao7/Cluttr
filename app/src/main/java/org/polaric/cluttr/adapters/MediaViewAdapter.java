package org.polaric.cluttr.adapters;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.activities.VideoActivity;
import org.polaric.cluttr.activities.ViewActivity;
import org.polaric.cluttr.data.MediaItem;

import java.util.List;

public class MediaViewAdapter extends PagerAdapter {
    private SizeCallback callback;
    private ViewActivity context;
    private String mediaPath=null;
    private List<MediaItem> data;
    public MediaViewAdapter(ViewActivity context, List<MediaItem> data, SizeCallback s) {
        this.data=data;
        this.context=context;
        this.callback=s;
    }
    public MediaViewAdapter(ViewActivity context, String path, SizeCallback s) {
        this.context=context;
        this.mediaPath=path;
        this.callback=s;
    }
    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final String path;
        if (mediaPath==null) {
            path=data.get(position).getPath();
        } else {
            path=mediaPath;
        }
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(path.substring(path.lastIndexOf(".") + 1).toLowerCase());
        if (mime.endsWith("gif")) {
            ImageView gif = ((ImageView) inflater.inflate(R.layout.adapter_view_gif, container, false));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            callback.sizeChanged(options.outWidth, options.outHeight);
            container.addView(gif);
            return gif;
        } else if (mime.startsWith("image")) {
            final SubsamplingScaleImageView image = ((SubsamplingScaleImageView) inflater.inflate(R.layout.adapter_view_image, container, false));
            image.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
            image.setImage(ImageSource.uri(path));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            callback.sizeChanged(options.outWidth, options.outHeight);
            container.addView(image);
            return image;
        } else if (mime.startsWith("video")) {
            FrameLayout video = (FrameLayout) inflater.inflate(R.layout.adapter_view_video, container, false);
            video.findViewById(R.id.media_item_video_play).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, VideoActivity.class).putExtra("MEDIA_URI", path));
                }
            });
            MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
            metaRetriever.setDataSource(path);
            String height = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String width = metaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            callback.sizeChanged(Integer.parseInt(width), Integer.parseInt(height));
            container.addView(video);
            return video;
        }
        return null;
    }
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
    @Override
    public int getCount() {
        return mediaPath!=null ? 1 : data.size();
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return mediaPath!=null ? mediaPath.substring(mediaPath.lastIndexOf("/")+1) : data.get(position).getName();
    }
    public interface SizeCallback {
        void sizeChanged(int w, int h);
    }
}