package com.example.daily_photo_day.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.daily_photo_day.databinding.ActivityAddEditPostBinding
import com.example.daily_photo_day.data.entity.PhotoPost
import com.example.daily_photo_day.viewmodel.PhotoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditPostBinding
    private lateinit var viewModel: PhotoViewModel
    private var postId: Long = -1
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PhotoViewModel::class.java]
        postId = intent.getLongExtra("POST_ID", -1)

        setupUI()
        if (postId != -1L) {
            loadPostData()
        }
    }

    private fun setupUI() {
        binding.buttonSelectImage.setOnClickListener {
            openImagePicker()
        }

        binding.buttonSave.setOnClickListener {
            savePost()
        }

        binding.buttonDelete.setOnClickListener {
            deletePost()
        }

        // Показываем кнопку удаления только при редактировании
        binding.buttonDelete.visibility = if (postId != -1L) View.VISIBLE else View.GONE
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    private fun loadPostData() {
        lifecycleScope.launch {
            val post = viewModel.getPostById(postId)
            post?.let {
                binding.editTextTitle.setText(it.title)
                binding.editTextDescription.setText(it.description)
                binding.editTextLocation.setText(it.location)

                selectedImageUri = Uri.parse(it.imageUri)
                Glide.with(this@AddEditPostActivity)
                    .load(selectedImageUri)
                    .into(binding.imagePreview)
            }
        }
    }

    private fun savePost() {
        val title = binding.editTextTitle.text.toString().trim()
        val description = binding.editTextDescription.text.toString().trim()
        val location = binding.editTextLocation.text.toString().trim()

        if (title.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "Заполните заголовок и выберите изображение", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val currentPost = if (postId != -1L) viewModel.getPostById(postId) else null

            val post = PhotoPost(
                id = if (postId != -1L) postId else 0,
                imageUri = selectedImageUri.toString(),
                title = title,
                description = description,
                location = location,
                date = currentPost?.date ?: getCurrentDate()
            )

            if (postId != -1L) {
                viewModel.updatePost(post)
                Toast.makeText(this@AddEditPostActivity, "Пост обновлен", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addPost(post)
                Toast.makeText(this@AddEditPostActivity, "Пост добавлен", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun deletePost() {
        lifecycleScope.launch {
            val post = viewModel.getPostById(postId)
            post?.let {
                viewModel.deletePost(it)
                Toast.makeText(this@AddEditPostActivity, "Пост удален", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            Glide.with(this)
                .load(selectedImageUri)
                .into(binding.imagePreview)
        }
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1001
    }
}