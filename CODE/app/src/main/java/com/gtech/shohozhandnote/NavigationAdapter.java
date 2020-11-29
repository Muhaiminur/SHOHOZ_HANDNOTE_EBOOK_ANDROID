package com.gtech.shohozhandnote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.ViewHolder> {

    private List<NavigationData> navigationDatas;
    private INavigation listener;

    public NavigationAdapter(INavigation listener) {
        navigationDatas = new ArrayList<>();
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @Nullable
        @BindView(R.id.tvNavigationName)
        TextView tvNavigationName;

        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onViewClick(Integer.parseInt(view.getTag().toString()));
        }
    }

    @Override
    public NavigationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_navigation, parent, false);

        return new NavigationAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NavigationAdapter.ViewHolder holder, int position) {
        NavigationData navigationData = navigationDatas.get(position);
        if (holder.tvNavigationName != null) {
            holder.tvNavigationName.setText(navigationData.getName());
        }
        holder.itemView.setTag(position);
        holder.itemView.setBackgroundResource(navigationData.isSelected() ? R.color.ripple_color : android.R.color.transparent);
    }

    @Override
    public int getItemCount() {
        return navigationDatas.size();
    }

    public void refreshAdapter(ArrayList<NavigationData> data) {
        navigationDatas.clear();
        navigationDatas.addAll(data);
        notifyDataSetChanged();
    }

    public void setSelected(int position) {
        for (int i = 0; i < navigationDatas.size(); i++) {
            navigationDatas.get(i).setSelected(i == position);
        }

        notifyDataSetChanged();
    }
}