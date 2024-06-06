package com.stream.baba.ui.resultdata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.stream.baba.R
import com.stream.baba.data.epData

class EpResultAdpater(val epData: List<epData?>?,val onMainClick : (epData, position: Int) -> Unit,val onPlayClick : (epData, position: Int) -> Unit,val onDownloadClick : (epData) -> Unit) : RecyclerView.Adapter<EpResultAdpater.MyViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.raw_ep_result, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return epData!!.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.apply {
            val data = epData!![position]
        textTitle.text = data!!.name
            mainPlay.setOnClickListener { onMainClick(data,position) }
            Playicon.setOnClickListener { onPlayClick(data,position) }
            download.setOnClickListener { onDownloadClick(data) }
        }
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainPlay = itemView.findViewById<View>(R.id.mainPlay)
        val Playicon = itemView.findViewById<ImageView>(R.id.play)
        val textTitle = itemView.findViewById<TextView>(R.id.txt_name)
        val download = itemView.findViewById<Button>(R.id.download)
    }
}
