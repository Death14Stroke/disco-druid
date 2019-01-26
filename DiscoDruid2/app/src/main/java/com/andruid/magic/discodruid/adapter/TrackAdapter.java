package com.andruid.magic.discodruid.adapter;

import android.content.Context;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;
import com.xwray.groupie.GroupAdapter;

import androidx.annotation.NonNull;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;

public class TrackAdapter extends GroupAdapter<TrackViewHolder> {
    private Context mContext;
    private final DiffUtil.ItemCallback<Track> DIFF_CALLBACK = new DiffUtil.ItemCallback<Track>() {
        @Override
        public boolean areItemsTheSame(@NonNull Track oldTrack, @NonNull Track newTrack) {
            return oldTrack.getAudioId() == newTrack.getAudioId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Track oldTrack, @NonNull Track newTrack) {
            return oldTrack.equals(newTrack);
        }
    };
    private AsyncPagedListDiffer<Track> mDiffer = new AsyncPagedListDiffer<>(this, DIFF_CALLBACK);

    public TrackAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void submitList(PagedList<Track> pagedList){
        mDiffer.submitList(pagedList, () ->
            Log.d("paginglog",pagedList.size()+""));
    }
}