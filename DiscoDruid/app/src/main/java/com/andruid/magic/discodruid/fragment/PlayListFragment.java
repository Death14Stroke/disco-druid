package com.andruid.magic.discodruid.fragment;

import android.Manifest;
import android.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.PagedPlaylistAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.services.BackgroundAudioService;
import com.andruid.magic.discodruid.util.ReadContent;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.andruid.magic.discodruid.viewmodel.PagedPlaylistViewModel;
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

public class PlayListFragment extends Fragment implements ActionMode.Callback {
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private MediaBrowserCompat mediaBrowserCompat;
    private PagedPlaylistAdapter  pagedPlaylistAdapter;
    private PagedPlaylistViewModel pagedPlaylistViewModel;
    private ActionMode actionMode;
    private boolean isMultiSelect = false;
    private List<String> selectedPlayListIds = new ArrayList<>();

    public PlayListFragment() {}

    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pagedPlaylistAdapter = new PagedPlaylistAdapter(getContext());
        pagedPlaylistViewModel = ViewModelProviders.of(this).get(PagedPlaylistViewModel.class);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(),new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),
                new MediaBrowserCompat.ConnectionCallback(){
                    @Override
                    public void onConnected() {
                        super.onConnected();
                        loadPlayLists();
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                swipeRefreshLayout.setRefreshing(false);
                                loadPlayLists();
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
        recyclerView.setAdapter(pagedPlaylistAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (isMultiSelect)
                    multiSelect(position);
                else {
                    PlaylistTracksDialog dialog = new PlaylistTracksDialog();
                    Bundle args = new Bundle();
                    args.putString(Constants.DIALOG_MODE,Constants.PLAYLIST_DIALOG);
                    args.putParcelable(Constants.ARG_PLAYLIST,Objects.requireNonNull(pagedPlaylistAdapter.getCurrentList()).snapshot().get(position));
                    dialog.setArguments(args);
                    FragmentTransaction fragmentTransaction = null;
                    if (getFragmentManager() != null) {
                        fragmentTransaction = getFragmentManager().beginTransaction();
                    }
                    if (fragmentTransaction != null) {
                        dialog.show(fragmentTransaction,Constants.PLAYLIST_DIALOG);
                    }
                }
            }

            @Override
            public void onLongClick(View view, int position) {
                if (!isMultiSelect){
                    selectedPlayListIds = new ArrayList<>();
                    isMultiSelect = true;
                    if (actionMode == null){
                        actionMode = Objects.requireNonNull(getActivity()).startActionMode(PlayListFragment.this);
                    }
                }
                multiSelect(position);
            }
        }));
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
    }

    private void multiSelect(int position) {
        PlayList playList = Objects.requireNonNull(pagedPlaylistAdapter.getCurrentList()).snapshot().get(position);
        if (playList != null) {
            if (actionMode != null) {
                if (selectedPlayListIds.contains(String.valueOf(playList.getPlayListId())))
                    selectedPlayListIds.remove(String.valueOf(playList.getPlayListId()));
                else
                    selectedPlayListIds.add(String.valueOf(playList.getPlayListId()));
                if (selectedPlayListIds.size() > 0)
                    actionMode.setTitle(String.valueOf(selectedPlayListIds.size()));
                else {
                    actionMode.setTitle("");
                    actionMode.finish();
                }
                pagedPlaylistAdapter.setSelectedPlayListIds(selectedPlayListIds);
            }
        }
    }

    private void loadPlayLists(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        pagedPlaylistViewModel.getPlayLists(mediaBrowserCompat).observe(PlayListFragment.this, new Observer<PagedList<PlayList>>() {
                            @Override
                            public void onChanged(@Nullable PagedList<PlayList> playLists) {
                                pagedPlaylistAdapter.submitList(playLists);
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
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.menu_delete_only, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                        .setTitle("Delete playlist(s)?")
                        .setCancelable(true)
                        .setIcon(R.mipmap.ic_launcher)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new DeletePlayListAsyncTask(getContext(),selectedPlayListIds).execute();
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
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        this.actionMode = null;
        isMultiSelect = false;
        selectedPlayListIds = new ArrayList<>();
        pagedPlaylistAdapter.setSelectedPlayListIds(new ArrayList<String>());
    }

    public static class DeletePlayListAsyncTask extends AsyncTask<Void,Void,Void>{
        private WeakReference<Context> contextRef;
        private List<String> playListIds;

        DeletePlayListAsyncTask(Context context, List<String> playListIds) {
            this.contextRef = new WeakReference<>(context);
            this.playListIds = playListIds;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ReadContent.deletePlayList(contextRef.get(),playListIds);
            return null;
        }
    }
}