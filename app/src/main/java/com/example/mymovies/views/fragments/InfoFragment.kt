package com.example.mymovies.views.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.mymovies.R
import com.example.mymovies.databinding.FragmentInfoBinding
import com.example.mymovies.databinding.ItemMovieDialogBinding
import com.example.mymovies.models.CastItem
import com.example.mymovies.models.Movie
import com.example.mymovies.utils.*
import com.example.mymovies.utils.constants.Constants
import com.example.mymovies.viewmodels.CommonViewModel
import com.example.mymovies.viewmodels.actions.MovieAction
import com.example.mymovies.views.adapters.ItemCastAdapter
import com.example.mymovies.views.adapters.ItemQualityAdapter
import com.example.mymovies.views.adapters.ItemScreenShotAdapter
import com.example.mymovies.views.adapters.listener.ListenerQuality
import com.google.android.youtube.player.*
import dagger.hilt.android.AndroidEntryPoint
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

import android.content.ClipData
import android.content.ClipboardManager


@AndroidEntryPoint
class InfoFragment : Fragment(), ListenerQuality,YouTubePlayer.OnFullscreenListener{

    private var _binding: FragmentInfoBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: CommonViewModel
    private var enableFullScreen = false
    private lateinit var alertDialog: AlertDialog
    private val youTubeFragment by lazy {childFragmentManager.findFragmentById(R.id.container_youtube_player) as YouTubePlayerSupportFragmentX? }
    private lateinit var youTubePlayer: YouTubePlayer
    private var movieID: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        movieID = InfoFragmentArgs.fromBundle(requireArguments()).id
        initViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imageMovieCover.transitionName = "transition"
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get(CommonViewModel::class.java)
    }

    private fun initObservers() {
        viewModel.isSaved.removeObservers(viewLifecycleOwner)
        viewModel.doAction(MovieAction.CheckSavedMovie(movieID))
        viewModel.isSaved.observe(
            viewLifecycleOwner,
            {
                if (it != null) {
                    if (it){
                        binding.buttonFavorite.text = "L"
                        binding.buttonFavorite.setBackgroundResource(R.drawable.ic_heart_24)
                    }else{
                        binding.buttonFavorite.text = "NL"
                        binding.buttonFavorite.setBackgroundResource(R.drawable.ic_heart_border_24)
                    }
                }
            }
        )
        viewModel.movieDetails.removeObservers(viewLifecycleOwner)
        viewModel.doAction(MovieAction.FetchMovieDetails(movieID))
        viewModel.movieDetails.observe(
            viewLifecycleOwner,
            {
                if (it != null){
                binding.apply {
                    imageMovieBackground.downloadImage(it.backgroundImageOriginal)
                    imageMovieCover.downloadImage(it.mediumCoverImage)
                    textMovieName.text = it.titleEnglish
                    textCategory.addCategories(it.genres!!)
                    textRatingMpa.textOrGone(it.mpaRating)
                    textDescription.text = it.descriptionFull
                    textMovieRating.formatText(R.string.format, it.rating.toString())
                }

                initRecycler(it.cast, listOf(it.mediumScreenshotImage1!!, it.mediumScreenshotImage2!!, it.mediumScreenshotImage3!!))
                initOnClick(it)
                }

                initYouTubePlayer(viewModel.movieDetails.value!!.ytTrailerCode)
                showToast(viewModel.movieDetails.value!!.ytTrailerCode.toString())
            }
        )
    }

    private fun generateMagneticUrl(hash: String, movieName: String?): String? {
        var baseString = "magnet:?xt=urn:btih:$hash&dn="
        val encodedMovieName: String = URLEncoder.encode(movieName, "utf-8").replace("+", "%20")
        baseString += encodedMovieName
        val tracker1 = "udp://glotorrents.pw:6969/announce"
        val tracker2 = "udp://tracker.opentrackr.org:1337/announce"
        val tracker3 = "udp://torrent.gresille.org:80/announce"
        val tracker4 = "udp://tracker.openbittorrent.com:80"
        val tracker5 = "udp://tracker.coppersurfer.tk:6969"
        val tracker6 = "udp://tracker.leechers-paradise.org:6969"
        val tracker7 = "udp://p4p.arenabg.ch:1337"
        val tracker8 = "udp://tracker.internetwarriors.net:1337"
        val trackerArray =
            arrayOf(tracker1, tracker2, tracker3, tracker4, tracker5, tracker6, tracker7, tracker8)
        var trackers = ""
        for (trackerItem in trackerArray) {
            trackers += "&tr=" + URLEncoder.encode(trackerItem, "utf-8").replace("+", "%20")
        }
        return baseString + trackers
    }

    private fun initOnClick(movie: Movie) {
        binding.fabPlay.setOnClickListener {
            showMovieQualityDialog(movie, "P",requireView())
        }

        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }

        binding.buttonFavorite.setOnClickListener {
            if (binding.buttonFavorite.text == "NL") {
                binding.buttonFavorite.text = "L"
                binding.buttonFavorite.setBackgroundResource(R.drawable.ic_heart_24)
                viewModel.doAction(MovieAction.SaveMovie(movie))
            } else {
                binding.buttonFavorite.text = "NL"
                binding.buttonFavorite.setBackgroundResource(R.drawable.ic_heart_border_24)
                viewModel.doAction(MovieAction.DeleteSavedMovie(movie.id!!))
            }
            showToast(binding.buttonFavorite.text.toString())
        }
    }

    private fun initRecycler(castList: List<CastItem>?, screenShotList: List<String>) {
        if (castList!!.isEmpty()) binding.recyclerCast.gone()
        else binding.recyclerCast.adapter = ItemCastAdapter(castList)

        binding.recyclerScreenshot.adapter = ItemScreenShotAdapter(screenShotList)
    }

    private fun initYouTubePlayer(ytTrailerCode: String?) {
        youTubeFragment!!.initialize(
            Constants.YOUTUBE_API_KEY,
            object : YouTubePlayer.OnInitializedListener {
                override fun onInitializationSuccess(
                    provider: YouTubePlayer.Provider?,
                    player: YouTubePlayer?,
                    bool: Boolean
                ) {
                    try {
                        if (ytTrailerCode!!.isEmpty())
                            throw IllegalArgumentException()
                        youTubePlayer = player!!
                        youTubePlayer.setOnFullscreenListener(this@InfoFragment)
                        if (!bool)
                            youTubePlayer.cueVideo(ytTrailerCode)
                    } catch (e: IllegalArgumentException) {
                        binding.containerYoutube.gone()
                    }
                }

                override fun onInitializationFailure(
                    p0: YouTubePlayer.Provider?,
                    p1: YouTubeInitializationResult?
                ) {
                    showToast("Is you tube there in phone")
                }
            }
        )
    }

    private fun showMovieQualityDialog(movie: Movie?,type:String, view: View) {
        val viewGroup: ViewGroup? = view.findViewById(android.R.id.content)
        val dialogView = ItemMovieDialogBinding.inflate(LayoutInflater.from(requireContext()), viewGroup, false)
        LayoutInflater.from(view.context).inflate(
            R.layout.item_movie_dialog, viewGroup, false
        )
        AlertDialog.Builder(view.context).apply {
            setTitle("Select Quality")
            setView(dialogView.root)
            alertDialog = create()
        }
        val magnetLinks = mutableListOf<String>()
        for (x in movie!!.torrents!!.indices)
            magnetLinks.add(generateMagneticUrl(movie.torrents!![x].hash!!, movie.titleEnglish!!.plus(" (${movie.year!!}) ${movie.torrents[x].quality}"))!!)
        dialogView.recyclerMovies.adapter = ItemQualityAdapter(
            movie.torrents!!,
            magnetLinks,
            type,
            "${movie.titleEnglish} ${movie.year}", this
        )
        alertDialog.show()
    }

    override fun selectQuality(url: String, name: String) {
        findNavController().navigate(InfoFragmentDirections.actionInfoFragmentToWatchFragment(url, name))
        alertDialog.dismiss()
    }

    fun onBackPressed(): Boolean {
        if (enableFullScreen)
            youTubePlayer.setFullscreen(false)
        else
            findNavController().popBackStack()
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setSavedNull()
        viewModel.setMovieDetailsNull()
        _binding = null
    }

    override fun onFullscreen(p0: Boolean) {
        enableFullScreen = p0
    }
}
