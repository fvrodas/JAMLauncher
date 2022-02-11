package io.github.fvrodas.jaml.features.launcher.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.fvrodas.jaml.databinding.ItemActivityInfoBinding
import io.github.fvrodas.jaml.core.domain.entities.AppInfo

class AppInfoRecyclerAdapter(val color: Int?, private val listener: IAppInfoListener) :
    ListAdapter<AppInfo, AppInfoViewHolder>(AppInfoDiffCallback()) {

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
            notificationIndicator.visibility = View.GONE
            if (item.hasNotification) {
                notificationIndicator.imageTintList = ColorStateList.valueOf(
                    color ?: Color.WHITE
                )
                notificationIndicator.visibility = View.VISIBLE
            }

            root.setOnClickListener {
                listener.onItemPressed(item)
            }

            root.setOnLongClickListener {
                listener.onItemLongPressed(item, it)
            }
        }
    }
}

class AppInfoViewHolder(val binding: ItemActivityInfoBinding) :
    RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun create(parent: ViewGroup): AppInfoViewHolder {
            val binding =
                ItemActivityInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AppInfoViewHolder(binding)
        }
    }
}

class AppInfoDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
    override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
        return oldItem == newItem
    }
}

interface IAppInfoListener {
    fun onItemPressed(appInfo: AppInfo)
    fun onItemLongPressed(appInfo: AppInfo, viewAnchor: View): Boolean
}