package com.github.libretube.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.libretube.R
import com.github.libretube.constants.IntentData
import com.github.libretube.constants.PreferenceKeys
import com.github.libretube.databinding.FragmentLibraryBinding
import com.github.libretube.enums.LibraryTab
import com.github.libretube.extensions.serializable
import com.github.libretube.helpers.NavBarHelper
import com.github.libretube.helpers.PreferenceHelper
import com.github.libretube.ui.adapters.LibraryPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment : Fragment(R.layout.fragment_library) {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentLibraryBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        // hide watch history button if history disabled
        val watchHistoryEnabled =
            PreferenceHelper.getBoolean(PreferenceKeys.WATCH_HISTORY_TOGGLE, true)
        if (!watchHistoryEnabled) {
            binding.watchHistory.isGone = true
        } else {
            binding.watchHistory.setOnClickListener {
                findNavController().navigate(R.id.action_libraryFragment_to_watchHistoryFragment)
            }
        }

        binding.downloads.setOnClickListener {
            findNavController().navigate(R.id.action_libraryFragment_to_downloadsFragment)
        }

        val navBarItems = NavBarHelper.getNavBarItemPreference(requireContext())
        if (navBarItems.any { (itemId, isVisible) -> isVisible && itemId == R.id.downloadsFragment }) {
            binding.downloads.isGone = true
        }

        val initialTab = requireArguments()
            .serializable<LibraryTab>(IntentData.libraryTab) ?: LibraryTab.PLAYLISTS

        binding.libraryPager.adapter = LibraryPagerAdapter(this)
        // keep both tab pages alive so switching tabs doesn't recreate fragments,
        // which caused jank during the tab animation. Data loading itself stays
        // lazy: each page only loads in onResume when it becomes visible.
        binding.libraryPager.offscreenPageLimit = LibraryTab.entries.size
        binding.libraryPager.setCurrentItem(initialTab.ordinal, false)

        TabLayoutMediator(binding.tabLayout, binding.libraryPager) { tab, position ->
            tab.text = when (LibraryTab.entries[position]) {
                LibraryTab.PLAYLISTS -> getString(R.string.playlists)
                LibraryTab.BOOKMARKS -> getString(R.string.bookmarks)
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = LibraryTab.entries.size

    override fun createFragment(position: Int): Fragment {
        return when (LibraryTab.entries[position]) {
            LibraryTab.PLAYLISTS -> PlaylistsFragmentPage()
            LibraryTab.BOOKMARKS -> BookmarksFragmentPage()
        }
    }
}
