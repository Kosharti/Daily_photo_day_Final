package com.example.daily_photo_day.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.daily_photo_day.databinding.ActivityCalendarBinding
import com.example.daily_photo_day.ui.adapter.PhotoPostAdapter
import com.example.daily_photo_day.viewmodel.PhotoViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCalendarBinding
    private lateinit var viewModel: PhotoViewModel
    private lateinit var adapter: PhotoPostAdapter
    private var selectedDate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PhotoViewModel::class.java]
        setupUI()
        setupRecyclerView()

        // Устанавливаем текущую дату по умолчанию
        selectedDate = getCurrentDate()
        binding.calendarView.date = System.currentTimeMillis()
        loadPostsForDate(selectedDate)
    }

    private fun setupUI() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
            loadPostsForDate(selectedDate)
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PhotoPostAdapter { post ->
            val intent = Intent(this, PostDetailActivity::class.java).apply {
                putExtra("POST_ID", post.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewPosts.adapter = adapter
        binding.recyclerViewPosts.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
    }

    private fun loadPostsForDate(date: String) {
        lifecycleScope.launch {
            viewModel.getPostsByDate(date).collect { posts ->
                adapter.submitList(posts)
                binding.textSelectedDate.text = "Снимки за $date (${posts.size})"
            }
        }
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(System.currentTimeMillis())
    }
}