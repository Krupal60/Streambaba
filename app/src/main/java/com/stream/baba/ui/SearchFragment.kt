package com.stream.baba.ui

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.stream.baba.R
import com.stream.baba.data.HomePageList
import com.stream.baba.data.HomeViewModel
import com.stream.baba.databinding.FragmentSearchBinding
import com.stream.baba.ui.homedata.ExpandAdapter
import com.stream.baba.ui.search.SearchAapter
import com.stream.baba.utils.AppUtils.ownHide
import com.stream.baba.utils.AppUtils.ownShow
import java.util.Locale

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private var bottomSheetDialog: BottomSheetDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
        )
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun Activity.loadHomepageList(
        homePageList: HomePageList?,
        ClickCallback: () -> Unit,
        dismissCallback: () -> Unit
    ): BottomSheetDialog {
        val context = this

        val bottomSheetDialogBuilder = BottomSheetDialog(context) // Apply the custom style
        bottomSheetDialogBuilder.setContentView(R.layout.home_expand)
        val title = bottomSheetDialogBuilder.findViewById<TextView>(R.id.home_expanded_text)!!
        val item = homePageList
        title.text = item!!.name
        val recyclerView =
            bottomSheetDialogBuilder.findViewById<RecyclerView>(R.id.home_expanded_recycler)!!
        val progressBar =
            bottomSheetDialogBuilder.findViewById<ProgressBar>(R.id.progressBar)!!
        val progressBarLoadMore =
            bottomSheetDialogBuilder.findViewById<ProgressBar>(R.id.progressBarLoadMore)!!

        progressBar.isVisible = false
        progressBarLoadMore.isVisible = false

        val layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
          recyclerView.layoutManager  = layoutManager

        recyclerView.adapter =
            ExpandAdapter(requireContext(),item.list.toMutableList(), viewModel, ClickCallback)
        val titleHolder =
            bottomSheetDialogBuilder.findViewById<FrameLayout>(R.id.home_expanded_drag_down)!!
        titleHolder.setOnClickListener {
            bottomSheetDialogBuilder.dismiss()
        }
        bottomSheetDialogBuilder.setOnDismissListener {
            dismissCallback.invoke()
        }
        bottomSheetDialogBuilder.show()
        return bottomSheetDialogBuilder
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            searchHistoryRecycler.visibility = View.GONE
            searchMasterRecycler.visibility = View.GONE
            bottomSheetDialog?.ownShow()
            searchLoadingBar.hide()

            mainSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchLoadingBar.show()
                    if (query != null) {
                        if (!query.isNullOrBlank()) {
                            val formattedQuery = formatQuery(query)
                            viewModel.loadSearchData(formattedQuery)
                        }
                    }
                    mainSearch.clearFocus()
                    return true // We return false here to indicate that the query is not being handled by this method
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })

            mainSearch.setOnCloseListener {
                searchMasterRecycler.visibility = View.GONE
                true
            }

            viewModel.searchdata.observe(viewLifecycleOwner) { homePageList ->
                searchLoadingBar.hide()
                updateUIWithData(homePageList, viewModel)
            }
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

    private fun updateUIWithData(
        data: List<HomePageList?>,
        viewModel: HomeViewModel
    ) {
        binding.apply {

        searchMasterRecycler.visibility = View.VISIBLE
            searchMasterRecycler.itemAnimator = DefaultItemAnimator()
            searchMasterRecycler.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val adapter = SearchAapter(requireContext(),data, viewModel, {
            goToResultFragment()
        }) {expandData ->
            bottomSheetDialog = activity?.loadHomepageList(expandData!!, {
                goToResultFragment()
            }) {
                bottomSheetDialog = null
            }
        }
            searchMasterRecycler.adapter = adapter
    }
    }

    private fun goToResultFragment() {
        bottomSheetDialog?.ownHide()
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
        bottomSheetDialog?.ownHide()
        _binding = null
    }
}

