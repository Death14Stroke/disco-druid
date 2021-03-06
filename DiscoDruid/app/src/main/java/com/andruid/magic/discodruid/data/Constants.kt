package com.andruid.magic.discodruid.data

const val PAGE_SIZE = 10

const val ACTION_GET_INSTANCE = "com.andruid.magic.discodruid.GET_INSTANCE"
const val ACTION_SELECT_TRACK = "com.andruid.magic.discodruid.SELECT_TRACK"

const val EXTRA_TRACK = "track"
const val EXTRA_TRACK_MODE = "track_mode"
const val EXTRA_ALBUM = "album"
const val EXTRA_ALBUM_ID = "album_id"
const val EXTRA_ARTIST = "artist"
const val EXTRA_ARTIST_ID = "artist_id"
const val EXTRA_PLAYLIST = "playlist"
const val EXTRA_PLAYLIST_ID = "playlist_id"
const val EXTRA_FROM_POS = "from_position"
const val EXTRA_TO_POS = "to_position"

const val MODE_ALL_TRACKS = 0
const val MODE_ALBUM_TRACKS = 1
const val MODE_ARTIST_TRACKS = 2
const val MODE_PLAYLIST_TRACKS = 3

const val VIEW_TYPE_ALL_TRACKS = 0
const val VIEW_TYPE_ALBUM_TRACKS = 1
const val VIEW_TYPE_ARTIST_TRACK = 0
const val VIEW_TYPE_ARTIST_ALBUM = 1

const val MB_LOAD_ALBUM = "load_album"
const val MB_LOAD_ARTIST = "load_artist"
const val MB_LOAD_TRACK = "load_track"
const val MB_LOAD_PLAYLIST = "load_playlist"
const val MB_PLAY_QUEUE = "play_queue"
const val MB_CURRENT_TRACK = "current_track"

const val CMD_PREPARE_QUEUE = "com.andruid.magic.discodruid.PREPARE_QUEUE"
const val CMD_REORDER_QUEUE = "com.andruid.magic.discodruid.REORDER_QUEUE"
const val CMD_DELETE_FROM_QUEUE = "com.andruid.magic.discodruid.DELETE_FROM_QUEUE"

const val PLAYLIST_MY_QUEUE = "My Droid"