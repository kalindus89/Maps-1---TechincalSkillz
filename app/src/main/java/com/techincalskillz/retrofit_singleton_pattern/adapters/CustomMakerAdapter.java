package com.techincalskillz.retrofit_singleton_pattern.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.techincalskillz.R;

public class CustomMakerAdapter implements GoogleMap.InfoWindowAdapter {
    private  View mWindow;
    private Context mContext;

    public CustomMakerAdapter(Context mContext) {
        this.mContext = mContext;
        mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_maker_info,null);

    }

    private void rendowWindowText(Marker maker, View view){

        TextView tvTitle= (TextView) view.findViewById(R.id.title);
        TextView tvSnippet= (TextView) view.findViewById(R.id.snippet);

        if(!maker.getTitle().equals("")){
            tvTitle.setText(maker.getTitle());
        }

        if(!maker.getSnippet().equals("")){
            tvSnippet.setText(maker.getSnippet());
        }



    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        rendowWindowText(marker,mWindow);
        return mWindow;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        rendowWindowText(marker,mWindow);
        return mWindow;
    }
}
