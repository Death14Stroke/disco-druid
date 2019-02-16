package com.andruid.magic.discodruid.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Toast;

import com.andruid.magic.discodruid.R;
import com.andruid.magic.discodruid.adapter.SpinnerAdapter;
import com.andruid.magic.discodruid.model.PlayList;
import com.andruid.magic.discodruid.provider.PlaylistProvider;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SelectPlayListDialog extends DialogFragment {
    private SpinnerAdapter spinnerAdapter;
    private PlayList selectedPlaylist = null;
    private PlayListDialogListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getTargetFragment() instanceof PlayListDialogListener) {
            mListener = (PlayListDialogListener) getTargetFragment();
        } else {
            if (getTargetFragment() != null) {
                throw new RuntimeException(getTargetFragment().toString()
                        + " must implement PlayListDialogListener");
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.dialog_playlist)
                .setTitle("Add to playlist")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onPlayListSelected(selectedPlaylist);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onDialogCancelled();
                        Toast.makeText(getContext(),"Cancelled",Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });
        Dialog dialog = builder.show();
        Spinner spinner = dialog.findViewById(R.id.playlist_spinner);
        spinnerAdapter = new SpinnerAdapter(new ArrayList<PlayList>(), getContext());
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPlaylist = spinnerAdapter.getPlayLists().get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        new PlayListAsyncTask(getContext(), new PlayListAsyncTask.PlayListLoadedListener() {
            @Override
            public void onPlayListLoaded(List<PlayList> playLists) {
                spinnerAdapter.setPlayLists(playLists);
            }
        }).execute();
    }

    public interface PlayListDialogListener{
        void onPlayListSelected(PlayList playList);
        void onDialogCancelled();
    }

    public static class PlayListAsyncTask extends AsyncTask<Void,Void,List<PlayList>> {
        private WeakReference<Context> contextRef;
        private PlayListLoadedListener mListener;

        PlayListAsyncTask(Context context, PlayListLoadedListener mListener) {
            this.contextRef = new WeakReference<>(context);
            this.mListener = mListener;
        }

        public interface PlayListLoadedListener{
            void onPlayListLoaded(List<PlayList> playLists);
        }

        @Override
        protected List<PlayList> doInBackground(Void... voids) {
            PlaylistProvider playlistProvider = new PlaylistProvider(contextRef.get());
            return playlistProvider.getAllPlaylist();
        }

        @Override
        protected void onPostExecute(List<PlayList> playLists) {
            super.onPostExecute(playLists);
            mListener.onPlayListLoaded(playLists);
        }
    }
}