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
import com.example.mymovies.databinding.FragmentSavedBinding
import com.example.mymovies.utils.*
import com.example.mymovies.viewmodels.CommonViewModel
import com.example.mymovies.viewmodels.actions.MovieAction
import com.example.mymovies.views.adapters.ItemMovieAdapter
import com.example.mymovies.views.adapters.ItemSavedAdapter
import com.example.mymovies.views.adapters.listener.ListenerSaved
import com.yarolegovich.discretescrollview.DiscreteScrollView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SavedFragment :
    Fragment(),
    ListenerSaved/*,
    DiscreteScrollView.OnItemChangedListener<ItemSavedAdapter.ViewHolder> */{

    private var _binding: FragmentSavedBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var viewModel: CommonViewModel
    private val itemSavedAdapter = ItemMovieAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        initViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        initObservers()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(requireActivity()).get(CommonViewModel::class.java)
    }

    private fun initObservers() {
        viewModel.listSavedMovie.removeObservers(viewLifecycleOwner)
        viewModel.listSavedMovie.observe(
            viewLifecycleOwner,
            {
                binding.progressMovie.gone()
                binding.containerNoMovies.gone()
                binding.recyclerSaved.show()
                binding.imageBackground.show()
                itemSavedAdapter.apply {
                    addList(it)
                    binding.imageBackground.downloadImage(getMovieCover(lastPosition))
                }
            }
        )
    }

    private fun initRecycler() {
        binding.recyclerSaved.apply {
            adapter = itemSavedAdapter
            /*val transformer: ScaleTransformer = get()
            setItemTransformer(transformer)*/
            addOnItemChangedListener(this@SavedFragment)
        }
    }

    override fun onDeleteSavedMovie(id: Int) {
        viewModel.doAction(MovieAction.DeleteSavedMovie(id))
    }

    override fun onMovieClicked(id: Int, imageView: ImageView) {
        val extras =
            FragmentNavigatorExtras(imageView to "transition")
        val action =
            SavedFragmentDirections.actionSavedFragmentToInfoFragment(id)
        findNavController().navigate(action, extras)

        activity?.hideBottomNav()
    }

    override fun onCurrentItemChanged(
        viewHolder: ItemSavedAdapter.ViewHolder?,
        adapterPosition: Int
    ) {
        binding.imageBackground.downloadImage(itemSavedAdapter.getMovieCover(adapterPosition))
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
