package com.github.ParagonVirtuoso.memorias.presentation.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.ParagonVirtuoso.memorias.R
import com.github.ParagonVirtuoso.memorias.domain.model.Video

class VideoAdapter(videos: List<Video>) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {
    var videos: List<Video> = videos
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(videos[position])
    }

    override fun getItemCount(): Int = videos.size

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvTitle)
        private val description: TextView = itemView.findViewById(R.id.tvDescription)

        fun bind(video: Video) {
            title.text = video.title
            description.text = video.description
        }
    }
} 