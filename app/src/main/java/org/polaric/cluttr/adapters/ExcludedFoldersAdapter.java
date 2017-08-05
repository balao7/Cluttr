package org.polaric.cluttr.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.polaric.cluttr.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subjects.PublishSubject;

public class ExcludedFoldersAdapter extends RecyclerView.Adapter<ExcludedFoldersAdapter.ItemViewHolder> {
    private Context context;
    private final PublishSubject<Integer> onClickSubject = PublishSubject.create();
    private List<String> folders = new ArrayList<>();

    public ExcludedFoldersAdapter(Context context) {
        this.context=context;
    }

    public void update(List<String> list) {
        folders=list;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    @Override
    public void onBindViewHolder(ItemViewHolder ViewHolder, int i) {
        ViewHolder.item.setText(folders.get(i).substring(folders.get(i).lastIndexOf("/")+1,folders.get(i).length()));
        ViewHolder.subItem.setText(folders.get(i));
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        final ItemViewHolder holder = new ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.adapter_excluded_folders, viewGroup, false));
        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onClickSubject.onNext(holder.getAdapterPosition());
                return true;
            }
        });

        return holder;
    }

    protected static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.excluded_folders_item) TextView item;
        @BindView(R.id.excluded_folders_item_subtitle) TextView subItem;
        @BindView(R.id.exclude_folder_container) View container;

        public ItemViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }
    }

    public Observable<Integer> getOnClickObservable() {
        return onClickSubject;
    }
}
