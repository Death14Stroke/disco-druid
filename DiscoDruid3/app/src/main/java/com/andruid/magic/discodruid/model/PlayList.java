package com.andruid.magic.discodruid.model;

import android.os.Parcel;
import android.os.Parcelable;

public class PlayList implements Parcelable {
    private String name;
    private long playListId, dateCreated;
    private int songCount;

    public PlayList() {
    }

    private PlayList(Parcel in) {
        name = in.readString();
        playListId = in.readLong();
        dateCreated = in.readLong();
        songCount = in.readInt();
    }

    public static final Creator<PlayList> CREATOR = new Creator<PlayList>() {
        @Override
        public PlayList createFromParcel(Parcel in) {
            return new PlayList(in);
        }

        @Override
        public PlayList[] newArray(int size) {
            return new PlayList[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getPlayListId() {
        return playListId;
    }

    public void setPlayListId(long playListId) {
        this.playListId = playListId;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeLong(playListId);
        parcel.writeLong(dateCreated);
        parcel.writeInt(songCount);
    }
}