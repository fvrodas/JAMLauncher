package io.github.fvrodas.jaml.features.launcher.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo

class AppShortcutInfoRecyclerAdapter(val color: Int?, private val listener: IAppShortcutInfoListener) :
    ListAdapter<AppShortcutInfo, AppInfoViewHolder>(AppShortcutInfoDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AppInfoViewHolder {
        return AppInfoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: AppInfoViewHolder, position: Int) {
        val item = getItem(holder.adapterPosition)
        with(holder.binding) {
            activityLabel.text = item.label
            color?.let {
                activityLabel.setTextColor(it)
            }
            applicationIcon.setImageBitmap(item.icon)
            root.setOnClickListener {
                listener.onItemPressed(item)
            }
        }
    }
}

class AppShortcutInfoDiffCallback: DiffUtil.ItemCallback<AppShortcutInfo>() {
    override fun areItemsTheSame(oldItem: AppShortcutInfo, newItem: AppShortcutInfo): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: AppShortcutInfo, newItem: AppShortcutInfo): Boolean {
        return oldItem == newItem
    }
}

interface IAppShortcutInfoListener {
    fun onItemPressed(appShortcutInfo: AppShortcutInfo)
}