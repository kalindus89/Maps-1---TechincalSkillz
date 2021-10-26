package com.techincalskillz.retrofit_singleton_pattern.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techincalskillz.MapFragmentActivity;
import com.techincalskillz.R;
import com.techincalskillz.retrofit_singleton_pattern.Model.PredictionArrayModel;

import java.util.List;

public class RecyclerAdapterAutoComplete extends RecyclerView.Adapter<RecyclerAdapterAutoComplete.MainViewHolder> {

    MapFragmentActivity context;
    List<PredictionArrayModel> getPredictionsList;

    public RecyclerAdapterAutoComplete(MapFragmentActivity context, List<PredictionArrayModel> getPredictionsList) {
        this.context = context;
        this.getPredictionsList = getPredictionsList;
    }



    @NonNull
    @Override
    public RecyclerAdapterAutoComplete.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return  new MainViewHolder(LayoutInflater.from(context).inflate(R.layout.autocompete_recycler_row,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapterAutoComplete.MainViewHolder holder, int position) {

        holder.locationDescription.setText(getPredictionsList.get(position).getDescription());
        holder.locationDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.setGetLocationFromPlaceID(getPredictionsList.get(holder.getAdapterPosition()).getDescription(),getPredictionsList.get(holder.getAdapterPosition()).getPlace_id());

            }
        });
    }

    @Override
    public int getItemCount() {
        return getPredictionsList.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder {

        TextView locationDescription;

        public MainViewHolder(@NonNull View itemView) {
            super(itemView);
            locationDescription=itemView.findViewById(R.id.locationDescription);
        }
    }


}
