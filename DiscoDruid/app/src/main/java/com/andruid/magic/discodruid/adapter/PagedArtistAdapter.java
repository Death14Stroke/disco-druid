package com.andruid.magic.discodruid.adapter;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Artist;
import com.andruid.magic.discodruid.viewholder.ArtistViewHolder;

public class PagedArtistAdapter extends PagedListAdapter<Artist,ArtistViewHolder> {
    public PagedArtistAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ArtistViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_artist,viewGroup,false);
        return new ArtistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtistViewHolder artistViewHolder, int position) {
        Artist artist = getItem(position);
        artistViewHolder.setItem(artist);
    }

    private static final DiffUtil.ItemCallback<Artist> DIFF_CALLBACK = new DiffUtil.ItemCallback<Artist>() {
        @Override
        public boolean areItemsTheSame(@NonNull Artist oldArtist, @NonNull Artist newArtist) {
            return oldArtist.getArtistId().equals(newArtist.getArtistId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Artist oldArtist, @NonNull Artist newArtist) {
            return oldArtist.equals(newArtist);
        }
    };
}