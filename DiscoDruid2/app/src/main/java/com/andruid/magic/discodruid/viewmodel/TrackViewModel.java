package com.andruid.magic.discodruid.viewmodel;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.fragment.TrackFragment;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.util.MediaUtils;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TrackViewModel extends ViewModel {
    private MutableLiveData<List<Track>> trackLiveData = new MutableLiveData<>();

    public void getTracks(MediaBrowserCompat mediaBrowserCompat, Bundle options, TrackFragment.TracksLoadedListener mListener){
        String parentId = Constants.TRACK;
        if(options.isEmpty())
            mediaBrowserCompat.subscribe(parentId, new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
                    super.onChildrenLoaded(parentId, children);
                    List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                    trackLiveData.postValue(trackList);
                    mListener.onTracksLoaded(trackLiveData);
                }
            });
        else
            mediaBrowserCompat.subscribe(parentId, options, new MediaBrowserCompat.SubscriptionCallback() {
                @Override
                public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                    super.onChildrenLoaded(parentId, children, options);
                    List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                    trackLiveData.postValue(trackList);
                    mListener.onTracksLoaded(trackLiveData);
                }
            });
    }
}