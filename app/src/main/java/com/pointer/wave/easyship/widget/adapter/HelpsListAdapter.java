package com.pointer.wave.easyship.widget.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.pointer.wave.easyship.R;
import com.pointer.wave.easyship.pojo.TipsBen;
import com.pointer.wave.easyship.widget.feedback.TouchFeedback;

import java.util.List;

public class HelpsListAdapter extends RecyclerView.Adapter<HelpsListAdapter.HelpHolder> implements TouchFeedback.OnFeedBackListener {

    private List<String> list;
    private TouchFeedback touchFeedback;
    private final Gson gson = new Gson();
    public HelpsListAdapter(List<String> list, Context context) {
        this.list = list;
        touchFeedback = TouchFeedback.newInstance(context);
    }

    @NonNull
    @Override
    public HelpHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        HelpHolder helpHolder = new HelpHolder(View.inflate(parent.getContext(), R.layout.item_help_layout, null));
        //touchFeedback.setOnFeedBackListener(this, helpHolder.itemView, false);
        return helpHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull HelpHolder holder, int position) {
        TipsBen tipsBen = gson.fromJson(list.get(position), TipsBen.class);
        holder.textView.setText(tipsBen.getName().replace(".txt", ""));
        holder.button.setOnClickListener((v)->{
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse(tipsBen.getContent()));
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<String> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onLongClick(View view) {

    }

    static class HelpHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public TextView button;
        public HelpHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
            button = itemView.findViewById(R.id.show_help);
        }
    }

}
