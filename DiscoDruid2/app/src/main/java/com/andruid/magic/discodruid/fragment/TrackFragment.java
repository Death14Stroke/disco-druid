package com.andruid.magic.discodruid.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.BackgroundAudioService;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.TrackAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.model.TrackItem;
import com.andruid.magic.discodruid.viewmodel.PagedTrackViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TrackFragment extends Fragment{
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private TrackAdapter trackAdapter;
    private MediaBrowserCompat mediaBrowserCompat;
    private PagedTrackViewModel pagedTrackViewModel;

    public TrackFragment() {}

    public static TrackFragment newInstance() {
        return new TrackFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackAdapter = new TrackAdapter();
        pagedTrackViewModel = ViewModelProviders.of(this).get(PagedTrackViewModel.class);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(), new ComponentName(Objects.requireNonNull(getActivity()),BackgroundAudioService.class),
                new MediaBrowserCompat.ConnectionCallback(){
                    @Override
                    public void onConnected() {
                        super.onConnected();
                        loadTracks();
                        mediaBrowserCompat.subscribe(Constants.CURRENT_TRACK, new Bundle(), new MediaBrowserCompat.SubscriptionCallback() {
                            @Override
                            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                                super.onChildrenLoaded(parentId, children, options);
                                if(children.size() == 1) {
                                    Bundle extras = children.get(0).getDescription().getExtras();
                                    if(extras!=null) {
                                        Track track = extras.getParcelable(Constants.TRACK);
                                        if (track != null) {
                                            //TODO
                                            Log.d("paginglog","select track bg");
                                        }
                                    }
                                }
                            }
                        });
                        swipeRefreshLayout.setOnRefreshListener(() -> {
                            swipeRefreshLayout.setRefreshing(true);
                            loadTracks();
                        });
                    }

                    @Override
                    public void onConnectionFailed() {
                        super.onConnectionFailed();
                        Toast.makeText(getContext(),"Connection to media service failed",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionSuspended() {
                        super.onConnectionSuspended();
                        Toast.makeText(getContext(),"Connection to media service suspended",Toast.LENGTH_SHORT).show();
                    }
                },null);
        mediaBrowserCompat.connect();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        ButterKnife.bind(this,view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        recyclerView.setAdapter(trackAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        return view;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof TrackClickListener) {
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TrackClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.unsubscribe(Constants.CURRENT_TRACK);
        mediaBrowserCompat.disconnect();
    }

    private void loadTracks() {
        pagedTrackViewModel.getTracks(mediaBrowserCompat,null).observe(TrackFragment.this, pagedList -> {
            Log.d("adapterlog","observe callback viewmodel:"+pagedList.size());
            trackAdapter.submitList(pagedList);
            for(Track track : pagedList.snapshot()){
                trackAdapter.add(new TrackItem(track,getContext()));
            }
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    public interface TrackClickListener{
        void onTrackClicked(List<Track> trackList,int pos);
    }
}