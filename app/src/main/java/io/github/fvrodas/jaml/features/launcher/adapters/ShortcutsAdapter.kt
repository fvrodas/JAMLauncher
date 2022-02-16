package io.github.fvrodas.jaml.features.launcher.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.databinding.ItemShortcutBinding

class ShortcutsAdapter(
    private var items: List<AppShortcutInfo>,
    private val listener: IShortcutListener
) : BaseAdapter() {

    override fun getCount(): Int = items.size

    override fun getItem(p0: Int): AppShortcutInfo = items[p0]

    override fun getItemId(p0: Int): Long = items[p0].hashCode().toLong()

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val item = getItem(p0)
        return ShortcutViewHolder.create(p2!!).apply {
            item.icon?.let {
                binding.applicationIcon.setImageBitmap(it)
            } ?: run {
                binding.applicationIcon.setImageResource(android.R.drawable.ic_menu_info_details)
            }
            binding.activityLabel.text = item.label
            binding.root.setOnClickListener {
                listener.onShortcutPressed(item)
            }
        }.itemView
    }

    fun updateAdapter(newItems: List<AppShortcutInfo>) {
        items = newItems
    }
}

interface IShortcutListener {
    fun onShortcutPressed(shortcut: AppShortcutInfo)
}

class ShortcutViewHolder(val binding: ItemShortcutBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup): ShortcutViewHolder {
            val binding = ItemShortcutBinding.inflate(LayoutInflater.from(parent.context))
            return ShortcutViewHolder(binding)
        }
    }
}