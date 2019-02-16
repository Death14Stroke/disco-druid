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
import androidx.core.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.PagedArtistAdapter;
import com.andruid.magic.discodruid.data.Constants;
import com.andruid.magic.discodruid.model.Artist;
import com.andruid.magic.discodruid.services.BackgroundAudioService;
import com.andruid.magic.discodruid.util.RecyclerTouchListener;
import com.andruid.magic.discodruid.viewmodel.PagedArtistViewModel;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArtistFragment extends Fragment{
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout swipeRefreshLayout;
    private MediaBrowserCompat mediaBrowserCompat;
    private PagedArtistAdapter pagedArtistAdapter;
    private PagedArtistViewModel pagedArtistViewModel;

    public ArtistFragment() {}

    public static ArtistFragment newInstance() {
        return new ArtistFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pagedArtistAdapter = new PagedArtistAdapter();
        pagedArtistViewModel = ViewModelProviders.of(this).get(PagedArtistViewModel.class);
        mediaBrowserCompat = new MediaBrowserCompat(getContext(),new ComponentName(Objects.requireNonNull(getActivity()), BackgroundAudioService.class),new MediaBrowserCompat.ConnectionCallback(){
            @Override
            public void onConnected() {
                super.onConnected();
                loadArtists();
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swipeRefreshLayout.setRefreshing(true);
                        loadArtists();
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
        recyclerView.setAdapter(pagedArtistAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()),DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getContext(), R.drawable.line_divider)));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                ArtistTracksDialog dialog = new ArtistTracksDialog();
                Bundle args = new Bundle();
                args.putParcelable(Constants.ARTIST,Objects.requireNonNull(pagedArtistAdapter.getCurrentList()).snapshot().get(position));
                dialog.setArguments(args);
                FragmentTransaction fragmentTransaction = null;
                if (getFragmentManager() != null) {
                    fragmentTransaction = getFragmentManager().beginTransaction();
                }
                if (fragmentTransaction != null) {
                    dialog.show(fragmentTransaction,Constants.ARTIST_DIALOG);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaBrowserCompat.disconnect();
    }

    private void loadArtists(){
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        pagedArtistViewModel.getArtists(mediaBrowserCompat).observe(ArtistFragment.this, new Observer<PagedList<Artist>>() {
                            @Override
                            public void onChanged(@Nullable PagedList<Artist> artists) {
                                pagedArtistAdapter.submitList(artists);
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