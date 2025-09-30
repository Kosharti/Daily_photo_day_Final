package com.example.daily_photo_day.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.daily_photo_day.databinding.ActivityAddEditPostBinding
import com.example.daily_photo_day.data.entity.PhotoPost
import com.example.daily_photo_day.viewmodel.PhotoViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEditPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditPostBinding
    private lateinit var viewModel: PhotoViewModel
    private var postId: Long = -1
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1001
        private const val REQUEST_IMAGE_PICK = 1002
        private const val REQUEST_CAMERA_PERMISSION = 1003
    }

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
            showImageSourceDialog()
        }

        binding.buttonSave.setOnClickListener {
            savePost()
        }

        binding.buttonDelete.setOnClickListener {
            deletePost()
        }

        // Добавляем кнопку назад
        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonDelete.visibility = if (postId != -1L) View.VISIBLE else View.GONE
        // Показываем кнопку назад всегда
        binding.buttonBack.visibility = View.VISIBLE
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Сделать фото", "Выбрать из галереи", "Отмена")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Выберите источник изображения")
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> takePhotoFromCamera()
                1 -> pickImageFromGallery()
            }
        }
        builder.show()
    }

    private fun takePhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            startCamera()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(null)

        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun startCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    Toast.makeText(this, "Ошибка создания файла", Toast.LENGTH_SHORT).show()
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } ?: run {
                Toast.makeText(this, "Камера не найдена", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    // Функция для копирования файла из галереи в постоянное хранилище
    private suspend fun copyFileToInternalStorage(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExternalFilesDir(null), "gallery_${timeStamp}.jpg")

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            FileProvider.getUriForFile(
                this@AddEditPostActivity,
                "${packageName}.fileprovider",
                file
            )
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera()
                } else {
                    Toast.makeText(this, "Разрешение на камеру необходимо для съемки фото", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                date = currentPost?.date ?: getCurrentDateTime()
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

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    currentPhotoPath?.let { path ->
                        val file = File(path)
                        if (file.exists()) {
                            selectedImageUri = FileProvider.getUriForFile(
                                this,
                                "${packageName}.fileprovider",
                                file
                            )
                            Glide.with(this)
                                .load(selectedImageUri)
                                .into(binding.imagePreview)
                        }
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    val galleryUri = data?.data
                    galleryUri?.let { uri ->
                        lifecycleScope.launch {
                            val permanentUri = copyFileToInternalStorage(uri)
                            selectedImageUri = permanentUri
                            permanentUri?.let {
                                Glide.with(this@AddEditPostActivity)
                                    .load(it)
                                    .into(binding.imagePreview)
                            } ?: run {
                                Toast.makeText(this@AddEditPostActivity, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }
}