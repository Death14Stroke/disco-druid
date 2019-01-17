package com.andruid.magic.discodruid.adapter;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.viewholder.AlbumViewHolder;

public class PagedAlbumAdapter extends PagedListAdapter<Album,AlbumViewHolder> {
    private Context mContext;

    public PagedAlbumAdapter(Context mContext) {
        super(DIFF_CALLBACK);
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_album,viewGroup,false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder albumViewHolder, int position) {
        Album album = getItem(position);
        albumViewHolder.setItem(album,mContext);
    }

    private static final DiffUtil.ItemCallback<Album> DIFF_CALLBACK = new DiffUtil.ItemCallback<Album>() {
        @Override
        public boolean areItemsTheSame(@NonNull Album oldAlbum, @NonNull Album newAlbum) {
            return oldAlbum.getAlbumId().equals(newAlbum.getAlbumId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Album oldAlbum, @NonNull Album newAlbum) {
            return oldAlbum.equals(newAlbum);
        }
    };
}