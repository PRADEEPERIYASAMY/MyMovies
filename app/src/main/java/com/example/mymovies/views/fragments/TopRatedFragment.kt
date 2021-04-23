package com.example.mymovies.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mymovies.databinding.FragmentTopRatedBinding
import com.example.mymovies.utils.*
import com.example.mymovies.utils.showBottomNav
import com.example.mymovies.viewmodels.CommonViewModel
import com.example.mymovies.viewmodels.actions.MovieAction
import com.example.mymovies.views.adapters.ItemMovieAdapter
import com.example.mymovies.views.adapters.listener.ListenerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TopRatedFragment : Fragment(), ListenerAdapter {

    private var _binding: FragmentTopRatedBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: CommonViewModel
    private val itemMovieAdapter = ItemMovieAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTopRatedBinding.inflate(inflater, container, false)
        initViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initRecycler()
        initListener()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get(CommonViewModel::class.java)
    }

    private fun initObservers() {
        viewModel.listTopRatedMovies.removeObservers(viewLifecycleOwner)
        viewModel.doAction(MovieAction.FetchTopRatedMovies(1))
        viewModel.listTopRatedMovies.observe(
            viewLifecycleOwner,
            {
                itemMovieAdapter.addList(it!!)
                binding.refreshTopRated.isRefreshing = false
                binding.containerNoConnection.root.gone()
                binding.recyclerTopRated.show()
            }
        )
    }

    private fun initOnclick() {
    }

    private fun initListener() {
        binding.refreshTopRated.setOnRefreshListener {
            viewModel.doAction(MovieAction.FetchTopRatedMovies(1))
        }
    }

    private fun initRecycler() {
        binding.recyclerTopRated.apply {
            adapter = itemMovieAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1))
                        viewModel.doAction(MovieAction.FetchTopRatedMovies(2))
                }
            })
        }
    }

    override fun itemClicked(pos: Int) {
    }

    override fun openMovie(movieID: Int, imageView: ImageView) {
        val extras =
            FragmentNavigatorExtras(imageView to "transition")
        val action =
            TopRatedFragmentDirections.actionTopRatedFragmentToInfoFragment(
                movieID
            )
        findNavController().navigate(action, extras)
        activity?.hideBottomNav()
    }

    override fun onResume() {
        super.onResume()
        activity?.showBottomNav()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
