package com.stream.baba.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.stream.baba.R
import com.stream.baba.data.Data
import com.stream.baba.data.HomeViewModel
import com.stream.baba.data.epData
import com.stream.baba.databinding.FragmentResultBinding
import com.stream.baba.ui.resultdata.EpResultAdpater

class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {

            viewModel.selectedData.observe(viewLifecycleOwner) { data ->
                if (data != null && !data.isMovie) {
                    viewModel.loadEpData(data.name, data.type)
                }
                updateUIWithData(data, viewModel.epData.value, viewModel)
            }

            viewModel.epData.observe(viewLifecycleOwner) { epData ->
                val selectedData = viewModel.selectedData.value
                if (selectedData != null && !selectedData.isMovie) {
                    updateUIWithData(viewModel.selectedData.value, epData, viewModel)
                }
            }

        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateUIWithData(data: Data?, epData: List<epData?>?, viewModel: HomeViewModel) {
        binding.apply {
            backButton.setOnClickListener {
                findNavController().popBackStack()
            }


            resultLoading.startShimmer()
            resultLoading.visibility = View.VISIBLE

            when (data!!.isMovie) {
                true -> {
                    resultPlayMovie.visibility = View.VISIBLE
                    resultDownlodeMovie.visibility = View.VISIBLE
                    resultMasterRecycler.visibility = View.GONE
                    resultEpisodeSelect.visibility = View.GONE
                    txtEp.visibility = View.GONE
                }

                false -> {
                    resultMasterRecycler.visibility = View.VISIBLE

                    if (epData != null && epData.size >= 20) {
                        resultEpisodeSelect.visibility = View.VISIBLE
                    } else {
                        resultEpisodeSelect.visibility = View.GONE
                    }
                    resultPlayMovie.visibility = View.GONE
                    resultDownlodeMovie.visibility = View.GONE
                    txtEp.text = getString(R.string.episode) + " : ${epData?.size ?: 0}"
                    txtEp.visibility = View.VISIBLE
                }


                else -> {}
            }
            Glide.with(this@ResultFragment)
                .load(GlideUrl(data.photo))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL).let { req ->
                    req.transition(DrawableTransitionOptions.withCrossFade())
                }
                .into(resultPosterBackground)
                .clearOnDetach()



            resultTitle.text = data.name
            resultMetaType.text = if (data.isMovie) "Movie" else "Series"
            resultDescription.text = data.description
            resultMetaYear.text = data.year.toString()
            resultMetaLaguage.text = data.language
            resultMetaRating.text = "Rated: ${data.rating}/10.0"
            resultTag.visibility = View.VISIBLE
            resultTagHolder.visibility = View.VISIBLE
            resultTag.apply {
                removeAllViews()
                val genres = stringToList(data.genres)
                genres.forEach { tag ->
                    val chip = Chip(context)
                    val chipDrawable = ChipDrawable.createFromAttributes(
                        context,
                        null,
                        0,
                        R.style.ChipFilled
                    )
                    chip.setChipDrawable(chipDrawable)
                    chip.text = tag
                    chip.isChecked = false
                    chip.isCheckable = false
                    chip.isFocusable = false
                    chip.isClickable = false
                    chip.setTextColor(context.getColor(R.color.textColor))
                    addView(chip)
                }
            }
            resultDescription.setOnClickListener { view ->
                view.context?.let { ctx ->
                    val builder: AlertDialog.Builder =
                        AlertDialog.Builder(ctx, R.style.AlertDialogCustom)
                    builder.setMessage(data.description)
                        .setTitle("Description")
                        .show()
                }
            }

            resultPlayMovie.setOnClickListener {
                goToPlayerFragment(
                    data.webLink, data.name, data.isMovie, arrayListOf(), arrayListOf(),0)

            }
            resultDownlodeMovie.setOnClickListener {
                openLink(data.downloadLink)
            }


            resultMasterRecycler.itemAnimator = DefaultItemAnimator()
            resultMasterRecycler.layoutManager = LinearLayoutManager(
                requireContext().applicationContext,
                LinearLayoutManager.VERTICAL,
                false
            )
            val adapter = EpResultAdpater(epData ?: emptyList(), { it,position ->
                Log.e("epData", "$epData")
                val epweblinks = ArrayList<String>()
                val epnames = ArrayList<String>()

                epData?.forEach { epdata ->
                    epweblinks.add(epdata?.webLink ?: "")
                    epnames.add(epdata?.name ?: "")
                }
                goToPlayerFragment(it.webLink, it.name, data.isMovie, epweblinks, epnames,position)
            }, {it,position ->
                val epweblinks = ArrayList<String>()
                val epnames = ArrayList<String>()

                epData?.forEach { epdata ->
                    epweblinks.add(epdata?.webLink ?: "")
                    epnames.add(epdata?.name ?: "")
                }
                goToPlayerFragment(it.webLink, it.name, data.isMovie, epweblinks, epnames,position)
            }) {
                openLink(it.downloadLink)
            }
            resultMasterRecycler.adapter = adapter


            Handler(Looper.getMainLooper()).postDelayed({
                resultLoading.visibility = View.GONE
                resultLoading.stopShimmer()
                resultScroll.visibility = View.VISIBLE
            }, 900)

        }
    }

    private fun goToPlayerFragment(
        videoUrl: String,
        name: String,
        movie: Boolean,
        epLinks: ArrayList<String>,
        epNames: ArrayList<String>,
        position : Int
    ) {
        val bundle = Bundle()
        bundle.putString("videoUrl", videoUrl)
        bundle.putString("name", name)
        bundle.putBoolean("isMovie", movie)
        if (!movie) {
            bundle.putStringArrayList("epLinks", epLinks)
            bundle.putStringArrayList("epNames", epNames)
            bundle.putInt("position", position)
        }
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in)
            .setExitAnim(R.anim.slide_out)
            .setPopEnterAnim(R.anim.slide_in)
            .setPopExitAnim(R.anim.slide_out)
            .build()
        findNavController().navigate(R.id.playerFragment, bundle, navOptions)
    }

    fun stringToList(input: String): List<String> {
        // Remove the square brackets from the input string
        val trimmedInput = input.replace("[", "").replace("]", "")

        // Split the input string by comma to get an array of strings
        val stringArray = trimmedInput.split(",")

        // Convert the array of strings to a list of strings and return it
        return stringArray.map { it.trim() }
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}