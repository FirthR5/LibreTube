package com.github.libretube.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.github.libretube.R
import com.github.libretube.databinding.FragmentLibraryBookmarksBinding
import com.github.libretube.db.obj.PlaylistBookmark
import com.github.libretube.ui.adapters.PlaylistBookmarkAdapter
import com.github.libretube.ui.base.DynamicLayoutManagerFragment
import com.github.libretube.ui.models.LibraryViewModel

class BookmarksFragmentPage :
    DynamicLayoutManagerFragment(R.layout.fragment_library_bookmarks) {
    private var _binding: FragmentLibraryBookmarksBinding? = null
    private val binding get() = _binding!!

    private val libraryViewModel: LibraryViewModel by activityViewModels()

    private val bookmarkAdapter = PlaylistBookmarkAdapter()

    override fun setLayoutManagers(gridItems: Int) {
        _binding?.bookmarksRecView?.layoutManager = GridLayoutManager(context, gridItems)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLibraryBookmarksBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        bookmarkAdapter.onBookmarkRemoved = { bookmark ->
            libraryViewModel.removeBookmark(bookmark.playlistId)
        }
        binding.bookmarksRecView.adapter = bookmarkAdapter

        libraryViewModel.bookmarks.observe(viewLifecycleOwner) { bookmarks ->
            if (bookmarks != null) showBookmarks(bookmarks)
        }
        libraryViewModel.isBookmarksLoading.observe(viewLifecycleOwner) { isLoading ->
            val showProgress = isLoading && libraryViewModel.bookmarks.value == null
            binding.progress.isVisible = showProgress
            binding.bookmarksScrollView.alpha = if (showProgress) 0.3f else 1.0f
            if (!isLoading) binding.bookmarksRefresh.isRefreshing = false
        }
        libraryViewModel.bookmarksError.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            context?.let {
                Toast.makeText(
                    it,
                    if (message.isEmpty()) getString(R.string.unknown_error) else message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            libraryViewModel.bookmarksError.value = null
        }

        binding.bookmarksRefresh.isEnabled = true
        binding.bookmarksRefresh.setOnRefreshListener {
            libraryViewModel.loadBookmarks(force = true)
        }
    }

    override fun onResume() {
        super.onResume()
        // only the visible tab loads; the viewmodel caches the result so
        // revisiting the tab doesn't refetch
        libraryViewModel.loadBookmarks()
    }

    override fun onDestroyView() {
        bookmarkAdapter.onBookmarkRemoved = null
        super.onDestroyView()
        _binding = null
    }

    private fun showBookmarks(bookmarks: List<PlaylistBookmark>) {
        val binding = _binding ?: return

        binding.bookmarksRefresh.isRefreshing = false
        binding.bookmarksCount.text = bookmarks.size.toString()
        binding.nothingHere.isVisible = bookmarks.isEmpty()
        bookmarkAdapter.submitList(bookmarks)
    }
}
