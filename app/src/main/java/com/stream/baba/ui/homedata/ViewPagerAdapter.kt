package com.stream.baba.ui.homedata

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.stream.baba.R
import com.stream.baba.data.Data

class ViewPagerAdapter(
    private val context: Context,
    private val dataList: List<Data?> ):
    PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = LayoutInflater.from(context).inflate(R.layout.view_pager_item, container, false)
        val imageView = itemView.findViewById<ImageView>(R.id.imageView)
        val imageText = itemView.findViewById<TextView>(R.id.imageText)
        val textgenres = itemView.findViewById<TextView>(R.id.textGenres)

        // Load image using Glide or any other image loading library
        Glide.with(context)
            .load(GlideUrl(dataList[position]!!.photo))
            .diskCacheStrategy(DiskCacheStrategy.ALL).let { req ->
                req.transition(DrawableTransitionOptions.withCrossFade())
            }
            .into(imageView)


        imageText.text = dataList[position]!!.name

        textgenres.text = dataList[position]!!.genres
            .split(",").joinToString( " ⁕ ")
            .replace("[", "")
            .replace("]", "")
        container.addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getCount(): Int {
        return dataList.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

}
