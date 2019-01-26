package com.andruid.magic.discodruid.adapter;

import android.util.Log;

import com.andruid.magic.discodruid.model.HeaderItem;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.model.TrackItem;
import com.andruid.magic.discodruid.viewholder.TrackViewHolder;
import com.xwray.groupie.Group;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Section;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;

public class TrackAdapter extends GroupAdapter<TrackViewHolder> {
    private List<Section> sectionList;
    private static final DiffUtil.ItemCallback<Track> DIFF_CALLBACK = new DiffUtil.ItemCallback<Track>() {
        @Override
        public boolean areItemsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
            return oldItem.getPath().equals(newItem.getPath());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Track oldItem, @NonNull Track newItem) {
            return oldItem.equals(newItem);
        }
    };
    private final AsyncPagedListDiffer<Track> mDiffer = new AsyncPagedListDiffer<>(this, DIFF_CALLBACK);

    public TrackAdapter(){
        sectionList = new ArrayList<>();
        Section section;
        for(int i=0;i<26;i++){
            section = new Section();
            section.setHeader(new HeaderItem((char) ('A'+i)));
            sectionList.add(section);
        }
        section = new Section();
        section.setHeader(new HeaderItem('$'));
        addAll(sectionList);
    }

    public void submitList(PagedList<Track> pagedList){
        mDiffer.submitList(pagedList);
    }

    @Override
    public void add(@NonNull Group group) {
        Log.d("adapterlog","called add");
        if(group instanceof TrackItem){
            char key = Character.toUpperCase(((TrackItem) group).getTrack().getTitle().charAt(0));
            if(Character.isAlphabetic(key))
                sectionList.get(key-'A').add(group);
            else
                sectionList.get(26).add(group);
        }
    }
}