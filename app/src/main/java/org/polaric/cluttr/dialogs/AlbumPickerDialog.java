package org.polaric.cluttr.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.polaric.cluttr.Cluttr;
import org.polaric.cluttr.R;
import org.polaric.cluttr.adapters.AlbumAdapter;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.functions.Action1;

public class AlbumPickerDialog extends Dialog implements View.OnClickListener, Action1<Album> {

    @BindView(R.id.pick_album_recycler) RecyclerView recycler;
    @BindView(R.id.pick_album_toolbar) Toolbar toolbar;

    private OnItemSelectedListener listener;
    private AlbumAdapter adapter;

    Realm realm;

    public AlbumPickerDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_album_picker);
        ButterKnife.bind(this);

        realm = Realm.getDefaultInstance();

        setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setTitle(R.string.select_album);



        adapter = new AlbumAdapter(getContext(), getResults());
        recycler.setLayoutManager(new GridLayoutManager(getContext(),2));
        recycler.setAdapter(adapter);

        adapter.getOnClickObservable().subscribe(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        realm.close();
    }

    private RealmResults<Album> getResults() {
        RealmResults<Album> results = null;
        switch (Media.getSortType()) {
            case ALPHABETICAL: {
                if (Media.isShowHidden()) {
                    results = realm.where(Album.class).findAll().sort("name", Media.isSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
                    break;
                } else {
                    results = realm.where(Album.class).equalTo("hidden", false).findAll().sort("name", Media.isSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
                    break;
                }
            }
            case SIZE: {
                if (Media.isShowHidden()) {
                    results = realm.where(Album.class).findAll().sort("size", Media.isSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
                    break;
                } else {
                    results = realm.where(Album.class).equalTo("hidden", false).findAll().sort("size", Media.isSortAscending() ? Sort.ASCENDING : Sort.DESCENDING);
                    break;
                }
            }
        }
        return results;
    }

    @Override
    public void onClick(View view) {
        dismiss();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        listener=l;
    }

    @Override
    public void call(Album album) {
        if (listener!=null) {
            dismiss();
            listener.onItemSelected(album);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(Album album);
    }
}
