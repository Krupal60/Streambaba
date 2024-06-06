package com.stream.baba.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kenilt.loopingviewpager.scroller.AutoScroller
import com.stream.baba.R
import com.stream.baba.data.Data
import com.stream.baba.data.HomePageList
import com.stream.baba.data.HomeViewModel
import com.stream.baba.databinding.FragmentHomeBinding
import com.stream.baba.ui.homedata.ExpandedPageAdapter
import com.stream.baba.ui.homedata.ParentAdapter
import com.stream.baba.ui.homedata.ViewPagerAdapter
import com.stream.baba.utils.AppUtils.observeOnce
import com.stream.baba.utils.AppUtils.ownHide
import com.stream.baba.utils.AppUtils.ownShow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()
    private var bottomSheetDialog: BottomSheetDialog? = null
    //private var expandAdapter: ExpandAdapter? = null

    private var newData: List<Data>? = null
    private var firstTime = true


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

        val layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        val expandAdapter = ExpandedPageAdapter(requireContext(), viewModel, ClickCallback)



        recyclerView.adapter = expandAdapter


        lifecycleScope.launch {
            val data = viewModel.createPager(item!!.name)
            data.collect {
                expandAdapter.submitData(it)
            }
        }

        lifecycleScope.launch {
            expandAdapter.loadStateFlow.collectLatest { loadStates ->
                progressBar.isVisible = loadStates.refresh is LoadState.Loading
                progressBarLoadMore.isVisible = loadStates.append is LoadState.Loading
            }
        }


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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel.loadData()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            bottomSheetDialog?.ownShow()
            mainSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (!query.isNullOrBlank()) {
                        val formattedQuery = formatQuery(query)
                        viewModel.loadHomeSearchData(formattedQuery)
                        viewModel.setquery(query)
                        goToHomeSearchFragment()
                        mainSearch.isIconified = true
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {

                    return false
                }

            })
            viewModel.data.observeOnce(viewLifecycleOwner) { homePageList ->
                updateUIWithData(homePageList, viewModel.bannerData.value, viewModel)
            }
            viewModel.bannerData.observeOnce(viewLifecycleOwner) { bannerList ->
                updateUIWithData(viewModel.data.value, bannerList, viewModel)
            }
        }
    }

    private fun formatQuery(query: String): String {
        return query.split(" ").joinToString(" ") { it ->
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.ROOT
                ) else it.toString()
            }
        }
    }

    private fun updateUIWithData(
        data: List<HomePageList?>?,
        bannerData: List<Data?>?,
        viewModel: HomeViewModel
    ) {
        binding.apply {
            // Update your UI components with the received data

            searchLoadingBar.hide()
            homePreview.visibility = View.VISIBLE
            mainSearch.visibility = View.VISIBLE
            homeplay.visibility = View.VISIBLE
            homeLoading.visibility = View.GONE
            homeLoadingShimmer.apply {
                stopShimmer()
                visibility = View.GONE
            }
            val viewPagerAdapter = ViewPagerAdapter(requireContext(), bannerData ?: emptyList())
            autoScrollViewPager.adapter = viewPagerAdapter
            val autoScroller = AutoScroller(autoScrollViewPager, lifecycle, 2500)
            autoScroller.isAutoScroll = true

            homeMasterRecycler.visibility = View.VISIBLE
            homeMasterRecycler.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            val adapter = ParentAdapter(requireContext(), data, viewModel, {
                goToResultFragment()
            }) { expandData ->
                firstTime = true

                Log.d("Debug", "firstTime set to true: $firstTime")
                bottomSheetDialog = activity?.loadHomepageList(expandData!!, {
                    goToResultFragment()
                }) {
                    bottomSheetDialog = null
                }

            }
            homePreviewInfo.setOnClickListener {

                val data = bannerData!![autoScrollViewPager.currentItem]
                viewModel.setSelectedData(data)
                goToResultFragment()

            }
            homePreviewPlay.setOnClickListener {
                val data = bannerData!![autoScrollViewPager.currentItem]
                if (data!!.isMovie) {
                    goToResultFragment()
                    goToPlayerFragment(data.webLink, data.name)
                } else {
                    goToResultFragment()
                    viewModel.loadEpData(data.name, data.type)
                    goToPlayerFragment(
                        viewModel.epData.value?.get(0)?.webLink ?: "https://www.google.com/",
                        viewModel.epData.value?.get(0)?.name ?: " None"
                    )
                }
                viewModel.setSelectedData(data)
            }

            homeMasterRecycler.adapter = adapter


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

    fun openLink(link: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(link)
        intent.setPackage("com.android.chrome")

        // Check if Chrome is installed
        if (context?.packageManager?.resolveActivity(intent, 0) != null) {
            startActivity(intent)
        } else {
            // Chrome is not installed, open it in a web browser
            intent.setPackage(null)
            startActivity(intent)
        }
    }

    private fun goToPlayerFragment(videoUrl: String, name: String) {
        val bundle = Bundle()
        bundle.putString("videoUrl", videoUrl)
        bundle.putString("name", name)
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.slide_out)
            .setPopEnterAnim(R.anim.slide_in)
            .setPopExitAnim(R.anim.slide_out)
            .build()
        findNavController().navigate(R.id.playerFragment, bundle, navOptions)
    }


    private fun goToHomeSearchFragment() {
        bottomSheetDialog?.ownHide()
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.slide_out)
            .setPopEnterAnim(R.anim.slide_in)
            .setPopExitAnim(R.anim.slide_out)
            .build()
        findNavController().navigate(R.id.homeSearchFragment, null, navOptions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bottomSheetDialog?.ownHide()
        _binding = null
    }


}







