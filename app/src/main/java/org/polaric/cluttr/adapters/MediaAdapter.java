package org.polaric.cluttr.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;
import org.polaric.cluttr.data.MediaItem;
import org.polaric.cluttr.widgets.AspectImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.Sort;
import rx.Observable;
import rx.subjects.PublishSubject;

public class MediaAdapter extends RealmRecyclerViewAdapter<MediaItem, MediaAdapter.ItemViewHolder> {
    private Context context;
    private final PublishSubject<MediaItem> onClickSubject = PublishSubject.create();
    private final PublishSubject<MediaItem> onLongClickSubject = PublishSubject.create();
    private String query = null;
    private Album album;

    public MediaAdapter(Context context, Album album) {
        super(context, getResults(album, null), true);
        this.context=context;
        this.album=album;
    }

    private static OrderedRealmCollection<MediaItem> getResults(Album album, String query) {
        switch (Media.getMediaSortType()) {
            case ALPHABETICAL: {
                return album.getMedia().where().contains("name", query).findAll().sort("name", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
            }
            case DATE: {
                return album.getMedia().where().contains("name", query).findAll().sort("date", Media.isMediaSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
            }
        }
        return null;
    }

    public void updateQuery() {
        updateData(getResults(album, query));
    }

    public void filter(String search) {
        if (search.isEmpty()) {
            cancelFilter();
            return;
        }
        Long time = System.currentTimeMillis();
        query=search;
        updateQuery();
        Log.d(Util.LOG_TAG, "Filtered query " + query + " in " + (System.currentTimeMillis() - time) + " milliseconds");
    }

    public void cancelFilter() {
        query=null;
        updateQuery();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder ViewHolder, int i) {
        ViewHolder.bind(context, getData().get(i));
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final ItemViewHolder holder;
        if (Media.isStaggered()) {
            holder = new StaggeredViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_media_item_staggered, viewGroup, false));
        } else {
            holder = new SquareViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_media_item, viewGroup, false));
        }
        holder.getClickSubject().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSubject.onNext(getData().get(holder.getAdapterPosition()));
            }
        });
        holder.getClickSubject().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onLongClickSubject.onNext(getData().get(holder.getAdapterPosition()));
                return true;
            }
        });
        return holder;
    }

    protected abstract static class ItemViewHolder extends RecyclerView.ViewHolder{

        public ItemViewHolder(View v) {
            super(v);
        }

        public abstract void bind(Context context, MediaItem media);

        public abstract View getClickSubject();
    }

    protected static class SquareViewHolder extends ItemViewHolder{

        @BindView(R.id.media_item) ImageView mediaItem;
        @BindView(R.id.media_item_check) ImageView check;

        public SquareViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }

        @Override
        public void bind(Context context, MediaItem media) {
            try {
                Glide.with(context).load(media.getPath()).asBitmap().into(mediaItem);
            } catch (Exception e) {
                Log.e(Util.LOG_TAG,"Failed to load image " + media.getName());
            }
            if (media.isSelected()) {
                check.setVisibility(View.VISIBLE);
            } else {
                check.setVisibility(View.GONE);
            }
        }

        @Override
        public View getClickSubject() {
            return mediaItem;
        }
    }

    protected static class StaggeredViewHolder extends ItemViewHolder{
        @BindView(R.id.media_item) AspectImageView mediaItem;
        @BindView(R.id.media_item_check) ImageView check;

        public StaggeredViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }

        @Override
        public void bind(final Context context, final MediaItem media) {
            if (media.getAspectRatio()==-1) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(media.getPath(), options);
                final float width = options.outWidth;
                final float height = options.outHeight;
                Realm realm = Realm.getDefaultInstance();
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        media.setAspectRatio(height/width);
                    }
                });
                realm.close();
            }

            mediaItem.setAspect(media.getAspectRatio());
            Glide.with(context).load(media.getPath()).into(mediaItem);

            if (media.isSelected()) {
                check.setVisibility(View.VISIBLE);
            } else {
                check.setVisibility(View.GONE);
            }
        }

        @Override
        public View getClickSubject() {
            return mediaItem;
        }
    }

    public Observable<MediaItem> getOnLongClickObservable() {
        return onLongClickSubject;
    }

    public Observable<MediaItem> getOnClickObservable() {
        return onClickSubject;
    }
}