package com.andruid.magic.discodruid.adapter;

import androidx.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.viewholder.PlayListViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PagedPlaylistAdapter extends PagedListAdapter<PlayList,PlayListViewHolder> {
    private Context mContext;
    private List<String> selectedPlayListIds = new ArrayList<>();

    public PagedPlaylistAdapter(Context mContext) {
        super(DIFF_CALLBACK);
        this.mContext = mContext;
    }

    public void setSelectedPlayListIds(List<String> selectedPlayListIds) {
        this.selectedPlayListIds = selectedPlayListIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_playlist, viewGroup, false);
        return new PlayListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder playListViewHolder, int position) {
        PlayList playList = getItem(position);
        playListViewHolder.setItem(mContext, playList);
        if (selectedPlayListIds.contains(String.valueOf(Objects.requireNonNull(playList).getPlayListId()))){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                playListViewHolder.itemView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
            else
                playListViewHolder.itemView.setBackground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                playListViewHolder.itemView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext,android.R.color.transparent)));
            else
                playListViewHolder.itemView.setBackground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
        }
    }

    private static final DiffUtil.ItemCallback<PlayList> DIFF_CALLBACK = new DiffUtil.ItemCallback<PlayList>() {
        @Override
        public boolean areItemsTheSame(@NonNull PlayList oldPlaylist, @NonNull PlayList newPlaylist) {
            return oldPlaylist.getPlayListId() == newPlaylist.getPlayListId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull PlayList oldPlaylist, @NonNull PlayList newPlaylist) {
            return oldPlaylist.equals(newPlaylist);
        }
    };
}