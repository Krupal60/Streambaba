package com.stream.baba.ui.homedata

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.stream.baba.data.Data
import com.stream.baba.data.HomeViewModel
import com.stream.baba.databinding.HomeResultGridExpandedBinding

class ExpandedPageAdapter(
    private val context: Context,
    private val viewModel: HomeViewModel,
    private val clickCallback: () -> Unit
) : PagingDataAdapter<Data, ExpandedViewHolder>(Companion) {

    companion object : DiffUtil.ItemCallback<Data>() {
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: ExpandedViewHolder, position: Int) {
        val binding = holder.binding
        val item = getItem(position)
        Log.d("items","$item")
        holder.bindData(item!!, clickCallback, viewModel, binding,context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpandedViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HomeResultGridExpandedBinding.inflate(layoutInflater, parent, false)
        return ExpandedViewHolder(binding)
    }
}

class ExpandedViewHolder(
    val binding: HomeResultGridExpandedBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bindData(
        data: Data,
        clickCallback: () -> Unit,
        viewModel: HomeViewModel,
        binding: HomeResultGridExpandedBinding,
        context: Context
    ) {
        // Bind the movie data to the views
        Glide.with(context)
            .load(GlideUrl(data.photo))
            .diskCacheStrategy(DiskCacheStrategy.ALL).let { req ->
                req.transition(DrawableTransitionOptions.withCrossFade())
            }
            .into(binding.imageView)
        binding.imageText.text = data.name

        binding.imageHolder.setOnClickListener {
            viewModel.setSelectedData(data)
            clickCallback()
        }

    }
}

