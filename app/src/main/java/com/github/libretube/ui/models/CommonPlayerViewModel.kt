package com.github.libretube.ui.models

import androidx.core.graphics.Insets
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.github.libretube.extensions.updateIfChanged

class CommonPlayerViewModel : ViewModel() {
    val isMiniPlayerVisible = MutableLiveData(false)
    val isFullscreen = MutableLiveData(false)
    var maxSheetHeightPx = 0

    /**
     * Current system bar insets of the activity window, published by MainActivity.
     */
    val systemBarInsets = MutableLiveData<Insets>()

    val sheetExpand = MutableLiveData<Boolean?>()

    fun setSheetExpand(state: Boolean?) {
        sheetExpand.updateIfChanged(state)
    }
}
