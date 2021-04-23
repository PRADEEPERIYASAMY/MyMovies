package com.example.mymovies.views.fragments

import android.app.AlertDialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.mymovies.databinding.FragmentWatchBinding
import com.example.mymovies.databinding.ItemMovieDialogBinding
import com.example.mymovies.utils.*
import com.example.mymovies.viewmodels.CommonViewModel
import com.example.mymovies.viewmodels.actions.MovieAction
import com.example.mymovies.views.adapters.ItemQualityAdapter
import com.example.mymovies.views.adapters.ItemSubtitleAdapter
import com.example.mymovies.views.adapters.listener.ListenerSubtitle
import com.github.se_bastiaan.torrentstream.StreamStatus
import com.github.se_bastiaan.torrentstream.Torrent
import com.github.se_bastiaan.torrentstream.TorrentStream
import com.github.se_bastiaan.torrentstream.listeners.TorrentListener
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MergingMediaSource
import com.google.android.exoplayer2.source.SingleSampleMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.masterwok.opensubtitlesandroid.models.OpenSubtitleItem
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.lang.Exception
import com.example.mymovies.R
import javax.inject.Inject

@AndroidEntryPoint
class WatchFragment : Fragment(), TorrentListener, ListenerSubtitle, Player.EventListener {

    private var _binding: FragmentWatchBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: CommonViewModel
    @Inject
    lateinit var simplePlayer: SimpleExoPlayer
    @Inject
    lateinit var torrentStream: TorrentStream
    @Inject
    lateinit var format: Format
    @Inject
    lateinit var factory: DefaultDataSourceFactory
    private lateinit var mergeMediaSource: MergingMediaSource
    private lateinit var mediaSource: ExtractorMediaSource
    private lateinit var alertDialog: AlertDialog
    private var url = ""
    private var name = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWatchBinding.inflate(inflater, container, false)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        url = WatchFragmentArgs.fromBundle(requireArguments()).url!!
        name = WatchFragmentArgs.fromBundle(requireArguments()).name
        showToast(url.toString())
        initViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTorrentStream()
        initObservers()
        initListeners()
    }

    private fun initTorrentStream() {
        torrentStream.startStream(url)
        torrentStream.addListener(this)
    }

    private fun initObservers() {
        viewModel.subtitleData.removeObservers(viewLifecycleOwner)
        viewModel.subtitleData.observe(viewLifecycleOwner, {
                if (it != null){
                    showMovieSubtitlesDialog(it, requireView())
                }
                simplePlayer.stopPlayer()
            }
        )
        viewModel.subtitleStatus.removeObservers(viewLifecycleOwner)
        viewModel.subtitleStatus.observe(viewLifecycleOwner, Observer {
            alertDialog.dismiss()
            addSubtitleToPlayer(it)
            }
        )
    }

    private fun initListeners() {
        binding.playerMovie.setControllerVisibilityListener {
            with(activity?.window?.decorView) {
                if (it == 0)
                    this?.systemUiVisibility = showSystemUI()
                else
                    this?.systemUiVisibility = hideSystemUI()
            }
        }
        binding.playerMovie.findViewById<ImageButton>(R.id.button_subtitle).setOnClickListener {
            viewModel.doAction(MovieAction.FetchMovieSubTitle(name))
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get(CommonViewModel::class.java)
    }

    private fun initPlayer(path: String) {
        binding.playerMovie.player = simplePlayer
        mediaSource = ExtractorMediaSource.Factory(factory).createMediaSource(Uri.parse(path))
        mergeMediaSource = MergingMediaSource(mediaSource)
        simplePlayer.apply {
            startPlayer(mergeMediaSource)
            addListener(this@WatchFragment)
        }
    }

    private fun showMovieSubtitlesDialog(listOfSubtitles: Array<OpenSubtitleItem>, view: View) {
        val viewGroup: ViewGroup? = view.findViewById(android.R.id.content)
        val dialogView = ItemMovieDialogBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, false)
        LayoutInflater.from(view.context).inflate(R.layout.item_movie_dialog, viewGroup, false)
        AlertDialog.Builder(view.context).apply {
            setTitle("Subtitle")
            setView(dialogView.root)
            alertDialog = create()
        }
        dialogView.recyclerMovies.apply {
            addDividers()
            adapter = ItemSubtitleAdapter(listOfSubtitles, this@WatchFragment)
        }
        alertDialog.show()
    }

    override fun onStreamPrepared(torrent: Torrent?) {

    }

    override fun onStreamStarted(torrent: Torrent?) {

    }

    override fun onStreamError(torrent: Torrent?, e: Exception?) {
        e!!.printStackTrace()
    }

    override fun onStreamReady(torrent: Torrent?) {
        initPlayer(torrent?.videoFile?.absolutePath!!)
    }

    override fun onStreamProgress(torrent: Torrent?, status: StreamStatus?) {
        try {
            binding.textSeeds.formatText(com.example.mymovies.R.string.streamSeeds, status?.seeds)
            binding.streamSpeed.formatText(com.example.mymovies.R.string.streamDownloadSpeed, status?.downloadSpeed?.div(1024))
            binding.streamProgressTxt.formatText(com.example.mymovies.R.string.streamProgress, status?.progress, "%")
        } catch (e: IllegalStateException) {
            println("ERROR: $e")
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        if (playWhenReady && playbackState == Player.STATE_READY)
            binding.containerProgress.gone()
        else if (playWhenReady && playbackState == Player.STATE_BUFFERING)
            binding.containerProgress.show()
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        simplePlayer.seekPlayer(simplePlayer.contentPosition, mergeMediaSource)
    }

    override fun onStreamStopped() {}

    private fun addSubtitleToPlayer(data: Uri?) {
        val textFormat = format
        val textMediaSource = SingleSampleMediaSource.Factory(factory)
            .createMediaSource(data, textFormat, C.TIME_UNSET)
        mergeMediaSource = MergingMediaSource(mediaSource, textMediaSource)
        simplePlayer.addingSubtitle(mergeMediaSource, simplePlayer.currentPosition)
    }

    override fun onSubtitleClicked(subtitle: OpenSubtitleItem) {
        viewModel.doAction(MovieAction.DownloadMovieSubTitle(requireContext(),subtitle, Uri.fromFile(File("${activity?.getExternalFilesDir(null)?.absolutePath}/${subtitle.SubFileName}"))))
    }

    override fun onPause() {
        super.onPause()
        torrentStream.currentTorrent.pause()
        if (this::simplePlayer.isInitialized) simplePlayer.stopPlayer()
    }

    override fun onResume() {
        super.onResume()
        if (torrentStream.currentTorrent != null)
            torrentStream.currentTorrent.resume()
        if (this::simplePlayer.isInitialized) simplePlayer.resumePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
        torrentStream.removeListener(this)
        torrentStream.stopStream()
        if (this::simplePlayer.isInitialized)
            simplePlayer.release()
        _binding = null
    }
}
