package com.talent4assure.retrofit_get.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.talent4assure.retrofit_get.Model.Android;
import com.talent4assure.retrofit_get.R;

import java.util.ArrayList;

/**
 * Created by krishm on 4/2/2018.
 */

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    private ArrayList<Android> mAndroidList;

    public DataAdapter(ArrayList<Android> androidList) {
        mAndroidList = androidList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mTvName.setText(mAndroidList.get(position).getName());
        holder.mTvVersion.setText(mAndroidList.get(position).getVer());
        holder.mTvApi.setText(mAndroidList.get(position).getApi());
    }

    @Override
    public int getItemCount() {
        return mAndroidList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvName,mTvVersion,mTvApi;
        public ViewHolder(View view) {
            super(view);

            mTvName = (TextView)view.findViewById(R.id.tv_name);
            mTvVersion = (TextView)view.findViewById(R.id.tv_version);
            mTvApi = (TextView)view.findViewById(R.id.tv_api_level);
        }
    }
}
