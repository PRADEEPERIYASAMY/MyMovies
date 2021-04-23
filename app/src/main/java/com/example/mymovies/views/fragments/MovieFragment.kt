package com.example.mymovies.views.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.R
import com.example.mymovies.databinding.FragmentMovieBinding
import com.example.mymovies.utils.constants.Constants
import com.example.mymovies.viewmodels.CommonViewModel
import com.example.mymovies.viewmodels.actions.MovieAction
import com.example.mymovies.views.adapters.ItemMovieAdapter
import com.example.mymovies.views.adapters.ItemMovieTypeAdapter
import com.example.mymovies.views.adapters.listener.ListenerAdapter
import dagger.hilt.android.AndroidEntryPoint
import android.R.menu
import android.app.ActionBar

import android.view.MenuInflater
import android.widget.SearchView
import com.example.mymovies.utils.*
import androidx.appcompat.app.AppCompatActivity

@AndroidEntryPoint
class MovieFragment : Fragment(), ListenerAdapter {

    private var _binding: FragmentMovieBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: CommonViewModel
    private val typeAdapter = ItemMovieTypeAdapter()
    private val movieAdapter = ItemMovieAdapter(this)
    private var pgNo = 1
    private var type:String = Constants.typeList[0]
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMovieBinding.inflate(inflater, container, false)
        initViewModel()
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initListener()
        initObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_movie_types, menu)
        searchView = menu.findItem(R.id.action_search).actionView as SearchView
        searchView.maxWidth = Int.MAX_VALUE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query!!.isNotEmpty()) {
                    closeSearch(requireContext(),query)
                }
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {

                return false
            }
        })
    }

    private fun closeSearch(context: Context,query:String) {
        viewModel.doAction(MovieAction.FetchQueryMovies(query, 1))
        searchView.clearFocus()
        resetType()
        val input = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        input.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get(CommonViewModel::class.java)
    }

    private fun initObservers() {
        viewModel.listTypeMovies.removeObservers(viewLifecycleOwner)
        viewModel.doAction(MovieAction.FetchTypeMovies(type, pgNo))
        viewModel.listTypeMovies.observe(
            viewLifecycleOwner,
            {
                binding.refreshMovies.isRefreshing = false
                binding.homeInternetConnection.root.gone()
                binding.recyclerMovies.show()
                movieAdapter.updateList(it!!)
            }
        )

        viewModel.moviesResponse.removeObservers(viewLifecycleOwner)
        viewModel.moviesResponse.observe(viewLifecycleOwner,{
            movieAdapter.clearList()
            movieAdapter.addList(it.data!!.movies)
        })

        typeAdapter.selectedIndex.removeObservers(viewLifecycleOwner)
        typeAdapter.selectedIndex.observe(viewLifecycleOwner,{
            type = it
            pgNo = 1
            initMovieTypeData()
            movieAdapter.clearList()
        })
    }

    private fun initRecycler() {
        binding.recyclerMovieType.apply {
            adapter = typeAdapter
        }

        binding.recyclerMovies.apply {
            adapter = movieAdapter
            itemAnimator = null
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(
                    recyclerView: RecyclerView,
                    newState: Int
                ) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1)){
                        binding.refreshMovies.isRefreshing = true
                        viewModel.doAction(MovieAction.FetchTypeMovies(type, pgNo))
                        pgNo += 1
                    }
                }
            })
        }
    }

    private fun initMovieTypeData(){
        viewModel.doAction(MovieAction.FetchTypeMovies(type, pgNo))
        pgNo += 1
    }

    private fun initOnclick() {
    }

    private fun initListener() {
        binding.refreshMovies.setOnRefreshListener { viewModel.doAction(MovieAction.FetchTypeMovies(type)) }
    }

    private fun resetType() {
        if (typeAdapter.selectedIndex.value != Constants.typeList[0]) {
            binding.recyclerMovieType.smoothScrollToPosition(0)
            typeAdapter.reset()
            pgNo = 1
        }
    }

    override fun itemClicked(pos: Int) {
        viewModel.doAction(MovieAction.FetchTypeMovies(Constants.typeList[pos]))
        movieAdapter.clearList()
    }

    override fun openMovie(movieID: Int, imageView: ImageView) {
        val extras = FragmentNavigatorExtras(imageView to "transition")
        val action =
            MovieFragmentDirections.actionMovieFragmentToInfoFragment(movieID)
        findNavController().navigate(action, extras)
        activity?.hideBottomNav()
    }

    override fun onResume() {
        super.onResume()
        activity?.showBottomNav()
    }

    fun onBackPressed(): Boolean {
        if (movieAdapter.currentPosition > 10) {
            movieAdapter.clearList()
            /*viewModel.doAction(MovieAction.FetchQueryMovies(binding.editSearchMovie.text.toString(), 1))*/
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
