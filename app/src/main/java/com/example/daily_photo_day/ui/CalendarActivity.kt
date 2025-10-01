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
    private var isDateSelected: Boolean = false
    private var lastCheckedMonthYear: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PhotoViewModel::class.java]
        setupUI()
        setupRecyclerView()

        // Устанавливаем текущую дату по умолчанию, но не загружаем посты
        selectedDate = getCurrentDate()
        binding.calendarView.date = System.currentTimeMillis()
        isDateSelected = false
        lastCheckedMonthYear = getCurrentMonthYear()
        clearPosts()

        // Запускаем проверку смены месяца
        setupRobustMonthChangeListener()
    }

    private fun setupUI() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            selectedDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(calendar.time)
            isDateSelected = true
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

    private fun setupRobustMonthChangeListener() {
        // Проверяем изменение месяца каждые 300ms
        val monthCheckRunnable = object : Runnable {
            override fun run() {
                val currentMonthYear = getCurrentMonthYear()
                if (currentMonthYear != lastCheckedMonthYear) {
                    // Месяц изменился - сбрасываем выбранную дату и очищаем посты
                    isDateSelected = false
                    clearPosts()
                    lastCheckedMonthYear = currentMonthYear
                }
                binding.calendarView.postDelayed(this, 300)
            }
        }

        binding.calendarView.postDelayed(monthCheckRunnable, 300)
    }

    private fun getCurrentMonthYear(): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = binding.calendarView.date
        }
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        return "$month-$year"
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
            // При смене года сбрасываем выбранную дату
            isDateSelected = false
            clearPosts()
            binding.textSelectedDate.text = "Выберите дату для просмотра снимков"
            lastCheckedMonthYear = getCurrentMonthYear()
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
                binding.textSelectedDate.text = "Снимки за $date (${posts.size})"

                // Показываем/скрываем текст "нет снимков"
                if (posts.isEmpty()) {
                    binding.textEmpty.visibility = View.VISIBLE
                    binding.textEmpty.text = "На $date нет снимков"
                } else {
                    binding.textEmpty.visibility = View.GONE
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