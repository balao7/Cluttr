package org.polaric.cluttr.fragments;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.activities.MainActivity;
import org.polaric.cluttr.adapters.AlbumAdapter;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;
import org.polaric.cluttr.io.MediaLoader;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.functions.Action1;

public class AlbumFragment extends BaseFragment implements View.OnClickListener, Action1<Album>, MainActivity.AuthenticationPassedCallback {
    private Unbinder unbinder;
    private boolean suspended=false;

    @BindView(R.id.album_recycler) protected RecyclerView albumRecycler;
    @BindView(R.id.toolbar) protected Toolbar toolbar;
    @BindView(R.id.no_media) protected TextView emptyWarning;

    private boolean isOperationInProgress=false;

    private AlbumAdapter adapter;

    private Realm realm;

    public AlbumFragment() {

    }

    @Override
    public void onClick(View view) {
        if (((MainActivity) getActivity()).getPickIntent()) {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();
        } else {
            ((MainActivity) getActivity()).openDrawer();
        }
    }

    Toolbar.OnMenuItemClickListener actionBarListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (!isOperationInProgress) {
                switch (item.getItemId()) {
                    case R.id.album_alphabetical: {
                        isOperationInProgress=true;
                        item.setChecked(true);
                        Media.setAlbumSortType(getContext(), Media.AlbumSortType.ALPHABETICAL, Media.isSortAscending());
                        adapter.updateData(getResults());
                        isOperationInProgress=false;
                        break;
                    }
                    case R.id.album_size: {
                        isOperationInProgress=true;
                        item.setChecked(true);
                        Media.setAlbumSortType(getContext(), Media.AlbumSortType.SIZE, Media.isSortAscending());
                        adapter.updateData(getResults());
                        isOperationInProgress=false;
                        break;
                    }
                    case R.id.album_ascending: {
                        isOperationInProgress=true;
                        item.setChecked(!item.isChecked());
                        Media.setAlbumSortType(getContext(), Media.getSortType(), item.isChecked());
                        adapter.updateData(getResults());
                        isOperationInProgress=false;
                        break;
                    }
                    case R.id.album_menu_hide: {
                        if (PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("hidden_secure",false) && !Media.isShowHidden()) {
                            ((MainActivity) getActivity()).openFingerprintDialog(AlbumFragment.this);
                        } else {
                            Media.setShowHidden(!Media.isShowHidden(), getContext());
                            toolbar.getMenu().findItem(R.id.album_menu_hide).setIcon(Media.isShowHidden() ? R.drawable.ic_visibility_white_48px : R.drawable.ic_visibility_off_white_48px);
                            adapter.updateData(getResults());
                        }
                        break;
                    }
                }

            }
            return false;
        }
    };

    @Override
    public void onAuthenticationPassed() {
        Media.setShowHidden(true, getContext());
        toolbar.getMenu().findItem(R.id.album_menu_hide).setIcon(R.drawable.ic_visibility_white_48px);
        adapter.updateData(getResults());
    }

    private void initSortMenu() {
        toolbar.getMenu().findItem(R.id.album_ascending).setChecked(Media.isSortAscending());
        if (Media.getSortType()==Media.AlbumSortType.ALPHABETICAL) {
            toolbar.getMenu().findItem(R.id.album_alphabetical).setChecked(true);
        } else {
            toolbar.getMenu().findItem(R.id.album_size).setChecked(true);
        }
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_album, container, false);
        unbinder = ButterKnife.bind(this,root);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).lockDrawer(false);
        ((MainActivity) getActivity()).toggleTransparentNav(false);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setTitle(R.string.app_name);
        if (((MainActivity) getActivity()).getPickIntent()) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_menu_white_48px);
        }
        toolbar.inflateMenu(R.menu.album_action_toolbar);

        toolbar.getMenu().findItem(R.id.album_menu_hide).setIcon(Media.isShowHidden() ? R.drawable.ic_visibility_white_48px : R.drawable.ic_visibility_off_white_48px);

        toolbar.setOnMenuItemClickListener(actionBarListener);
        initSortMenu();
        adapter = new AlbumAdapter(getContext(), getResults());
        albumRecycler.setLayoutManager(new GridLayoutManager(getContext(),getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? Media.getSizePortrait() : Media.getSizeLandscape()));
        albumRecycler.setAdapter(adapter);
        adapter.getOnClickObservable().subscribe(this);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (suspended) {
            for (String key : Media.albumsNeedReloaded()) {
                MediaLoader.refreshAlbum(key)
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {

                            }
                        });
            }
        }

        //TODO: NOT HAPPY WITH THIS IF
        if (realm.where(Album.class).findAll().size()<1) {
            Log.e(Util.LOG_TAG, "No albums are displaying, assuming reload is needed");
            MediaLoader.init()
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean aBoolean) {
                            if (realm.where(Album.class).findAll().size()<1) {
                                emptyWarning.setVisibility(View.VISIBLE);
                            } else {
                                emptyWarning.setVisibility(View.GONE);
                            }
                        }
                    });
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        suspended=true;
    }

    @Override
    public void call(Album album) {
        Log.d(Util.LOG_TAG,"Loading album " + album.getName());
        try {
            Fragment fragment = new MediaFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Util.ALBUM_PASS_KEY,album.getPath());
            fragment.setArguments(bundle);
            FragmentTransaction trans = getFragmentManager().beginTransaction();
            if (Media.useAnimations()) {
                trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
            }
            trans
                    .replace(((ViewGroup) getView().getParent()).getId(), fragment, Util.Fragment.FRAGMENT_ALBUM_DETAIL_TAG)
                    .addToBackStack(null)
                    .commit();
        } catch (Exception e) {
            Log.e(Util.LOG_TAG,"Error while loading MediaFragment");
            e.printStackTrace();
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
