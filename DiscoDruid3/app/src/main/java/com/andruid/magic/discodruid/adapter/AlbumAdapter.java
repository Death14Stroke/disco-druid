package com.andruid.magic.discodruid.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.databinding.LayoutAlbumBinding;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.viewholder.AlbumViewHolder;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class AlbumAdapter extends PagedListAdapter<Album, AlbumViewHolder> {
    private static final DiffUtil.ItemCallback<Album> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Album>() {
                @Override
                public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.getAlbumId().equalsIgnoreCase(newItem.getAlbumId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public AlbumAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutAlbumBinding binding = LayoutAlbumBinding.inflate(inflater,parent,false);
        return new AlbumViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = getItem(position);
        holder.bind(album);
    }
}