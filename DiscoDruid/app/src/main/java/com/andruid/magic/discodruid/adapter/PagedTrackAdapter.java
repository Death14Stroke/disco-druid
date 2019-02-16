package com.andruid.magic.discodruid.adapter;

import androidx.paging.PagedListAdapter;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.viewholder.AlbumTracksViewHolder;
import com.andruid.magic.discodruid.viewholder.ArtistTracksViewHolder;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PagedTrackAdapter extends PagedListAdapter<Track,RecyclerView.ViewHolder> {
    private Context mContext;
    private List<String> selectedTrackIds = new ArrayList<>();
    private long playingTrackId;
    private LongSparseArray<Integer> positionMap = new LongSparseArray<>();
    private int viewType;

    public PagedTrackAdapter(Context mContext, int viewType) {
        super(DIFF_CALLBACK);
        this.mContext = mContext;
        this.viewType = viewType;
    }

    public void setSelectedTrackIds(List<String> selectedTrackIds) {
        this.selectedTrackIds = selectedTrackIds;
        notifyDataSetChanged();
    }

    public void setPlayingTrackId(long playingTrackId) {
        Integer integer = positionMap.get(this.playingTrackId);
        if(integer!=null) {
            int oldPos = integer;
            notifyItemChanged(oldPos);
        }
        integer = positionMap.get(playingTrackId);
        if(integer!=null) {
            int newPos = integer;
            notifyItemChanged(newPos);
        }
        this.playingTrackId = playingTrackId;
    }

    public void reset(){
        playingTrackId = -1;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        switch (viewType){
            case Constants.VIEW_ALBUM_TRACKS:
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_album_tracks,viewGroup,false);
                viewHolder = new AlbumTracksViewHolder(view);
                break;
            case Constants.VIEW_ARTIST_TRACKS:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_artist_track,viewGroup,false);
                viewHolder = new ArtistTracksViewHolder(view);
                break;
            default:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_track,viewGroup,false);
                viewHolder = new TrackViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Track track = getItem(position);
        if (track != null) {
            positionMap.append(track.getAudioId(),position);
        }
        if (track != null) {
            if(track.getAudioId()==playingTrackId)
                viewHolder.itemView.setSelected(true);
            else
                viewHolder.itemView.setSelected(false);
        }
        if (selectedTrackIds.contains(String.valueOf(Objects.requireNonNull(track).getAudioId()))){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                viewHolder.itemView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
            else
                viewHolder.itemView.setBackground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                viewHolder.itemView.setForeground(new ColorDrawable(ContextCompat.getColor(mContext,android.R.color.transparent)));
            else
                viewHolder.itemView.setBackground(new ColorDrawable(ContextCompat.getColor(mContext,R.color.colorMultiSelect)));
        }
        switch (getItemViewType(position)){
            case Constants.VIEW_ALBUM_TRACKS:
                ((AlbumTracksViewHolder)viewHolder).setItem(track);
                break;
            case Constants.VIEW_ARTIST_TRACKS:
                ((ArtistTracksViewHolder)viewHolder).setItem(track,mContext);
                break;
            default:
                ((TrackViewHolder)viewHolder).setItem(track,mContext);
                break;
        }
    }

    private static final DiffUtil.ItemCallback<Track> DIFF_CALLBACK = new DiffUtil.ItemCallback<Track>() {
        @Override
        public boolean areItemsTheSame(@NonNull Track oldTrack, @NonNull Track newTrack) {
            return oldTrack.getAudioId()==newTrack.getAudioId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Track oldTrack, @NonNull Track newTrack) {
            return oldTrack.equals(newTrack);
        }
    };
}