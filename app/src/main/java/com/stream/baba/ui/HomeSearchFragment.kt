package com.stream.baba.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.stream.baba.R
import com.stream.baba.data.Data
import com.stream.baba.data.HomeViewModel
import com.stream.baba.databinding.FragmentHomeSearchBinding
import com.stream.baba.ui.homedata.homesearch.HomeSearchAdapter
import java.util.Locale


class HomeSearchFragment : Fragment() {

    private var _binding: FragmentHomeSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            viewModel.homeSearchdata.observe(viewLifecycleOwner) { data ->
                updateUiWithData(data,viewModel, viewModel.query.value!!)
               Handler(Looper.getMainLooper()).postDelayed({
                   searchLoadingBar.hide()
               },1000)
            }
            viewModel.query.observe(viewLifecycleOwner) { query ->
                updateUiWithData(viewModel.homeSearchdata.value,viewModel,query)
            }
            backButton.setOnClickListener {
                 findNavController().popBackStack()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        binding.searchLoadingBar.show()
    }


    private fun updateUiWithData(data: List<Data?>?, viewModel: HomeViewModel, query: String) {
        binding.apply {
            mainSearch.setQuery(query, false)
            mainSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrBlank()) {
                        val formattedQuery = formatQuery(query)
                        viewModel.loadHomeSearchData(formattedQuery)
                        viewModel.setquery(query)
                        searchLoadingBar.show()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

            })
            homeSearchMasterRecycler.itemAnimator = DefaultItemAnimator()
            homeSearchMasterRecycler.layoutManager = GridLayoutManager(requireContext(), 3)
                val adapter = HomeSearchAdapter(data ?: emptyList(), viewModel) {
                    goToResultFragment()
                }
                homeSearchMasterRecycler.adapter = adapter
            }
        }

    private fun formatQuery(query: String): String {
        return query.split(" ").joinToString(" ") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            } }
    }

    private fun goToResultFragment() {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.slide_out)
            .setPopEnterAnim(R.anim.slide_in)
            .setPopExitAnim(R.anim.slide_out)
            .build()
        findNavController().navigate(R.id.resultFragment, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}