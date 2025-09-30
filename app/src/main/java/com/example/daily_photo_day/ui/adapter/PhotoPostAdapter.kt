package com.example.daily_photo_day.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.daily_photo_day.data.entity.PhotoPost
import com.example.daily_photo_day.databinding.ItemPhotoPostBinding

class PhotoPostAdapter(
    private val onItemClick: (PhotoPost) -> Unit
) : ListAdapter<PhotoPost, PhotoPostAdapter.PhotoPostViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPostViewHolder {
        val binding = ItemPhotoPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoPostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoPostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }

    inner class PhotoPostViewHolder(
        private val binding: ItemPhotoPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(adapterPosition))
                }
            }
        }

        fun bind(post: PhotoPost) {
            binding.textTitle.text = post.title
            binding.textDate.text = post.date

            Glide.with(binding.root.context)
                .load(post.imageUri)
                .into(binding.imagePost)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<PhotoPost>() {
        override fun areItemsTheSame(oldItem: PhotoPost, newItem: PhotoPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PhotoPost, newItem: PhotoPost): Boolean {
            return oldItem == newItem
        }
    }
}