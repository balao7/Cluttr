package org.polaric.cluttr.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.polaric.cluttr.R;
import org.polaric.cluttr.activities.MainActivity;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class GrantFragment extends BaseFragment {
    private Unbinder unbinder;

    public GrantFragment() {

    }

    @OnClick(R.id.grant_permission_button)
    public void requestPermission() {
        ((MainActivity) getActivity()).requestPerms();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((MainActivity) getActivity()).toggleTransparentNav(true);
        View root = inflater.inflate(R.layout.fragment_grant, container, false);
        unbinder = ButterKnife.bind(this,root);
        ((MainActivity) getActivity()).lockDrawer(true);
        return root;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
