package com.andruid.magic.mediareader.util;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;

import androidx.paging.PositionalDataSource;

public class PagingUtils {

    public static Bundle getRangeBundle(PositionalDataSource.LoadRangeParams params) {
        Bundle extra = new Bundle();
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE, getPageIndex(params));
        extra.putInt(MediaBrowserCompat.EXTRA_PAGE_SIZE,params.loadSize);
        return extra;
    }

    public static int getPageIndex(PositionalDataSource.LoadRangeParams params){
        return params.startPosition/params.loadSize;
    }
}
