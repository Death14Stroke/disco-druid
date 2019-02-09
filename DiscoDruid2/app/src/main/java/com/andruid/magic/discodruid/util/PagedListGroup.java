package com.andruid.magic.discodruid.util;

import android.util.Log;

import com.andruid.magic.discodruid.model.TrackItem;
import com.xwray.groupie.Group;
import com.xwray.groupie.GroupDataObserver;
import com.xwray.groupie.Item;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.paging.AsyncPagedListDiffer;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;

public class PagedListGroup implements Group, GroupDataObserver {
    private GroupDataObserver parentObserver;
    private final ListUpdateCallback listUpdateCallback = new ListUpdateCallback() {
        @Override
        public void onInserted(int position, int count) {
            if(count==0)
                return;
            Log.d("paginglog","inserted:"+position+":"+count+":"+differ.getItemCount());
            parentObserver.onItemRangeInserted(PagedListGroup.this, position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            parentObserver.onItemRangeRemoved(PagedListGroup.this, position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            parentObserver.onItemMoved(PagedListGroup.this, fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count, Object payload) {
            parentObserver.onItemRangeChanged(PagedListGroup.this, position, count);
        }
    };
    private final AsyncPagedListDiffer<TrackItem> differ = new AsyncPagedListDiffer<>(
            listUpdateCallback,
            new AsyncDifferConfig.Builder<>(new DiffUtil.ItemCallback<TrackItem>() {
                @Override
                public boolean areItemsTheSame(@NotNull TrackItem oldItem, @NotNull TrackItem newItem) {
                    return newItem.isSameAs(oldItem);
                }

                @Override
                public boolean areContentsTheSame(@NotNull TrackItem oldItem, @NotNull TrackItem newItem) {
                    return newItem.equals(oldItem);
                }
            }).build()
    );
    private Item placeHolder = null;

    public void setPlaceHolder(Item placeHolder) {
        this.placeHolder = placeHolder;
    }

    public void submitList(PagedList<TrackItem> newPagedList) {
        differ.submitList(newPagedList);
    }

    @Override
    public int getItemCount() {
        return differ.getItemCount();
    }

    @NonNull
    @Override
    public Item getItem(int position) {
        Item item = differ.getItem(position);
        if (item != null) {
            // TODO find more efficiency registration timing, and removing observer
            //item.registerGroupDataObserver(this);
            return item;
        }
        return placeHolder;
    }

    @Override
    public int getPosition(@NonNull Item item) {
        List<TrackItem> currentList = differ.getCurrentList();
        if (currentList == null) {
            return -1;
        }
        //noinspection SuspiciousMethodCalls
        return currentList.indexOf(item);
    }

    @Override
    public void registerGroupDataObserver(@NonNull GroupDataObserver groupDataObserver) {
        Log.d("groupobslog","registered in class");
        parentObserver = groupDataObserver;
    }

    @Override
    public void unregisterGroupDataObserver(@NonNull GroupDataObserver groupDataObserver) {
        parentObserver = null;
    }

    @Override
    public void onChanged(@NonNull Group group) {
        parentObserver.onChanged(this);
    }

    @Override
    public void onItemInserted(@NonNull Group group, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onItemChanged(@NonNull Group group, int position) {
        int index = getItemPosition(group);
        if (index >= 0) {
            parentObserver.onItemChanged(this, index);
        }
    }

    @Override
    public void onItemChanged(@NonNull Group group, int position, Object payload) {
        int index = getItemPosition(group);
        if (index >= 0) {
            parentObserver.onItemChanged(this, index, payload);
        }
    }

    @Override
    public void onItemRemoved(@NonNull Group group, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onItemRangeChanged(@NonNull Group group, int positionStart, int itemCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onItemRangeChanged(@NonNull Group group, int positionStart, int itemCount, Object payload) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onItemRangeInserted(@NonNull Group group, int positionStart, int itemCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onItemRangeRemoved(@NonNull Group group, int positionStart, int itemCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onItemMoved(@NonNull Group group, int fromPosition, int toPosition) {
        throw new UnsupportedOperationException();
    }

    private int getItemPosition(@NonNull Group group) {
        List<TrackItem> currentList = differ.getCurrentList();
        if (currentList == null) {
            return -1;
        }
        //noinspection SuspiciousMethodCalls
        return currentList.indexOf(group);
    }
}