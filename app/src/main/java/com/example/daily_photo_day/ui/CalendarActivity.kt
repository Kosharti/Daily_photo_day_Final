package com.example.daily_photo_day.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
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
        clearPosts()
    }

    private fun setupUI() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
            loadPostsForDate(selectedDate)
        }

        // Кнопка для быстрого перехода к выбору года
        binding.buttonSelectYear.setOnClickListener {
            showYearSelectionDialog()
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun showYearSelectionDialog() {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (currentYear - 10..currentYear + 10).toList().reversed()

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Выберите год")

        val yearsArray = years.map { it.toString() }.toTypedArray()

        builder.setItems(yearsArray) { _, which ->
            val selectedYear = years[which]
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
            }
            binding.calendarView.date = calendar.timeInMillis
            // При смене года очищаем посты
            clearPosts()
        }

        builder.setNegativeButton("Отмена", null)
        builder.show()
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

                // Простая и понятная надпись
                if (posts.isNotEmpty()) {
                    binding.textSelectedDate.text = "Снимки за $date (${posts.size})"
                    binding.textEmpty.visibility = View.GONE
                } else {
                    binding.textSelectedDate.text = "Снимки за $date"
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = "На выбранную дату снимков нет"
                }
            }
        }
    }

    private fun clearPosts() {
        adapter.submitList(emptyList())
        binding.textEmpty.visibility = View.VISIBLE
        binding.textEmpty.text = "Выберите дату для просмотра снимков"
        binding.textSelectedDate.text = "Выберите дату для просмотра снимков"
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(System.currentTimeMillis())
    }
}