package com.example.daily_photo_day.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.daily_photo_day.databinding.ActivityPostDetailBinding
import com.example.daily_photo_day.viewmodel.PhotoViewModel
import kotlinx.coroutines.launch

class PostDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostDetailBinding
    private lateinit var viewModel: PhotoViewModel
    private var postId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PhotoViewModel::class.java]
        postId = intent.getLongExtra("POST_ID", -1)

        if (postId == -1L) {
            finish()
            return
        }

        setupUI()
        loadPostData()
    }

    private fun setupUI() {
        binding.buttonEdit.setOnClickListener {
            val intent = Intent(this, AddEditPostActivity::class.java).apply {
                putExtra("POST_ID", postId)
            }
            startActivity(intent)
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun loadPostData() {
        lifecycleScope.launch {
            val post = viewModel.getPostById(postId)
            post?.let {
                binding.textTitle.text = it.title
                binding.textDescription.text = it.description
                binding.textLocation.text = it.location
                binding.textDate.text = it.date

                Glide.with(this@PostDetailActivity)
                    .load(it.imageUri)
                    .into(binding.imagePost)
            }
        }
    }
}