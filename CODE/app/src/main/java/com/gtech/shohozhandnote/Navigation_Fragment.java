package com.gtech.shohozhandnote;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class Navigation_Fragment extends Fragment implements INavigation {
    Unbinder unbinder;
    @BindView(R.id.rvNavigation)
    RecyclerView rvNavigation;
    View view;
    public static NavigationAdapter adapter;
    public static final String TAG = Navigation_Fragment.class.getSimpleName();
    ArrayList<NavigationData> DataArrayList = new ArrayList<>();

    public static Navigation_Fragment newInstance(ArrayList<NavigationData> data) {
        Navigation_Fragment navigationFragment = new Navigation_Fragment(data);
        Bundle args = new Bundle();
        navigationFragment.setArguments(args);
        return navigationFragment;
    }

    public Navigation_Fragment(ArrayList<NavigationData> data) {
        // Required empty public constructor
        DataArrayList = data;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_navigation_, container, false);
        unbinder = ButterKnife.bind(this, view);
        fillData();
        setAdapter();
        adapter.setSelected(0);
        return view;
    }

    private ArrayList<NavigationData> fillData() {
        ArrayList<NavigationData> navigationDataArrayList = new ArrayList<>();
        return navigationDataArrayList;
    }

    private void setAdapter() {
        adapter = new NavigationAdapter(this);
        rvNavigation.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvNavigation.setAdapter(adapter);

        //adapter.refreshAdapter(fillData());
        adapter.refreshAdapter(DataArrayList);
    }

    @Override
    public void onViewClick(int position) {
        Log.d(TAG, "position " + position);
        //replaceFragment(position);

        if (Menu_Activity.status.equals("online")) {
            Log.d(TAG, "online position " + position);
            replaceFragment(position);
        } else {
            Log.d(TAG, "offline position " + position);
            replaceFragmentoffline(position);
        }
    }

    @Override
    public void onIconClick(int position) {
        Log.d(TAG, "Icon" + position);
        if (Menu_Activity.status.equals("online")) {
            Log.d(TAG, "online position " + position);
            replaceFragment(position);
        } else {
            Log.d(TAG, "offline position " + position);
            replaceFragmentoffline(position);

        }
    }

    private void replaceFragment(int position) {
        ((Menu_Activity) getActivity()).replaceFragment(position);
        adapter.setSelected(position);
    }

    private void replaceFragmentoffline(int position) {
        ((Menu_Activity) getActivity()).replaceFragmentOffline(position);
        adapter.setSelected(position);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
