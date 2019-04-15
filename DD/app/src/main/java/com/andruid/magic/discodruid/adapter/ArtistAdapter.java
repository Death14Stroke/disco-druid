package com.andruid.magic.discodruid.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.databinding.LayoutArtistBinding;
import com.andruid.magic.discodruid.viewholder.ArtistViewHolder;
import com.andruid.magic.mediareader.model.Artist;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class ArtistAdapter extends PagedListAdapter<Artist, ArtistViewHolder> {
    private static final DiffUtil.ItemCallback<Artist> DIFF_UTIL =
            new DiffUtil.ItemCallback<Artist>() {
                @Override
                public boolean areItemsTheSame(@NonNull Artist oldItem, @NonNull Artist newItem) {
                    return oldItem.getArtistId().equalsIgnoreCase(newItem.getArtistId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Artist oldItem, @NonNull Artist newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public ArtistAdapter(){
        super(DIFF_UTIL);
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutArtistBinding binding = LayoutArtistBinding.inflate(inflater,parent,false);
        return new ArtistViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder holder, int position) {
        Artist artist = getItem(position);
        holder.bind(artist);
    }
}