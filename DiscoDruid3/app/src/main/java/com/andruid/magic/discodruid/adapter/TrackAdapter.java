package com.andruid.magic.discodruid.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.databinding.LayoutTrackBinding;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;
import com.annimon.stream.Stream;

import java.util.NoSuchElementException;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;

public class TrackAdapter extends PagedListAdapter<Track,TrackViewHolder> {
    private long playingId = -1;
    private static final DiffUtil.ItemCallback<Track> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Track>() {
                @Override
                public boolean areItemsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
                    return oldItem.getPath().equalsIgnoreCase(newItem.getPath());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public TrackAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        LayoutTrackBinding binding = LayoutTrackBinding.inflate(layoutInflater,parent,false);
        return new TrackViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder holder, int position) {
        Track track = getItem(position);
        if(track==null)
            return;
        if(track.getAudioId()== playingId)
            holder.itemView.setSelected(true);
        else
            holder.itemView.setSelected(false);
        holder.bind(track);
    }

    public void setPlayingTrackId(long playingTrackId) {
        Log.d("tracksellog","playing:"+playingTrackId);
        if(getCurrentList()==null)
            return;
        try {
            if(playingId !=-1) {
                Track track = Stream.of(getCurrentList())
                        .filter(t -> t.getAudioId() == playingId)
                        .single();
                int oldPos = getCurrentList().indexOf(track);
                if(oldPos!=-1)
                    notifyItemChanged(oldPos);
            }
            playingId = playingTrackId;
            Track track = Stream.of(getCurrentList())
                    .filter(t -> t.getAudioId() == playingId)
                    .single();
            int newPos = getCurrentList().indexOf(track);
            if(newPos!=-1)
                notifyItemChanged(newPos);
        }
        catch (NoSuchElementException | IllegalStateException e){
            e.printStackTrace();
        }
    }
}