package com.andruid.magic.discodruid.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.PlayList;

import java.util.List;

public class SpinnerAdapter extends BaseAdapter {
    private List<PlayList> playLists;
    private Context mContext;

    public List<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(List<PlayList> playLists) {
        this.playLists = playLists;
        notifyDataSetChanged();
    }

    public SpinnerAdapter(List<PlayList> playLists, Context mContext) {
        this.playLists = playLists;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return playLists.size();
    }

    @Override
    public Object getItem(int position) {
        return playLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView==null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_spinner_playlist, parent,false);
        }
        TextView nameTV = convertView.findViewById(R.id.spinner_nameTV);
        TextView countTV = convertView.findViewById(R.id.spinner_countTV);
        PlayList playList = playLists.get(position);
        nameTV.setText(playList.getName());
        String str = String.valueOf(playList.getSongCount()) + " songs";
        countTV.setText(str);
        return convertView;
    }
}