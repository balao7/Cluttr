package org.polaric.cluttr.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import org.polaric.cluttr.R;
import org.polaric.cluttr.Util;
import org.polaric.cluttr.activities.EditActivity;
import org.polaric.cluttr.activities.MainActivity;
import org.polaric.cluttr.activities.ViewActivity;
import org.polaric.cluttr.adapters.MediaAdapter;
import org.polaric.cluttr.data.Album;
import org.polaric.cluttr.data.Media;
import org.polaric.cluttr.data.MediaItem;
import org.polaric.cluttr.dialogs.ActionMessageDialog;
import org.polaric.cluttr.io.MediaLoader;
import org.polaric.colorful.Colorful;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.realm.Realm;
import rx.functions.Action1;

public class MediaFragment extends BaseFragment implements View.OnClickListener, Action1<MediaItem>, Toolbar.OnMenuItemClickListener, SearchView.OnQueryTextListener, MenuItemCompat.OnActionExpandListener {
    private final String STATE_OUT_SELECT="select_bool";
    private final String STATE_OUT_OPERATION="operation_bool";

    @BindView(R.id.media_app_bar) AppBarLayout appbar;
    @BindView(R.id.media_coordinator) CoordinatorLayout coordinator;
    @BindView(R.id.fastscroller) RecyclerFastScroller scroller;
    @BindView(R.id.media_toolbar) Toolbar toolbar;
    @BindView(R.id.media_file_toolbar) Toolbar fileToolbar;
    @BindView(R.id.media_recycler) RecyclerView mediaRecycler;
    @BindView(R.id.media_card_container) CardView editContainer;

    private Unbinder unbinder;
    private Album album;
    private MediaAdapter adapter;
    private boolean isSelecting=false;
    private boolean operationInProgress=false;
    private SearchView searchView;
    private boolean isRefreshInProgress=false;

    private Realm realm;

    public MediaFragment() {

    }

    @Override
    public void onClick(View view) {
        getFragmentManager().popBackStack();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try {
            if (album.countSelected()>0) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        album.clearMediaSelection();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(Util.LOG_TAG, "NPE, fix this");
        }
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


    Toolbar.OnMenuItemClickListener actionBarListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (!isRefreshInProgress) {
                switch (item.getItemId()) {
                    case R.id.media_alphabetical: {
                        isRefreshInProgress=true;
                        item.setChecked(true);
                        Media.setMediaSortType(getContext(), Media.MediaSortType.ALPHABETICAL, Media.isMediaSortAscending());
                        adapter.updateQuery();
                        MediaLoader.updateCoverPhotos();
                        isRefreshInProgress=false;
                        break;
                    }
                    case R.id.media_date: {
                        isRefreshInProgress=true;
                        item.setChecked(true);
                        Media.setMediaSortType(getContext(), Media.MediaSortType.DATE, Media.isMediaSortAscending());
                        adapter.updateQuery();
                        MediaLoader.updateCoverPhotos();
                        isRefreshInProgress=false;
                        break;
                    }
                    case R.id.media_ascending: {
                        isRefreshInProgress=true;
                        item.setChecked(!item.isChecked());
                        Media.setMediaSortType(getContext(), Media.getMediaSortType(), item.isChecked());
                        adapter.updateQuery();
                        MediaLoader.updateCoverPhotos();
                        isRefreshInProgress=false;
                        break;
                    }
                    case R.id.menu_hide: {
                        ActionMessageDialog d = new ActionMessageDialog(getContext());
                        d.setOnPositiveListener(new ActionMessageDialog.OnPositiveListener() {
                            @Override
                            public void onPositive() {
                                try {
                                    new File(album.getPath(), ".nomedia").createNewFile();
                                } catch (Exception e) {
                                    Log.e(Util.LOG_TAG, "Failed to create .nomedia file, cause: " + e.getMessage());
                                }
                                toolbar.getMenu().findItem(R.id.menu_hide).setVisible(false);
                                toolbar.getMenu().findItem(R.id.menu_unhide).setVisible(true);
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        album.setHidden(true);
                                    }
                                });
                            }
                        });
                        d.setPositiveButtonMsg(getResources().getString(R.string.hide_short));
                        d.setMsgText(getResources().getString(R.string.hide_warn));
                        d.setTitleText(getResources().getString(R.string.hide));
                        d.show();
                        break;
                    }
                    case R.id.menu_unhide: {
                        ActionMessageDialog d = new ActionMessageDialog(getContext());
                        d.setOnPositiveListener(new ActionMessageDialog.OnPositiveListener() {
                            @Override
                            public void onPositive() {
                                try {
                                    new File(album.getPath(), ".nomedia").delete();
                                } catch (Exception e) {
                                    Log.e(Util.LOG_TAG, "Failed to create .nomedia file, cause: " + e.getMessage());
                                }
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        album.setHidden(false);
                                    }
                                });
                                toolbar.getMenu().findItem(R.id.menu_hide).setVisible(true);
                                toolbar.getMenu().findItem(R.id.menu_unhide).setVisible(false);
                                if (!Media.isShowHidden()) {
                                    onClick(null);
                                }
                            }
                        });
                        d.setPositiveButtonMsg(getResources().getString(R.string.unhide_short));
                        d.setMsgText(getResources().getString(R.string.unhide_warn));
                        d.setTitleText(getResources().getString(R.string.unhide));
                        d.show();
                        break;
                    }
                }
            }
            return false;
        }
    };

    private Action1<MediaItem> onLongClickAction = new Action1<MediaItem>() {
        @Override
        public void call(final MediaItem media) {
            if (!operationInProgress && !((MainActivity) getActivity()).getPickIntent()) {
                if (!isSelecting) {
                    toggleToolbar(true);
                }
                isSelecting=true;
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        media.setSelected(!media.isSelected());
                    }
                });
                fileToolbar.setTitle(album.countSelected() + " " + getContext().getResources().getString(R.string.selected));
                adapter.notifyDataSetChanged();
                if (!checkIsSelected()) {
                    isSelecting=false;
                    toggleToolbar(false);
                }
            }
        }
    };

    private void initSortMenu() {
        toolbar.getMenu().findItem(R.id.media_ascending).setChecked(Media.isMediaSortAscending());
        if (Media.getMediaSortType()==Media.MediaSortType.ALPHABETICAL) {
            toolbar.getMenu().findItem(R.id.media_alphabetical).setChecked(true);
        } else {
            toolbar.getMenu().findItem(R.id.media_date).setChecked(true);
        }
        if (new File(album.getPath(),".nomedia").exists()) {
            toolbar.getMenu().findItem(R.id.menu_unhide).setVisible(true);
            toolbar.getMenu().findItem(R.id.menu_hide).setVisible(false);
        } else {
            toolbar.getMenu().findItem(R.id.menu_unhide).setVisible(false);
            toolbar.getMenu().findItem(R.id.menu_hide).setVisible(true);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.filter(newText.trim().toLowerCase());
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (searchView.isIconified()) searchView.setIconified(false);
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        adapter.cancelFilter();
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (!operationInProgress) {
            switch (item.getItemId()) {
                case R.id.delete: {
                    int amount = album.countSelected();
                    new AlertDialog.Builder(getContext())
                            .setMessage(getContext().getResources().getString(R.string.delete) + " " + amount + " " + getContext().getResources().getString(R.string.item) + (amount>1 ? "s" : ""))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    operationInProgress = true;
                                    final ProgressDialog pDialog = new ProgressDialog(getContext());
                                    pDialog.show();
                                    pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    MediaLoader.deleteFiles(album.getPath())
                                            .subscribe(new Action1<Boolean>() {
                                                @Override
                                                public void call(Boolean aBoolean) {
                                                    operationInProgress = false;
                                                    pDialog.dismiss();
                                                    //adapter.refreshData();
                                                    isSelecting = false;
                                                    toggleToolbar(false);
                                                    if (aBoolean) {
                                                        Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(getContext(), R.string.error, Toast.LENGTH_LONG).show();
                                                    }
                                                    if (album.getMedia().size()<1) {
                                                        MediaFragment.this.onClick(null);
                                                    }
                                                }
                                            });
                                }
                            })
                    .show();
                    break;
                }
                case R.id.move: {
                    int amount = album.countSelected();
                    new AlertDialog.Builder(getContext())
                            .setMessage(getContext().getResources().getString(R.string.move) + " " + amount + " " + getContext().getResources().getString(R.string.item) + (amount>1 ? "s" : ""))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    operationInProgress = true;
                                    AlbumPickerFragment fragment = new AlbumPickerFragment();
                                    fragment.setOnItemSelectedListener(new AlbumPickerFragment.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(final Album target) {
                                            if (target == null) {
                                                operationInProgress = false;
                                            } else {
                                                final ProgressDialog pDialog = new ProgressDialog(getContext());
                                                pDialog.show();
                                                MediaLoader.moveFiles(album.getPath(), target.getPath())
                                                        .subscribe(new Action1<Boolean>() {
                                                            @Override
                                                            public void call(Boolean aBoolean) {
                                                                operationInProgress = false;
                                                                pDialog.dismiss();
                                                                //adapter.refreshData();
                                                                isSelecting = false;
                                                                toggleToolbar(false);
                                                                if (aBoolean) {
                                                                    Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_LONG).show();
                                                                }
                                                                if (album.getMedia().size() < 1) {
                                                                    MediaFragment.this.onClick(null);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                                    FragmentTransaction trans = getFragmentManager().beginTransaction();
                                    if (Media.useAnimations()) {
                                        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                                    }
                                    trans
                                            .replace(((ViewGroup) getView().getParent()).getId(), fragment, Util.Fragment.FRAGMENT_ALBUM_DETAIL_TAG)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            })
                    .show();
                    break;
                }
                case R.id.copy: {
                    int amount = album.countSelected();
                    new AlertDialog.Builder(getContext())
                            .setMessage(getContext().getResources().getString(R.string.copy) + " " + amount + " " + getContext().getResources().getString(R.string.item) + (amount>1 ? "s" : ""))
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    operationInProgress = true;
                                    AlbumPickerFragment fragment = new AlbumPickerFragment();
                                    fragment.setOnItemSelectedListener(new AlbumPickerFragment.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(final Album target) {
                                            if (target == null) {
                                                operationInProgress = false;
                                            } else {
                                                final ProgressDialog pDialog = new ProgressDialog(getContext());
                                                pDialog.show();
                                                MediaLoader.copyFiles(album.getPath(), target.getPath())
                                                        .subscribe(new Action1<Boolean>() {
                                                            @Override
                                                            public void call(Boolean aBoolean) {
                                                                operationInProgress = false;
                                                                pDialog.dismiss();
                                                                //adapter.refreshData();
                                                                isSelecting = false;
                                                                toggleToolbar(false);
                                                                if (aBoolean) {
                                                                    Toast.makeText(getContext(), R.string.action_complete, Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    Toast.makeText(getContext(), R.string.error, Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                                    FragmentTransaction trans = getFragmentManager().beginTransaction();
                                    if (Media.useAnimations()) {
                                        trans.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out);
                                    }
                                    trans
                                            .replace(((ViewGroup) getView().getParent()).getId(), fragment, Util.Fragment.FRAGMENT_ALBUM_DETAIL_TAG)
                                            .addToBackStack(null)
                                            .commit();
                                }
                            })
                    .show();
                    break;
                }
                case R.id.share: {
                    Media.share(getContext(), album.getPath());
                }
            }
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_OUT_SELECT,isSelecting);
        outState.putBoolean(STATE_OUT_OPERATION,operationInProgress);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_media, container, false);
        unbinder = ButterKnife.bind(this,root);
        album = realm.where(Album.class).equalTo("path", getArguments().getString(Util.ALBUM_PASS_KEY)).findFirst();
        if (savedInstanceState!=null) {
            isSelecting = savedInstanceState.getBoolean(STATE_OUT_SELECT,false);
            operationInProgress = savedInstanceState.getBoolean(STATE_OUT_OPERATION,false);
        }
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) getActivity()).lockDrawer(true);
        ((MainActivity) getActivity()).toggleTransparentNav(false);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_48px);
        toolbar.setNavigationOnClickListener(this);
        toolbar.setTitle(album.getName());
        toolbar.inflateMenu(R.menu.action_toolbar);
        toolbar.setOnMenuItemClickListener(actionBarListener);

        MenuItem searchItem = toolbar.getMenu().findItem(R.id.menu_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.search));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchItem, this);

        initSortMenu();

        fileToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSelecting=false;
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        album.clearMediaSelection();
                    }
                });
                toggleToolbar(false);
                adapter.notifyDataSetChanged();
            }
        });
        fileToolbar.inflateMenu(R.menu.media_edit_toolbar);
        fileToolbar.setOnMenuItemClickListener(this);
        fileToolbar.setNavigationIcon(R.drawable.ic_close_white_48px);
        if (!Colorful.getThemeDelegate().isDark()) {
            Util.ViewUtils.colorizeToolbar(fileToolbar, Color.BLACK);
        }
        if (isSelecting) {
            editContainer.setVisibility(View.VISIBLE);
            fileToolbar.setTitle(album.countSelected() + " " + getContext().getResources().getString(R.string.selected));
        }

        adapter = new MediaAdapter(getContext(), album);

        if (Media.isStaggered()) {
            StaggeredGridLayoutManager sglm = new StaggeredGridLayoutManager(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? Media.getSizeAlbumPortrait() : Media.getSizeAlbumLandscape(), StaggeredGridLayoutManager.VERTICAL);
            //sglm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            mediaRecycler.setLayoutManager(sglm);
        } else {
            mediaRecycler.setLayoutManager(new GridLayoutManager(getContext(),getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? Media.getSizeAlbumPortrait() : Media.getSizeAlbumLandscape()));
        }
        mediaRecycler.setAdapter(adapter);
        scroller.attachRecyclerView(mediaRecycler);
        //TODO: Log gets spammed from this, find out why
        //scroller.attachAppBarLayout(coordinator,appbar);
        adapter.getOnClickObservable().subscribe(this);
        adapter.getOnLongClickObservable().subscribe(onLongClickAction);
    }

    private void loadImage(MediaItem item) {
        if (((MainActivity) getActivity()).getPickIntent()) {
            getActivity().setResult(Activity.RESULT_OK, new Intent().setData(Uri.fromFile(new File(item.getPath()))));
            getActivity().finish();
        } else {
            Log.d(Util.LOG_TAG,"Loading media: " + item.getName());
            startActivity(new Intent(getContext(), ViewActivity.class).putExtra(Util.ALBUM_PASS_KEY,album.getPath()).putExtra(Util.MEDIA_PASS_KEY,item.getPath()));
            if (Media.useAnimations()) {
                getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                getActivity().overridePendingTransition(0,0);
            }
        }
    }

    @Override
    public void call(final MediaItem media) {
        if (!operationInProgress) {
            if (isSelecting) {
                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        media.setSelected(!media.isSelected());
                    }
                });
                fileToolbar.setTitle(album.countSelected() + " " + getContext().getResources().getString(R.string.selected));
                adapter.notifyDataSetChanged();
                if (!checkIsSelected()) {
                    isSelecting=false;
                    toggleToolbar(false);
                }
            } else {
                loadImage(media);
            }
        }
    }

    private void toggleToolbar(boolean visible) {
        if (visible) {
            Util.Animations.createReveal(editContainer);
        } else {
            Util.Animations.createHide(editContainer);
        }
    }

    private boolean checkIsSelected() {
        for (MediaItem i : album.getMedia()) {
            if (i.isSelected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (album.isValid()) {
            if (album.needsReload()) {
                MediaLoader.refreshAlbum(album.getPath())
                        .subscribe(new Action1<Boolean>() {
                            @Override
                            public void call(Boolean aBoolean) {
                                //Stub
                            }
                        });
            }
        } else {
            Log.e(Util.LOG_TAG, "The reference to the album is nolonger valid, working around and closing fragment");
            onClick(null);
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
