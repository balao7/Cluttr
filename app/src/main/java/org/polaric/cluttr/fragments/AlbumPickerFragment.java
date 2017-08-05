package org.polaric.cluttr.fragments;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.polaric.cluttr.Cluttr;
import org.polaric.cluttr.R;
import org.polaric.cluttr.activities.MainActivity;
import org.polaric.cluttr.adapters.AlbumAdapter;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.functions.Action1;

public class AlbumPickerFragment extends BaseFragment implements View.OnClickListener, Action1<Album> {
    private OnItemSelectedListener listener;
    private Unbinder unbinder;

    @BindView(R.id.album_picker_recycler) protected RecyclerView albumRecycler;
    @BindView(R.id.album_picker_toolbar) protected Toolbar toolbar;

    private AlbumAdapter adapter;

    private Realm realm;

    public AlbumPickerFragment() {

    }

    @Override
    public void onClick(View view) {
        getFragmentManager().popBackStack();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (listener!=null) {
            listener.onItemSelected(null);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album_picker, container, false);
        unbinder = ButterKnife.bind(this,root);
        return root;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener l) {
        listener=l;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).lockDrawer(true);
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setTitle(R.string.select_album);
        adapter = new AlbumAdapter(getContext(), getResults());
        albumRecycler.setLayoutManager(new GridLayoutManager(getContext(),getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? Media.getSizePortrait() : Media.getSizeLandscape()));
        albumRecycler.setAdapter(adapter);

        adapter.getOnClickObservable().subscribe(this);

    }

    @Override
    public void call(Album album) {
        if (listener!=null) {
            getFragmentManager().popBackStack();
            listener.onItemSelected(album);
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface OnItemSelectedListener {
        abstract void onItemSelected(Album album);
    }

}
