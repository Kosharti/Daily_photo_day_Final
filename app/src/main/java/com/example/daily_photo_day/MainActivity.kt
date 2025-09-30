package com.example.daily_photo_day

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.daily_photo_day.databinding.ActivityMainBinding
import com.example.daily_photo_day.ui.AddEditPostActivity
import com.example.daily_photo_day.ui.CalendarActivity
import com.example.daily_photo_day.ui.PostDetailActivity
import com.example.daily_photo_day.ui.SettingsActivity
import com.example.daily_photo_day.ui.adapter.PhotoPostAdapter
import com.example.daily_photo_day.utils.SearchFilterHelper
import com.example.daily_photo_day.viewmodel.PhotoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: PhotoViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PhotoPostAdapter
    private var currentQuery: String? = null
    private var currentSortType = SearchFilterHelper.SortType.DATE_DESC

    private val PREFS_NAME = "PhotoDiaryPrefs"
    private val SORT_TYPE_KEY = "sort_type"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Загружаем сохраненный тип сортировки
        loadSortType()

        setupCustomToolbar()
        viewModel = ViewModelProvider(this)[PhotoViewModel::class.java]
        setupRecyclerView()
        observePosts()
        setupBottomNavigation()
    }

    private fun loadSortType() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSortType = prefs.getString(SORT_TYPE_KEY, "DATE_DESC")
        currentSortType = when (savedSortType) {
            "DATE_ASC" -> SearchFilterHelper.SortType.DATE_ASC
            "TITLE_ASC" -> SearchFilterHelper.SortType.TITLE_ASC
            "TITLE_DESC" -> SearchFilterHelper.SortType.TITLE_DESC
            else -> SearchFilterHelper.SortType.DATE_DESC
        }
    }

    private fun saveSortType(sortType: SearchFilterHelper.SortType) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(SORT_TYPE_KEY, sortType.name)
        editor.apply()
    }


    private fun setupCustomToolbar() {
        // Скрываем стандартный ActionBar
        supportActionBar?.hide()

        // Устанавливаем наш Toolbar как ActionBar
        setSupportActionBar(binding.toolbar)

        // Убираем стандартный заголовок
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Настраиваем наш кастомный тулбар
        binding.toolbarTitle.text = "Ежедневный фотодневник"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // Настраиваем поиск - используем безопасное приведение типов
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText
                filterAndSortPosts()
                return true
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_date_desc -> {
                currentSortType = SearchFilterHelper.SortType.DATE_DESC
                saveSortType(currentSortType)
                filterAndSortPosts()
                true
            }
            R.id.action_sort_date_asc -> {
                currentSortType = SearchFilterHelper.SortType.DATE_ASC
                saveSortType(currentSortType)
                filterAndSortPosts()
                true
            }
            R.id.action_sort_title_asc -> {
                currentSortType = SearchFilterHelper.SortType.TITLE_ASC
                saveSortType(currentSortType)
                filterAndSortPosts()
                true
            }
            R.id.action_sort_title_desc -> {
                currentSortType = SearchFilterHelper.SortType.TITLE_DESC
                saveSortType(currentSortType)
                filterAndSortPosts()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_add -> {
                    startActivity(Intent(this, AddEditPostActivity::class.java))
                    true
                }
                R.id.navigation_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun observePosts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allPosts.collectLatest { posts ->
                    filterAndSortPosts(posts)
                }
            }
        }
    }

    private fun filterAndSortPosts(posts: List<com.example.daily_photo_day.data.entity.PhotoPost>? = null) {
        lifecycleScope.launch {
            try {
                val currentPosts = posts ?: viewModel.allPosts.value ?: emptyList()
                val filtered = SearchFilterHelper.filterPosts(
                    currentPosts,
                    currentQuery,
                    currentSortType
                )
                adapter.submitList(filtered)

                // Обновляем UI состояния
                updateEmptyState(filtered)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.textEmpty.visibility = View.VISIBLE
                binding.textEmpty.text = "Ошибка загрузки данных"
            }
        }
    }

    private fun updateEmptyState(posts: List<com.example.daily_photo_day.data.entity.PhotoPost>) {
        if (posts.isEmpty()) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.textEmpty.text = if (!currentQuery.isNullOrBlank()) {
                "По запросу \"$currentQuery\" ничего не найдено"
            } else {
                "Нет фотографий\nДобавьте первую фотографию!"
            }
        } else {
            binding.textEmpty.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Всегда выделяем "Галерея" при возврате в MainActivity
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }
}