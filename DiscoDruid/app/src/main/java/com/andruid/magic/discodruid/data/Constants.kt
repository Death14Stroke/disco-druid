package com.andruid.magic.discodruid.data

const val PAGE_SIZE = 10

const val ACTION_PREPARE_QUEUE = "com.andruid.magic.discodruid.PREPARE_QUEUE"
const val ACTION_GET_INSTANCE = "com.andruid.magic.discodruid.GET_INSTANCE"

const val EXTRA_TRACK = "track"
const val EXTRA_TRACK_MODE = "track_mode"
const val EXTRA_ALBUM = "album"
const val EXTRA_ALBUM_ID = "album_id"
const val EXTRA_ARTIST = "artist"

const val MODE_ALL_TRACKS = "all_tracks"
const val MODE_ALBUM_TRACKS = "album_tracks"

const val VIEW_TYPE_ALL_TRACKS = 0
const val VIEW_TYPE_ALBUM_TRACKS = 1

const val LOAD_ALBUM = "load_album"
const val LOAD_ARTIST = "load_artist"
const val LOAD_TRACK = "load_track"