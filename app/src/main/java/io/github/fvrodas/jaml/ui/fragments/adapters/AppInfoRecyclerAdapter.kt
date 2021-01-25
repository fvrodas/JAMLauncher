package io.github.fvrodas.jaml.ui.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.ItemActivityInfoBinding
import io.github.fvrodas.jaml.model.AppInfo

class AppInfoRecyclerAdapter() :
    RecyclerView.Adapter<AppInfoRecyclerAdapter.AppInfoViewHolder>() {

    private val resultDataSet: ArrayList<AppInfo> = ArrayList()
    private val dataSet: ArrayList<AppInfo> = ArrayList()

    var onItemPressed: (appInfo: AppInfo) -> Unit = {}
    var onItemLongPressed: (appInfo: AppInfo) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppInfoRecyclerAdapter.AppInfoViewHolder {
        val binding = DataBindingUtil.inflate<ItemActivityInfoBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_activity_info,
            parent,
            false
        )
        return AppInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppInfoRecyclerAdapter.AppInfoViewHolder, position: Int) {
        val item = resultDataSet[holder.adapterPosition]
        holder.binding.label = item.label
        holder.itemView.setOnClickListener {
            onItemPressed(item)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongPressed(item)
            true
        }
    }

    fun updateDataSet(newList: ArrayList<AppInfo> ) {
        dataSet.clear()
        dataSet.addAll(newList)
        resultDataSet.clear()
        resultDataSet.addAll(newList)
        notifyDataSetChanged()
    }

    fun filterDataset(query: String) {
        resultDataSet.clear()
        resultDataSet.addAll(dataSet.filter { it.label.contains(query, ignoreCase = true) })
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = resultDataSet.size

    inner class AppInfoViewHolder(val binding: ItemActivityInfoBinding) :
        RecyclerView.ViewHolder(binding.root)
}