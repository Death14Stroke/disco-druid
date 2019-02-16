package com.andruid.magic.discodruid.fragment;

import android.Manifest;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import android.content.ComponentName;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.PagedAlbumAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Album;
import com.andruid.magic.discodruid.services.BackgroundAudioService;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.andruid.magic.discodruid.viewmodel.PagedAlbumViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumFragment extends Fragment{
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private MediaBrowserCompat mediaBrowserCompat;
    private PagedAlbumAdapter pagedAlbumAdapter;
    private PagedAlbumViewModel pagedAlbumViewModel;

    public AlbumFragment() {}

    public static AlbumFragment newInstance() {
        return new AlbumFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pagedAlbumAdapter = new PagedAlbumAdapter(getContext());
        pagedAlbumViewModel = ViewModelProviders.of(this).get(PagedAlbumViewModel.class);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(),new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),
                new MediaBrowserCompat.ConnectionCallback(){
                    @Override
                    public void onConnected() {
                        super.onConnected();
                        loadAlbums();
                        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                swipeRefreshLayout.setRefreshing(true);
                                loadAlbums();
                            }
                        });
                    }
                },null);
        mediaBrowserCompat.connect();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout, container, false);
        ButterKnife.bind(this,view);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        recyclerView.setAdapter(pagedAlbumAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),2));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                AlbumTracksDialog dialog = new AlbumTracksDialog();
                Bundle args = new Bundle();
                args.putParcelable(Constants.ALBUM,Objects.requireNonNull(pagedAlbumAdapter.getCurrentList()).snapshot().get(position));
                dialog.setArguments(args);
                FragmentTransaction fragmentTransaction = null;
                if (getFragmentManager() != null) {
                    fragmentTransaction = getFragmentManager().beginTransaction();
                }
                if (fragmentTransaction != null) {
                    dialog.show(fragmentTransaction,Constants.ALBUM_DIALOG);
                }
            }

            @Override
            public void onLongClick(View view, int position) {}
        }));
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
    }

    private void loadAlbums(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        pagedAlbumViewModel.getAlbums(mediaBrowserCompat).observe(AlbumFragment.this, new Observer<PagedList<Album>>() {
                            @Override
                            public void onChanged(@Nullable PagedList<Album> albums) {
                                pagedAlbumAdapter.submitList(albums);
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
}