package com.andruid.magic.discodruid.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.HeaderViewHolder;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;
import com.zhukic.sectionedrecyclerview.SectionedRecyclerViewAdapter;

import androidx.annotation.NonNull;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;

public class TrackAdapter extends SectionedRecyclerViewAdapter<HeaderViewHolder, TrackViewHolder> {
    private static final DiffUtil.ItemCallback<Track> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Track>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Track oldUser, @NonNull Track newUser) {
                    // User properties may have changed if reloaded from the DB, but ID is fixed
                    return oldUser.getAudioId() == newUser.getAudioId();
                }
                @Override
                public boolean areContentsTheSame(
                        @NonNull Track oldUser, @NonNull Track newUser) {
                    // NOTE: if you use equals, your object must properly override Object#equals()
                    // Incorrectly returning false here will result in too many animations.
                    return oldUser.equals(newUser);
                }
            };
    private final AsyncPagedListDiffer<Track> mDiffer
            = new AsyncPagedListDiffer<>(this, DIFF_CALLBACK);
    private Context mContext;

    public TrackAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void submitList(PagedList<Track> pagedList) {
        mDiffer.submitList(pagedList);
    }

    @Override
    public TrackViewHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_track,parent,false);
        return new TrackViewHolder(view);
    }

    @Override
    public HeaderViewHolder onCreateSubheaderViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.header_track,parent,false);
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(TrackViewHolder holder, int itemPosition) {
        Track track = mDiffer.getItem(itemPosition);
        holder.setItem(track,mContext);
    }

    @Override
    public void onBindSubheaderViewHolder(HeaderViewHolder subheaderHolder, int nextItemPosition) {
        Track nextTrack = mDiffer.getItem(nextItemPosition);
        if(nextTrack==null)
            return;
        char c = Character.toUpperCase(nextTrack.getTitle().charAt(0));
        subheaderHolder.textView.setText(c);
    }

    @Override
    public boolean onPlaceSubheaderBetweenItems(int position) {
        if(position+1>=mDiffer.getItemCount())
            return false;
        char curr = Character.toUpperCase(mDiffer.getItem(position).getTitle().charAt(0));
        char next = Character.toUpperCase(mDiffer.getItem(position+1).getTitle().charAt(0));
        return curr != next;
    }

    @Override
    public int getItemSize() {
        Log.d("secrv","item size:"+mDiffer.getItemCount());
        return mDiffer.getItemCount();
    }
}