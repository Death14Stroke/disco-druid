package com.andruid.magic.discodruid.fragment;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.GeneralAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.model.Track;
import com.andruid.magic.discodruid.services.BackgroundAudioService;
import com.andruid.magic.discodruid.util.MediaUtils;
import com.andruid.magic.discodruid.util.ReadContent;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.thesurix.gesturerecycler.DefaultItemClickListener;
import com.thesurix.gesturerecycler.GestureAdapter;
import com.thesurix.gesturerecycler.GestureManager;
import com.thesurix.gesturerecycler.RecyclerItemTouchListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaylistTracksDialog extends DialogFragment{
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.full_screen_rv) RecyclerView recyclerView;
    private GeneralAdapter generalAdapter;
    private PlayList playList;
    private PlaylistDialogClickedListener mListener;
    private MediaBrowserCompat mediaBrowserCompat;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        Bundle args = getArguments();
        if (args != null)
            playList = args.getParcelable(Constants.ARG_PLAYLIST);
        generalAdapter = new GeneralAdapter(getContext());
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
                                        if (track != null)
                                            generalAdapter.setPlayingTrackId(track.getAudioId());
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.fullscreen_dialog,container,false);
        ButterKnife.bind(this,view);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(playList.getName());
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorUnselectedTab));
        toolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        recyclerView.setAdapter(generalAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()),DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        GestureManager gestureManager = new GestureManager.Builder(recyclerView)
                .setLongPressDragEnabled(true)
                .setSwipeEnabled(true)
                .setSwipeFlags(ItemTouchHelper.START | ItemTouchHelper.END)
                .setDragFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN)
                .build();
        gestureManager.setManualDragEnabled(true);
        generalAdapter.setDataChangeListener(new GestureAdapter.OnDataChangeListener<Track>() {
            @Override
            public void onItemRemoved(Track track, int i) {
                List<String> audioIdList = new ArrayList<>();
                audioIdList.add(String.valueOf(track.getAudioId()));
                new DeleteTracksFromPlayListAsyncTask(getContext(),audioIdList,playList.getPlayListId()).execute();
            }

            @Override
            public void onItemReorder(Track track, int from, int to) {
                new ReOrderAsyncTask(getContext(),playList.getPlayListId(),from,to).execute();
            }
        });
        recyclerView.addOnItemTouchListener(new RecyclerItemTouchListener<>(new DefaultItemClickListener<Track>(){
            @Override
            public boolean onItemClick(Track item, int position) {
                Bundle params = new Bundle();
                params.putLong(Constants.ARG_PLAYLIST,playList.getPlayListId());
                mListener.onPlaylistDialogClicked(generalAdapter.getData(),position,params);
                return true;
            }
        }));
        return view;
    }

    private void loadTracks() {
        final Bundle options = new Bundle();
        options.putLong(Constants.PLAYLIST_ID,playList.getPlayListId());
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        mediaBrowserCompat.subscribe(Constants.PLAYLIST_TRACK, options, new MediaBrowserCompat.SubscriptionCallback() {
                            @Override
                            public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children, @NonNull Bundle options) {
                                super.onChildrenLoaded(parentId, children, options);
                                List<Track> trackList = MediaUtils.getTracksFromMediaItems(children);
                                generalAdapter.setData(trackList);
                                generalAdapter.notifyDataSetChanged();
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

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlaylistDialogClickedListener) {
            mListener = (PlaylistDialogClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlaylistDialogClickedListener");
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
        mediaBrowserCompat.unsubscribe(Constants.PLAY_QUEUE);
        mediaBrowserCompat.unsubscribe(Constants.CURRENT_TRACK);
        mediaBrowserCompat.disconnect();
    }

    public interface PlaylistDialogClickedListener {
        void onPlaylistDialogClicked(List<Track> trackList, int pos, Bundle params);
    }

    public static class DeleteTracksFromPlayListAsyncTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<Context> contextRef;
        private List<String> audioIdList;
        private long playListId;

        DeleteTracksFromPlayListAsyncTask(Context context, List<String> audioIdList, long playListId) {
            this.contextRef = new WeakReference<>(context);
            this.audioIdList = audioIdList;
            this.playListId = playListId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReadContent.deleteSongsFromPlayList(contextRef.get(),audioIdList,playListId);
            return null;
        }
    }

    public static class ReOrderAsyncTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<Context> contextRef;
        private long playListId;
        private int from, to;

        ReOrderAsyncTask(Context context, long playListId, int from, int to) {
            this.contextRef = new WeakReference<>(context);
            this.playListId = playListId;
            this.from = from;
            this.to = to;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReadContent.moveSongsInPlayList(contextRef.get(),playListId,from,to);
            return null;
        }
    }
}