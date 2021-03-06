package com.cajetanp.youtubeplus.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cajetanp.youtubeplus.R
import com.cajetanp.youtubeplus.adapters.ContentListAdapter
import com.cajetanp.youtubeplus.data.VideoData
import com.cajetanp.youtubeplus.data.MainDataViewModel
import com.cajetanp.youtubeplus.utils.FeedItem
import com.cajetanp.youtubeplus.utils.ItemType
import com.cajetanp.youtubeplus.utils.YouTubeData
import com.google.api.services.youtube.model.PlaylistItem
import com.google.api.services.youtube.model.Video
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.yesButton
import java.lang.ClassCastException

class PlaylistContentFragment : Fragment(), ContentListAdapter.ListItemClickListener,
        YouTubeData.PlaylistDataListener, YouTubeData.VideoListDataListener {

    private val TAG: String = this.javaClass.simpleName

    private lateinit var mListener: InteractionListener

    private lateinit var mAdapter: ContentListAdapter
    private lateinit var mYouTubeData: YouTubeData
    private lateinit var mMainDataViewModel: MainDataViewModel

    private lateinit var videoList: RecyclerView
    private lateinit var searchProgressBarCentre: ProgressBar
    private lateinit var searchProgressBarBottom: ProgressBar

    private var mPlaylistId: String = ""
    private var mPreviousPageToken: String = ""
    private var mNextPageToken: String = ""

    ////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ////////////////////////////////////////////////////////////////////////////////

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainDataViewModel = ViewModelProviders.of(this).get(MainDataViewModel::class.java)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAdapter  = ContentListAdapter(emptyList(), this, activity!!)
        mYouTubeData = YouTubeData(activity!!, this)

        setupSearchResultList()
        mPlaylistId = arguments?.getString(getString(R.string.playlist_id_key))!!
        loadPlaylistVideos()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (activity is InteractionListener)
            mListener = activity as InteractionListener
        else
            throw ClassCastException("Activity must implement InteractionListener")

        if (this::mListener.isInitialized)
            mListener.onChannelTitle(arguments?.getString(getString(R.string.channel_title_key))!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_start, container, false)

        videoList = view.findViewById(R.id.videoList)
        searchProgressBarCentre = view.findViewById(R.id.progressBarCentre)
        searchProgressBarBottom = view.findViewById(R.id.progressBarBottom)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        mYouTubeData.onParentActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Init
    ////////////////////////////////////////////////////////////////////////////////

    private fun setupSearchResultList() {
        videoList.layoutManager = LinearLayoutManager(activity!!)

        mAdapter.onBottomReached = {
            loadPlaylistVideos(mNextPageToken)
        }

        videoList.setHasFixedSize(false)
        videoList.adapter = mAdapter
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Utils
    ////////////////////////////////////////////////////////////////////////////////

    private fun loadPlaylistVideos(nextPageToken: String = "") {
        if (nextPageToken.isEmpty()) {
            searchProgressBarCentre.visibility = View.VISIBLE
            videoList.visibility = View.INVISIBLE
        } else {
            searchProgressBarBottom.visibility = View.VISIBLE
        }

        mYouTubeData.receivePlaylistResults(mPlaylistId, nextPageToken)
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    ////////////////////////////////////////////////////////////////////////////////

    override fun onPlaylistDataReceived(results: List<PlaylistItem>,
                                        nextPageToken: String, previousPageToken: String) {
        val playlistItems: List<VideoData> = results.map { VideoData(it.contentDetails.videoId) }

        mPreviousPageToken = previousPageToken
        mNextPageToken = nextPageToken
        mYouTubeData.receiveVideoListResults(playlistItems)
    }

    override fun onVideoListReceived(results: List<Video>, block: ((List<Video>) -> List<Video>)?) {
        if (mPreviousPageToken.isEmpty()) {
            searchProgressBarCentre.visibility = View.INVISIBLE
            videoList.visibility = View.VISIBLE
        } else {
            searchProgressBarBottom.visibility = View.GONE
        }

        mAdapter.addItems(results.map { FeedItem(it.id, video = it) })
    }

    override fun onListItemClick(id: String, position: Int, type: ItemType) {
        when (type) {
            ItemType.Video -> findNavController().navigate(R.id.action_playlistContent_to_playerActivity,
                    bundleOf(getString(R.string.video_id_key) to id))
            else -> {}
        }
    }

    override fun onListItemLongClick(id: String, type: ItemType) {
        when (type) {
            ItemType.Video ->
                activity!!.alert(getString(R.string.favourite_add_confirmation)) {
                    yesButton { mMainDataViewModel.insertFavourite(VideoData(id)) }
                    noButton { }
                }.show()
            else -> {}
        }
    }

    interface InteractionListener {
        fun onChannelTitle(title: String)
    }
}


