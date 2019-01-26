package com.andruid.magic.discodruid.fragment;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ActionMode;
import android.widget.Toast;

import com.andruid.magic.discodruid.viewmodel.PagedTrackViewModel;
import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.PagedTrackAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.services.BackgroundAudioService;
import com.andruid.magic.discodruid.util.ReadContent;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TrackFragment extends Fragment implements SelectPlayListDialog.PlayListDialogListener, ActionMode.Callback {

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private MediaBrowserCompat mediaBrowserCompat;
    private PagedTrackAdapter pagedTrackAdapter;
    private TrackClickListener mListener;
    private ActionMode actionMode;
    private boolean isMultiSelect = false;
    private List<String> selectedTrackIds = new ArrayList<>();
    private PagedTrackViewModel pagedTrackViewModel;

    public TrackFragment() {}

    public static TrackFragment newInstance() {
        return new TrackFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pagedTrackAdapter = new PagedTrackAdapter(getContext(),Constants.VIEW_TRACKS);
        pagedTrackViewModel = ViewModelProviders.of(this).get(PagedTrackViewModel.class);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(),new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),
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
                                            pagedTrackAdapter.setPlayingTrackId(track.getAudioId());
                                        }
                                    }
                                }
                            }
                        });
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                swipeRefreshLayout.setRefreshing(true);
                                loadTracks();
                            }
                        });
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
        recyclerView.setAdapter(pagedTrackAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()),DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (isMultiSelect)
                    multiSelect(position);
                else {
                    pagedTrackAdapter.reset();
                    mListener.onTrackClicked(Objects.requireNonNull(Objects.requireNonNull(pagedTrackAdapter.getCurrentList()).snapshot()), position);
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
    public void onAttach(Context context) {
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

    private void loadTracks(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        pagedTrackViewModel.getTracks(mediaBrowserCompat,null).observe(TrackFragment.this, new Observer<PagedList<Track>>() {
                            @Override
                            public void onChanged(@Nullable PagedList<Track> tracks) {
                                Log.d("adapterlogold",tracks.snapshot().size()+"");
                                pagedTrackAdapter.submitList(tracks);
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(getContext(),response.getPermissionName()+" permission denied",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    private void multiSelect(int position) {
        Track track = Objects.requireNonNull(pagedTrackAdapter.getCurrentList()).snapshot().get(position);
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
            pagedTrackAdapter.setSelectedTrackIds(selectedTrackIds);
        }
    }

    @Override
    public void onPlayListSelected(PlayList playList) {
        new AddSongsToPlayListAsyncTask(getContext(),selectedTrackIds,playList.getPlayListId()).execute();
        actionMode.finish();
    }

    @Override
    public void onDialogCancelled(){
        actionMode.finish();
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
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeleteFromStorageAsyncTask(getContext(),selectedTrackIds).execute();
                                dialog.cancel();
                                actionMode.finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Cancelled",Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                                actionMode.finish();
                            }
                        });
                builder.show();
                break;
            case R.id.menu_queue:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        List<Track> trackList = Objects.requireNonNull(pagedTrackAdapter.getCurrentList()).snapshot();
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
                        Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actionMode.finish();
                            }
                        });
                    }
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
        pagedTrackAdapter.setSelectedTrackIds(new ArrayList<String>());
    }

    public interface TrackClickListener{
        void onTrackClicked(List<Track> trackList,int pos);
    }

    public static class AddSongsToPlayListAsyncTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<Context> contextRef;
        private List<String> selectedIdList;
        private long playListId;

        AddSongsToPlayListAsyncTask(Context context, List<String> selectedIdList, long playListId) {
            this.contextRef = new WeakReference<>(context);
            this.selectedIdList = selectedIdList;
            this.playListId = playListId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReadContent.addTracksToPlayList(contextRef.get(),playListId,selectedIdList);
            return null;
        }
    }

    public static class DeleteFromStorageAsyncTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<Context> contextRef;
        private List<String> audioIdList;

        public DeleteFromStorageAsyncTask(Context context,List<String> audioIdList) {
            this.audioIdList = audioIdList;
            contextRef = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReadContent.deleteSongsFromStorage(contextRef.get(),audioIdList);
            return null;
        }
    }
}