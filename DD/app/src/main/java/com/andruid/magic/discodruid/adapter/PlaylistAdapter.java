package com.andruid.magic.discodruid.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.databinding.LayoutPlaylistBinding;
import com.andruid.magic.discodruid.viewholder.PlayListViewHolder;
import com.andruid.magic.mediareader.model.PlayList;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class PlaylistAdapter extends PagedListAdapter<PlayList, PlayListViewHolder> {
    private static final DiffUtil.ItemCallback<PlayList> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<PlayList>() {
                @Override
                public boolean areItemsTheSame(@NonNull PlayList oldItem, @NonNull PlayList newItem) {
                    return oldItem.getPlayListId()==newItem.getPlayListId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull PlayList oldItem, @NonNull PlayList newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public PlaylistAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public PlayListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutPlaylistBinding binding = LayoutPlaylistBinding.inflate(inflater,parent,false);
        return new PlayListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayListViewHolder holder, int position) {
        PlayList playList = getItem(position);
        holder.bind(playList);
    }
}