package com.github.libretube.ui.models

import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.libretube.api.PlaylistsHelper
import com.github.libretube.api.obj.Playlists
import com.github.libretube.db.obj.PlaylistBookmark
import com.github.libretube.extensions.TAG
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LibraryViewModel : ViewModel() {
    val playlists = MutableLiveData<List<Playlists>?>(null)
    val bookmarks = MutableLiveData<List<PlaylistBookmark>?>(null)
    val isPlaylistsLoading = MutableLiveData(false)
    val isBookmarksLoading = MutableLiveData(false)
    val playlistsError = MutableLiveData<String?>(null)
    val bookmarksError = MutableLiveData<String?>(null)

    private var playlistsJob: Job? = null
    private var bookmarksJob: Job? = null

    private fun setLoading(liveData: MutableLiveData<Boolean>, loading: Boolean) {
        if (Looper.myLooper() == Looper.getMainLooper()) liveData.value = loading
        else liveData.postValue(loading)
    }

    fun loadPlaylists(force: Boolean = false) {
        if (!force && (playlists.value != null || playlistsJob?.isActive == true)) return

        playlistsJob?.cancel()
        setLoading(isPlaylistsLoading, true)
        playlistsJob = viewModelScope.launch {
            val result = try {
                PlaylistsHelper.getPlaylists()
            } catch (e: Exception) {
                Log.e(TAG(), e.toString())
                playlistsError.postValue("")
                setLoading(isPlaylistsLoading, false)
                return@launch
            }

            playlists.postValue(result)
            setLoading(isPlaylistsLoading, false)
        }
    }

    fun loadBookmarks(force: Boolean = false) {
        if (!force && (bookmarks.value != null || bookmarksJob?.isActive == true)) return

        bookmarksJob?.cancel()
        setLoading(isBookmarksLoading, true)
        bookmarksJob = viewModelScope.launch {
            val result = try {
                PlaylistsHelper.getPlaylistBookmarks()
            } catch (e: Exception) {
                Log.e(TAG(), e.toString())
                bookmarksError.postValue(e.message ?: "")
                setLoading(isBookmarksLoading, false)
                return@launch
            }

            bookmarks.postValue(result)
            setLoading(isBookmarksLoading, false)
        }
    }

    fun setPlaylists(newPlaylists: List<Playlists>) {
        playlists.value = newPlaylists
    }

    fun refreshPlaylists() = loadPlaylists(force = true)

    fun removePlaylist(playlistId: String) {
        playlists.value = playlists.value?.filterNot { it.id == playlistId }
    }

    fun removeBookmark(playlistId: String) {
        bookmarks.value = bookmarks.value?.filterNot { it.playlistId == playlistId }
    }
}
