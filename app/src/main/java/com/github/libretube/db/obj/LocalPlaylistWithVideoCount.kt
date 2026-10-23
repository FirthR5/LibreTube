package com.github.libretube.db.obj

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class LocalPlaylistWithVideoCount(
    @Embedded val playlist: LocalPlaylist,
    @ColumnInfo(name = "videoCount") val videoCount: Int
)
