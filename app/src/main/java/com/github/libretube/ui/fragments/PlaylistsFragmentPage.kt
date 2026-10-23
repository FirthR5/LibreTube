package com.github.libretube.ui.fragments

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.libretube.R
import com.github.libretube.api.obj.Playlists
import com.github.libretube.constants.IntentData
import com.github.libretube.constants.PreferenceKeys
import com.github.libretube.databinding.FragmentLibraryPlaylistsBinding
import com.github.libretube.enums.PlaylistType
import com.github.libretube.extensions.dpToPx
import com.github.libretube.helpers.PreferenceHelper
import com.github.libretube.ui.adapters.PlaylistsAdapter
import com.github.libretube.ui.base.DynamicLayoutManagerFragment
import com.github.libretube.ui.dialogs.CreatePlaylistDialog
import com.github.libretube.ui.dialogs.CreatePlaylistDialog.Companion.CREATE_PLAYLIST_DIALOG_REQUEST_KEY
import com.github.libretube.ui.models.CommonPlayerViewModel
import com.github.libretube.ui.models.LibraryViewModel
import com.github.libretube.ui.sheets.BaseBottomSheet

class PlaylistsFragmentPage :
    DynamicLayoutManagerFragment(R.layout.fragment_library_playlists) {
    private var _binding: FragmentLibraryPlaylistsBinding? = null
    private val binding get() = _binding!!

    private val libraryViewModel: LibraryViewModel by activityViewModels()
    private val commonPlayerViewModel: CommonPlayerViewModel by activityViewModels()

    private val playlistsAdapter = PlaylistsAdapter(PlaylistType.PRIVATE)

    override fun setLayoutManagers(gridItems: Int) {
        _binding?.playlistRecView?.layoutManager = GridLayoutManager(context, gridItems)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLibraryPlaylistsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        // listen for playlists to become deleted
        playlistsAdapter.registerAdapterDataObserver(object :
            RecyclerView.AdapterDataObserver() {
            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                _binding?.nothingHere?.isVisible = playlistsAdapter.itemCount == 0
                _binding?.sortTV?.isVisible = playlistsAdapter.itemCount > 0
                super.onItemRangeRemoved(positionStart, itemCount)
            }
        })
        playlistsAdapter.onListChanged = { playlists ->
            libraryViewModel.setPlaylists(playlists)
        }
        binding.playlistRecView.adapter = playlistsAdapter

        // listen for the mini player state changing
        commonPlayerViewModel.isMiniPlayerVisible.observe(viewLifecycleOwner) {
            updateFABMargin(it)
        }

        libraryViewModel.playlists.observe(viewLifecycleOwner) { playlists ->
            if (playlists != null) showPlaylists(playlists)
        }
        libraryViewModel.isPlaylistsLoading.observe(viewLifecycleOwner) { isLoading ->
            val showProgress = isLoading && libraryViewModel.playlists.value == null
            binding.progress.isVisible = showProgress
            binding.playlistScrollView.alpha = if (showProgress) 0.3f else 1.0f
            if (!isLoading) binding.playlistRefresh.isRefreshing = false
        }
        libraryViewModel.playlistsError.observe(viewLifecycleOwner) { message ->
            if (message == null) return@observe
            context?.let {
                Toast.makeText(
                    it,
                    if (message.isEmpty()) getString(R.string.unknown_error) else message,
                    Toast.LENGTH_SHORT
                ).show()
            }
            libraryViewModel.playlistsError.value = null
        }

        binding.playlistRefresh.isEnabled = true
        binding.playlistRefresh.setOnRefreshListener {
            libraryViewModel.loadPlaylists(force = true)
        }

        childFragmentManager.setFragmentResultListener(
            CREATE_PLAYLIST_DIALOG_REQUEST_KEY,
            this
        ) { _, resultBundle ->
            val isPlaylistCreated = resultBundle.getBoolean(IntentData.playlistTask)
            if (isPlaylistCreated) {
                libraryViewModel.loadPlaylists(force = true)
            }
        }
        binding.createPlaylist.setOnClickListener {
            CreatePlaylistDialog()
                .show(childFragmentManager, CreatePlaylistDialog::class.java.name)
        }

        val sortOptions = resources.getStringArray(R.array.playlistSortingOptions)
        val sortOptionValues = resources.getStringArray(R.array.playlistSortingOptionsValues)
        val order = PreferenceHelper.getString(
            PreferenceKeys.PLAYLISTS_ORDER,
            sortOptionValues.first()
        )
        val orderIndex = sortOptionValues.indexOf(order)
        binding.sortTV.text = sortOptions.getOrNull(orderIndex)

        binding.sortTV.setOnClickListener {
            BaseBottomSheet().apply {
                setSimpleItems(sortOptions.toList()) { index ->
                    binding.sortTV.text = sortOptions[index]
                    val value = sortOptionValues[index]
                    PreferenceHelper.putString(PreferenceKeys.PLAYLISTS_ORDER, value)
                    libraryViewModel.loadPlaylists(force = true)
                }
            }.show(childFragmentManager)
        }
    }

    override fun onResume() {
        super.onResume()
        // only the visible tab loads; the viewmodel caches the result so
        // revisiting the tab doesn't refetch
        libraryViewModel.loadPlaylists()
    }

    override fun onDestroyView() {
        playlistsAdapter.onListChanged = null
        super.onDestroyView()
        _binding = null
    }

    private fun updateFABMargin(isMiniPlayerVisible: Boolean) {
        // optimize CreatePlaylistFab bottom margin if miniPlayer active
        binding.createPlaylist.updateLayoutParams<MarginLayoutParams> {
            bottomMargin = (if (isMiniPlayerVisible) 64f else 16f).dpToPx()
        }
    }

    private fun showPlaylists(playlists: List<Playlists>) {
        val binding = _binding ?: return

        binding.playlistRefresh.isRefreshing = false
        binding.nothingHere.isVisible = playlists.isEmpty()
        binding.sortTV.isVisible = playlists.isNotEmpty()
        playlistsAdapter.submitList(playlists)
    }
}
