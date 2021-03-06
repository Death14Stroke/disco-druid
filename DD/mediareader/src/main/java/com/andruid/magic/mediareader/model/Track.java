package com.andruid.magic.mediareader.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class Track implements Parcelable{
    private String path,title, artist, album, albumId, albumArtUri;
    private long audioId, duration;

    public Track() {
    }

    protected Track(Parcel in) {
        path = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        albumId = in.readString();
        albumArtUri = in.readString();
        audioId = in.readLong();
        duration = in.readLong();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Track track = (Track) obj;
        return path.equalsIgnoreCase(track.path) && title.equalsIgnoreCase(track.title)
                && artist.equalsIgnoreCase(track.artist) && album.equalsIgnoreCase(track.album)
                && albumId.equalsIgnoreCase(track.albumId) && albumArtUri.equalsIgnoreCase(track.albumArtUri)
                && audioId == track.audioId && duration == track.duration;
    }

    public static final Creator<Track> CREATOR = new Creator<Track>() {
        @Override
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        @Override
        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbumArtUri() {
        return albumArtUri;
    }

    public void setAlbumArtUri(String albumArtUri) {
        this.albumArtUri = albumArtUri;
    }

    public long getAudioId() {
        return audioId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeString(albumId);
        dest.writeString(albumArtUri);
        dest.writeLong(audioId);
        dest.writeLong(duration);
    }
}