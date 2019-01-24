package com.andruid.magic.discodruid.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.support.v4.media.MediaBrowserCompat;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.BackgroundAudioService;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.TrackAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.HeaderItem;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.model.TrackItem;
import com.andruid.magic.discodruid.util.MediaUtils;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.andruid.magic.discodruid.viewmodel.PagedTrackViewModel;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Section;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class TrackFragment extends Fragment implements ActionMode.Callback {
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private MediaBrowserCompat mediaBrowserCompat;
    private TrackAdapter trackAdapter;
    private TrackClickListener mListener;
    private ActionMode actionMode;
    private boolean isMultiSelect = false;
    private List<String> selectedTrackIds = new ArrayList<>();
    private PagedTrackViewModel pagedTrackViewModel;
    private GroupAdapter groupAdapter;

    public TrackFragment() {}

    public static TrackFragment newInstance() {
        return new TrackFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackAdapter = new TrackAdapter(getContext(), Constants.VIEW_TRACKS);
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
                                            trackAdapter.setPlayingTrackId(track.getAudioId());
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
        groupAdapter = new GroupAdapter();
        recyclerView.setAdapter(groupAdapter);
        //recyclerView.setAdapter(trackAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (isMultiSelect)
                    multiSelect(position);
                else {
                    trackAdapter.reset();
                    mListener.onTrackClicked(trackAdapter.getTrackList(), position);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (!isMultiSelect){
                    selectedTrackIds = new ArrayList<>();
                    isMultiSelect = true;
                    if (actionMode == null){
                        actionMode = Objects.requireNonNull(getActivity()).startActionMode(TrackFragment.this);
                    }
                }
                multiSelect(position);
            }
        }));
        return view;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof TrackClickListener) {
            mListener = (TrackClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement TrackClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.unsubscribe(Constants.CURRENT_TRACK);
        mediaBrowserCompat.disconnect();
    }

    private void loadTracks() {
        pagedTrackViewModel.getTracks(mediaBrowserCompat,null).observe(TrackFragment.this, tracks -> {
            //pagedTrackAdapter.submitList(tracks);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void multiSelect(int position) {
        Track track = trackAdapter.getTrackList().get(position);
        if (actionMode != null) {
            if (selectedTrackIds.contains(String.valueOf(track.getAudioId())))
                selectedTrackIds.remove(String.valueOf(track.getAudioId()));
            else
                selectedTrackIds.add(String.valueOf(track.getAudioId()));

            if (selectedTrackIds.size() > 0)
                actionMode.setTitle(String.valueOf(selectedTrackIds.size())+" selected");
            else {
                actionMode.setTitle("");
                actionMode.finish();
            }
            trackAdapter.setSelectedTrackIds(selectedTrackIds);
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_add_to_queue, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_add:
                SelectPlayListDialog dialog = new SelectPlayListDialog();
                dialog.setTargetFragment(this, 0);
                FragmentTransaction fragmentTransaction = null;
                if (getFragmentManager() != null) {
                    fragmentTransaction = getFragmentManager().beginTransaction();
                }
                if (fragmentTransaction != null) {
                    dialog.show(fragmentTransaction,Constants.PLAYLIST_DIALOG);
                }
                break;
            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                        .setTitle("Delete song(s) from storage?")
                        .setCancelable(true)
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("Yes", (dialog1, which) -> {
                            new DeleteFromStorageAsyncTask(getContext(),selectedTrackIds).execute();
                            dialog1.cancel();
                            actionMode.finish();
                        })
                        .setNegativeButton("No", (dialog12, which) -> {
                            Toast.makeText(getContext(),"Cancelled",Toast.LENGTH_SHORT).show();
                            dialog12.cancel();
                            actionMode.finish();
                        });
                builder.show();
                break;
            case R.id.menu_queue:
                new Thread(() -> {
                    List<Track> trackList = trackAdapter.getTrackList();
                    ArrayList<Track> selectedList = new ArrayList<>();
                    for(Track track : trackList){
                        if(selectedTrackIds.contains(String.valueOf(track.getAudioId())))
                            selectedList.add(track);
                    }
                    Bundle extras = new Bundle();
                    extras.putParcelableArrayList(Constants.PLAY_QUEUE, selectedList);
                    mediaBrowserCompat.sendCustomAction(Constants.ADD_QUEUE, extras, new MediaBrowserCompat.CustomActionCallback() {
                        @Override
                        public void onResult(String action, Bundle extras, Bundle resultData) {
                            Toast.makeText(getContext(),"Added to queue",Toast.LENGTH_SHORT).show();
                        }
                    });
                    Objects.requireNonNull(getActivity()).runOnUiThread(() ->
                            actionMode.finish());
                }).start();
                break;
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        isMultiSelect = false;
        selectedTrackIds = new ArrayList<>();
        trackAdapter.setSelectedTrackIds(new ArrayList<>());
    }

    public interface TracksLoadedListener{
        void onTracksLoaded(LiveData<List<Track>> trackLiveData);
    }

    public interface TrackClickListener{
        void onTrackClicked(List<Track> trackList,int pos);
    }

    private static class DeleteFromStorageAsyncTask extends AsyncTask<Void,Void,Void> {
        private WeakReference<Context> contextRef;
        private List<String> audioIdList;

        DeleteFromStorageAsyncTask(Context context, List<String> audioIdList) {
            this.audioIdList = audioIdList;
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MediaUtils.deleteSongsFromStorage(contextRef.get(),audioIdList);
            return null;
        }
    }
}