package com.stream.baba.ui.homedata

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.stream.baba.R
import com.stream.baba.data.Data
import com.stream.baba.data.HomeViewModel

class HomeAdapter(
    private val context: Context,
    private val itemList: List<Data>,
    private val viewModel: HomeViewModel,
    private val cardClickCallback: () -> Unit

) :
    RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.home_result_grid, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.bindData(item,cardClickCallback,viewModel)
    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.background_card)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val imageText: TextView = itemView.findViewById(R.id.imageText)

        fun bindData(data: Data, cardClickCallback: () -> Unit, viewModel: HomeViewModel) {
            // Bind the movie data to the views
            Glide.with(context)
                .load(GlideUrl(data.photo))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL).let { req ->
                    req.transition(DrawableTransitionOptions.withCrossFade())
                }
                .into(imageView)

            imageText.text = data.name

            cardView.setOnClickListener {
                viewModel.setSelectedData(data)
                cardClickCallback()
            }
        }
    }
}
