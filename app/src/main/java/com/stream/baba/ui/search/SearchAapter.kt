package com.stream.baba.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.stream.baba.R
import com.stream.baba.data.HomePageList
import com.stream.baba.data.HomeViewModel

class SearchAapter(
    private val context: Context,
    private val parentDataList: List<HomePageList?>?,
    private val viewModel: HomeViewModel,
    private val cardClickCallback: () -> Unit,
    private val moreInfoClickCallback: (HomePageList?) -> Unit
) :
    RecyclerView.Adapter<SearchAapter.SearchViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.homepage_parent, parent, false)
        return SearchViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val item = parentDataList!![position]
        holder.bindData(context,item!! ,viewModel,moreInfoClickCallback,cardClickCallback)
    }


    override fun getItemCount(): Int {
        return parentDataList!!.size
    }

    class SearchViewHolder constructor(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val childRecyclerView: RecyclerView =
            itemView.findViewById(R.id.home_child_recyclerview)
        private var title: TextView = itemView.findViewById(R.id.home_child_more_info)

        fun bindData(
            context: Context,
            homePageList: HomePageList,
            viewModel: HomeViewModel,
            moreInfoClickCallback: (HomePageList?) -> Unit,
            cardClickCallback: () -> Unit
        ) {
            title.text = homePageList.name
            val childAdapter = SearchHomeAdapter(context,homePageList.list,viewModel){
                cardClickCallback()
            }
            childRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = childAdapter
            }
            title.setOnClickListener {
                moreInfoClickCallback(homePageList)
            }
        }
    }
}
