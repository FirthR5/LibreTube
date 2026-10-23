package com.github.libretube.ui.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.libretube.enums.LibraryTab
import com.github.libretube.ui.fragments.BookmarksFragmentPage
import com.github.libretube.ui.fragments.PlaylistsFragmentPage

class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = LibraryTab.entries.size

    override fun createFragment(position: Int): Fragment = when (LibraryTab.entries[position]) {
        LibraryTab.PLAYLISTS -> PlaylistsFragmentPage()
        LibraryTab.BOOKMARKS -> BookmarksFragmentPage()
    }
}
