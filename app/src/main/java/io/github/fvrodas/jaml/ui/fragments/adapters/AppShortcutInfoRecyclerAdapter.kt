package io.github.fvrodas.jaml.ui.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.ItemActivityInfoBinding
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.model.AppShortcutInfo

class AppShortcutInfoRecyclerAdapter(val color: Int?) :
    RecyclerView.Adapter<AppShortcutInfoRecyclerAdapter.AppInfoViewHolder>() {

    private val resultDataSet: ArrayList<AppShortcutInfo> = ArrayList()
    private val dataSet: ArrayList<AppShortcutInfo> = ArrayList()

    var onItemPressed: (appShortcutInfo: AppShortcutInfo) -> Unit = {}

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppShortcutInfoRecyclerAdapter.AppInfoViewHolder {
        val binding = DataBindingUtil.inflate<ItemActivityInfoBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_activity_info,
            parent,
            false
        )
        return AppInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppShortcutInfoRecyclerAdapter.AppInfoViewHolder, position: Int) {
        val item = resultDataSet[holder.adapterPosition]
        holder.binding.label = item.label
        holder.binding.icon = item.icon
        holder.binding.color = color
        holder.itemView.setOnClickListener {
            onItemPressed(item)
        }
    }

    fun updateDataSet(newList: ArrayList<AppShortcutInfo> ) {
        dataSet.clear()
        dataSet.addAll(newList)
        resultDataSet.clear()
        resultDataSet.addAll(newList)
        notifyDataSetChanged()
    }

    fun filterDataSet(query: String) {
        resultDataSet.clear()
        resultDataSet.addAll(dataSet.filter { it.label.contains(query, ignoreCase = true) })
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = resultDataSet.size

    inner class AppInfoViewHolder(val binding: ItemActivityInfoBinding) :
        RecyclerView.ViewHolder(binding.root)
}