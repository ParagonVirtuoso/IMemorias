package com.github.ParagonVirtuoso.memorias.presentation.memory

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.databinding.ItemMemoryBinding
import com.github.ParagonVirtuoso.memorias.domain.model.Memory
import java.text.SimpleDateFormat
import java.util.Locale

class MemoriesAdapter(
    private val onMemoryClick: (Memory) -> Unit,
    private val onEditClick: (Memory) -> Unit,
    private val onDeleteClick: (Memory) -> Unit
) : ListAdapter<Memory, MemoriesAdapter.MemoryViewHolder>(MemoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val binding = ItemMemoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemoryViewHolder(
        private val binding: ItemMemoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(memory: Memory) {
            binding.apply {
                titleTextView.text = memory.videoTitle
                dateTimeTextView.text = formatDateTime(memory.notificationTime)

                Glide.with(thumbnailImageView)
                    .load(memory.videoThumbnail)
                    .centerCrop()
                    .into(thumbnailImageView)

                root.setOnClickListener {
                    onMemoryClick(memory)
                }

                menuButton.setOnClickListener { view ->
                    showPopupMenu(view, memory)
                }
            }
        }

        private fun showPopupMenu(view: android.view.View, memory: Memory) {
            PopupMenu(view.context, view).apply {
                inflate(R.menu.menu_memory_item)
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_edit -> {
                            onEditClick(memory)
                            true
                        }
                        R.id.action_delete -> {
                            onDeleteClick(memory)
                            true
                        }
                        else -> false
                    }
                }
                show()
            }
        }

        private fun formatDateTime(date: java.util.Date): String {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale.getDefault())
            return dateFormat.format(date)
        }
    }
}

private class MemoryDiffCallback : DiffUtil.ItemCallback<Memory>() {
    override fun areItemsTheSame(oldItem: Memory, newItem: Memory): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Memory, newItem: Memory): Boolean {
        return oldItem == newItem
    }
} 