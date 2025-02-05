package com.github.ParagonVirtuoso.memorias.presentation.video

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.ParagonVirtuoso.memorias.databinding.ItemCommentBinding
import com.github.ParagonVirtuoso.memorias.domain.model.Comment
import com.github.ParagonVirtuoso.memorias.util.formatLikeCount
import com.github.ParagonVirtuoso.memorias.util.formatTimeAgo

class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CommentViewHolder(
        private val binding: ItemCommentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(comment: Comment) {
            binding.apply {
                authorTextView.text = comment.authorName
                commentTextView.text = comment.text
                likesTextView.text = formatLikeCount(comment.likeCount)
                timeTextView.text = formatTimeAgo(comment.publishedAt)
            }
        }
    }

    private class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
} 