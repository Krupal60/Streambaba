package com.stream.baba.ui

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.CaptionStyleCompat.EDGE_TYPE_OUTLINE
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.stream.baba.CommonActivity.showToast
import com.stream.baba.R
import com.stream.baba.databinding.CustomControllerBinding
import com.stream.baba.databinding.FragmentPlayerBinding
import com.stream.baba.hideSystemUi
import com.stream.baba.showSystemUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext


@UnstableApi
class PlayerFragment : Fragment() {

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!


    private var _controller: CustomControllerBinding? = null
    private val controller get() = _controller!!
    private var player: ExoPlayer? = null
    private var resizeMode: Int = 13
    private var playWhenReady = true
    private var isItemChanged = false
    private var currentItem = 0
    private var playbackPosition = 0L
    private var playerInitialized = false
    private val playerInitMutex = Mutex()
    private val playbackStateListener: Player.Listener = playbackStateListener()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        setFullscreen()
        _binding!!.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        return binding.root
    }


    private fun setFullscreen() {
        activity?.hideSystemUi()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
    }

    @SuppressLint("SourceLockedOrientationActivity")
    fun setNotFullScreen() {
        activity?.showSystemUi()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        } else {
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        }
    }


    suspend fun initializePlayerIfNeeded() {
        if (!playerInitialized) {
            playerInitMutex.withLock {
                if (!playerInitialized) {
                    player = initializePlayer()
                    playerInitialized = true
                }
            }
        }
    }

    suspend fun releasePlayerIfNeeded() {
        if (playerInitialized) {
            playerInitMutex.withLock {
                if (playerInitialized) {
                    releasePlayer()
                    playerInitialized = false
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _controller = CustomControllerBinding.bind(view)
        val playerResizeBtt = controller.playerResizeBtt
        val playerGoBack = controller.playerGoBack


        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.visibility = View.GONE

        binding.apply {

            playerView.subtitleView?.setBottomPaddingFraction(1f)
            playerView.subtitleView?.setUserDefaultTextSize()
            val captionStyle = CaptionStyleCompat(
                Color.WHITE,
                ContextCompat.getColor(requireContext(), R.color.black_overlay),
                Color.TRANSPARENT,
                EDGE_TYPE_OUTLINE,
                Color.BLACK,
                Typeface.DEFAULT
            )
            playerView.subtitleView?.setStyle(captionStyle)
            playerView.controllerShowTimeoutMs = 3000


            playerResizeBtt.setOnClickListener {
                nextResize()
            }
            playerGoBack.setOnClickListener {
                findNavController().popBackStack()
            }


        }
    }

    fun releasePlayer() {
        player?.let { exoPlayer ->
            playWhenReady = exoPlayer.playWhenReady
            currentItem = exoPlayer.currentMediaItemIndex
            playbackPosition = exoPlayer.currentPosition
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }


    fun initializePlayer(): ExoPlayer {
        val ismovie = arguments?.getBoolean("isMovie")
        val name = arguments?.getString("name")
        val position = arguments?.getInt("position")!!

        val epLinks = arguments?.getStringArrayList("epLinks")
        val epNames = arguments?.getStringArrayList("epNames")

        val trackSelector = DefaultTrackSelector(requireContext()).apply {
            setParameters(
                buildUponParameters().setMaxVideoSizeSd()
                    .setRendererDisabled(C.TRACK_TYPE_VIDEO, false)
                    .setExceedRendererCapabilitiesIfNecessary(true)
            )
        }

        val renderersFactory = DefaultRenderersFactory(requireContext())
        renderersFactory.setEnableDecoderFallback(true)


        val exoPlayer = ExoPlayer.Builder(requireContext())
            .setTrackSelector(trackSelector)
            .setSeekForwardIncrementMs(5000)
            .setSeekBackIncrementMs(5000)
            .setRenderersFactory(renderersFactory)
            .build()

        binding.playerView.player = exoPlayer
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.addListener(playbackStateListener)

        val videoUrl = arguments?.getString("videoUrl")
        lifecycleScope.launch(Dispatchers.Main) {
            when {
                ismovie!! -> {
                    val mimeType = async(Dispatchers.IO) {
                        someFunctionToGetMimeType(videoUrl!!)
                    }.await()
                    val mediaItem = MediaItem.Builder()
                        .setUri(videoUrl)
                        .setMimeType(mimeType ?: "video/*")
                        .build()

                    exoPlayer.setMediaItem(mediaItem)

                    exoPlayer.seekTo(
                        when (isItemChanged) {
                            true -> {
                                currentItem
                            }

                            false -> {
                                position
                            }
                        },
                        playbackPosition
                    )
                    exoPlayer.prepare()
                }

                !ismovie && epLinks != null -> {
                    val mimeTypeMain = async(Dispatchers.IO) {
                        someFunctionToGetMimeType(if (isItemChanged) epLinks[currentItem] else epLinks[position])
                    }.await()
                    for (i in epLinks.indices.iterator()) {
                        when (i) {
                            0 -> {
                                val episodeMediaItem = MediaItem.Builder()
                                    .setUri(epLinks[i])
                                    .setMimeType(mimeTypeMain ?: "video/*")
                                    .build()

                                exoPlayer.setMediaItem(episodeMediaItem)

                                controller.exoMainText.text =
                                    if (ismovie) name else if (isItemChanged) epNames!![currentItem] else epNames!![position]
                                //  binding.contentProgress.visibility = View.GONE

                                exoPlayer.seekTo(
                                    when (isItemChanged) {
                                        true -> {
                                            currentItem
                                        }

                                        false -> {
                                            position
                                        }
                                    },
                                    playbackPosition
                                )
                                exoPlayer.prepare()
                            }

                            else -> {
                                val mimeType = async(Dispatchers.IO) {
                                    someFunctionToGetMimeType(epLinks[i])
                                }.await()
                                val episodeMediaItem = MediaItem.Builder()
                                    .setUri(epLinks[i])
                                    .setMimeType(mimeType ?: "video/*")
                                    .build()
                                exoPlayer.addMediaItem(i, episodeMediaItem)
                            }
                        }

                    }
                }
            }

            controller.exoMainText.text =
                if (ismovie!!) name else if (isItemChanged) epNames!![currentItem] else epNames!![position]


        }
        binding.apply {
            playerView.useController = true
            playerView.keepScreenOn = true
        }
        return exoPlayer

    }


    private suspend fun someFunctionToGetMimeType(uri: String): String? {
        return withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(uri)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                retriever.release()
            }
        }
    }


    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val exoProgressBar = controller.exoProgressBar
            val exoPlayPause = controller.exoPlayPause

            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    exoPlayPause.visibility = View.GONE
                    exoProgressBar.visibility = View.VISIBLE
                }

                Player.STATE_READY -> {
                    binding.contentProgress.visibility = View.GONE
                    exoPlayPause.visibility = View.VISIBLE
                    exoProgressBar.visibility = View.GONE
                }

                Player.STATE_IDLE -> {
                    exoPlayPause.visibility = View.GONE
                    exoProgressBar.visibility = View.VISIBLE
                }

                Player.STATE_ENDED -> {
                    if (player!!.hasNextMediaItem()) {
                        player!!.seekToNextMediaItem()
                    }
                }
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val ismovie = arguments?.getBoolean("isMovie")
            val name = arguments?.getString("name")
            val epNames = arguments?.getStringArrayList("epNames")
            isItemChanged = true
            controller.exoMainText.text =
                if (ismovie!!) name else epNames!![player!!.currentMediaItemIndex]
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val exoProgressBar = controller.exoProgressBar
            val exoPlayPause = controller.exoPlayPause
            if (isPlaying) {
                binding.contentProgress.visibility = View.GONE
                exoPlayPause.visibility = View.VISIBLE
                exoProgressBar.visibility = View.GONE
            }
            super.onIsPlayingChanged(isPlaying)
        }


    }


    enum class PlayerResize(@StringRes val nameRes: Int) {
        Fit(R.string.resize_fit),
        Fill(R.string.resize_fill),
        Zoom(R.string.resize_zoom),
    }

    fun nextResize() {
        resizeMode = (resizeMode + 1) % PlayerResize.values().size
        resize(resizeMode, true)
    }

    fun resize(resize: Int, showToast: Boolean) {
        resize(PlayerResize.values()[resize], showToast)
    }

    fun resize(resize: PlayerResize, showToast: Boolean) {

        val type = when (resize) {
            PlayerResize.Fill -> AspectRatioFrameLayout.RESIZE_MODE_FILL
            PlayerResize.Fit -> AspectRatioFrameLayout.RESIZE_MODE_FIT
            PlayerResize.Zoom -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
        }
        binding.playerView.resizeMode = type

        if (showToast)
            showToast(activity, resize.nameRes, Toast.LENGTH_SHORT)
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            lifecycleScope.launch {
                initializePlayerIfNeeded()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || player == null) {
            lifecycleScope.launch {
                initializePlayerIfNeeded()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            lifecycleScope.launch {
                releasePlayerIfNeeded()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            // releasePlayer()
            lifecycleScope.launch {
                releasePlayerIfNeeded()
            }
        }
    }

    override fun onDestroyView() {
        setNotFullScreen()
        super.onDestroyView()
        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.visibility = View.VISIBLE
        _binding = null

    }

}