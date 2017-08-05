package org.polaric.cluttr.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.polaric.cluttr.R;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.subjects.PublishSubject;

public class AlbumAdapter extends RealmRecyclerViewAdapter<Album, AlbumAdapter.ItemViewHolder> {
    private final PublishSubject<Album> onClickSubject = PublishSubject.create();
    private Context context;

    public AlbumAdapter(Context context, RealmResults<Album> query) {
        super(context, query, true);
        this.context=context;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder ViewHolder, int i) {
        ViewHolder.albumCount.setText(Integer.toString(getData().get(i).getSize()));
        ViewHolder.albumTitle.setText(getData().get(i).getName());
        Glide.with(context).load(getData().get(i).getCoverPath()).asBitmap().into(ViewHolder.albumCover);

    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final ItemViewHolder holder = new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_album_item, viewGroup, false));
        holder.albumTouch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSubject.onNext(getData().get(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    protected static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.album_cover) ImageView albumCover;
        @BindView(R.id.album_title) TextView albumTitle;
        @BindView(R.id.album_count) TextView albumCount;
        @BindView(R.id.touch_interceptor) View albumTouch;

        public ItemViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }
    }

    public Observable<Album> getOnClickObservable() {
        return onClickSubject;
    }
}
