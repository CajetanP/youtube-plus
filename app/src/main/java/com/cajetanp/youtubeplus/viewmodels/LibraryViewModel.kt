package com.cajetanp.youtubeplus.viewmodels

import androidx.lifecycle.ViewModel
import com.cajetanp.youtubeplus.utils.FeedItem

class LibraryViewModel : ViewModel() {
//    var filterQuery: String = ""

    private var adapterItems: ArrayList<FeedItem> = ArrayList(emptyList())

    fun getAdapterItems(): ArrayList<FeedItem> {
        return adapterItems
    }

    fun addAdapterItems(items: List<FeedItem>) {
        adapterItems.addAll(items)
    }

    fun clearAdapterItems() {
        adapterItems = ArrayList(emptyList())
    }
}
