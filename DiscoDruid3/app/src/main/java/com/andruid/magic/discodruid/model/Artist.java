package com.andruid.magic.discodruid.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class Artist implements Parcelable {
    private String artistId, artist;
    private int tracksCount, albumsCount;

    public Artist() {
    }

    protected Artist(Parcel in) {
        artistId = in.readString();
        artist = in.readString();
        tracksCount = in.readInt();
        albumsCount = in.readInt();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        Artist artist = (Artist) obj;
        return artistId.equalsIgnoreCase(artist.artistId) && this.artist.equalsIgnoreCase(artist.artist) &&
                tracksCount==artist.tracksCount && albumsCount==artist.albumsCount;
    }

    public static final Creator<Artist> CREATOR = new Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel in) {
            return new Artist(in);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getTracksCount() {
        return tracksCount;
    }

    public void setTracksCount(int tracksCount) {
        this.tracksCount = tracksCount;
    }

    public int getAlbumsCount() {
        return albumsCount;
    }

    public void setAlbumsCount(int albumsCount) {
        this.albumsCount = albumsCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(artistId);
        parcel.writeString(artist);
        parcel.writeInt(tracksCount);
        parcel.writeInt(albumsCount);
    }
}