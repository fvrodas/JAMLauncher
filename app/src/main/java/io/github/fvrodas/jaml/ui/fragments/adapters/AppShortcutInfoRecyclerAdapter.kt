package io.github.fvrodas.jaml.ui.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.databinding.ItemActivityInfoBinding
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.model.AppShortcutInfo

class AppShortcutInfoRecyclerAdapter(val color: Int?) :
    RecyclerView.Adapter<AppShortcutInfoRecyclerAdapter.AppInfoViewHolder>() {

    private var dataSet: ArrayList<AppShortcutInfo> = ArrayList()

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
        val item = dataSet[holder.adapterPosition]
        holder.binding.label = item.label
        holder.binding.icon = item.icon
        holder.binding.color = color
        holder.itemView.setOnClickListener {
            onItemPressed(item)
        }
    }

    fun updateDataSet(newList: ArrayList<AppShortcutInfo>) {
        val oldList = dataSet
        dataSet = newList
        notifyChanges(newList, oldList)
    }

    private fun notifyChanges(newList: ArrayList<AppShortcutInfo>, oldList: ArrayList<AppShortcutInfo>) {
        val diffUtil = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.count()

            override fun getNewListSize(): Int = newList.count()

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].packageName == newList[newItemPosition].packageName
            }

        })
        diffUtil.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = dataSet.size

    inner class AppInfoViewHolder(val binding: ItemActivityInfoBinding) :
        RecyclerView.ViewHolder(binding.root)
}