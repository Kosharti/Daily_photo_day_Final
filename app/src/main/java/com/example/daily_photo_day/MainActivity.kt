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

        // Восстанавливаем сохраненную позицию фильтра
        updateSortMenuCheckedState(menu)

        // Настраиваем поиск
        setupSearchView(menu)

        return true
    }

    private fun setupSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? android.widget.SearchView

        searchView?.let { sv ->
            // Устанавливаем текущий запрос если есть
            if (!currentQuery.isNullOrBlank()) {
                searchItem.expandActionView()
                sv.setQuery(currentQuery, false)
            }

            sv.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    // Обработка нажатия кнопки поиска на клавиатуре
                    currentQuery = query
                    performSearch()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    // Обработка изменения текста в реальном времени
                    currentQuery = newText
                    if (newText.isNullOrBlank()) {
                        // Если текст пустой, показываем все посты
                        filterAndSortPosts()
                    } else {
                        // Если есть текст, выполняем поиск
                        performSearch()
                    }
                    return true
                }
            })

            // Обработка закрытия поиска
            searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
                override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                    return true
                }

                override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                    currentQuery = null
                    filterAndSortPosts()
                    return true
                }
            })
        }
    }

    private fun updateSortMenuCheckedState(menu: Menu) {
        // Сначала снимаем все отметки
        menu.findItem(R.id.action_sort_date_desc)?.isChecked = false
        menu.findItem(R.id.action_sort_date_asc)?.isChecked = false
        menu.findItem(R.id.action_sort_title_asc)?.isChecked = false
        menu.findItem(R.id.action_sort_title_desc)?.isChecked = false

        // Затем устанавливаем отметку на текущем фильтре
        when (currentSortType) {
            SearchFilterHelper.SortType.DATE_DESC -> menu.findItem(R.id.action_sort_date_desc)?.isChecked = true
            SearchFilterHelper.SortType.DATE_ASC -> menu.findItem(R.id.action_sort_date_asc)?.isChecked = true
            SearchFilterHelper.SortType.TITLE_ASC -> menu.findItem(R.id.action_sort_title_asc)?.isChecked = true
            SearchFilterHelper.SortType.TITLE_DESC -> menu.findItem(R.id.action_sort_title_desc)?.isChecked = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort_date_desc -> {
                setSortType(SearchFilterHelper.SortType.DATE_DESC)
                true
            }
            R.id.action_sort_date_asc -> {
                setSortType(SearchFilterHelper.SortType.DATE_ASC)
                true
            }
            R.id.action_sort_title_asc -> {
                setSortType(SearchFilterHelper.SortType.TITLE_ASC)
                true
            }
            R.id.action_sort_title_desc -> {
                setSortType(SearchFilterHelper.SortType.TITLE_DESC)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setSortType(sortType: SearchFilterHelper.SortType) {
        currentSortType = sortType
        saveSortType(sortType)
        performSearch() // Применяем сортировку к текущим результатам
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
                    // Сохраняем все посты и применяем текущие фильтры
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
                updateEmptyState(filtered)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.textEmpty.visibility = View.VISIBLE
                binding.textEmpty.text = "Ошибка загрузки данных"
            }
        }
    }

    private fun performSearch() {
        lifecycleScope.launch {
            try {
                val allPosts = viewModel.allPosts.value ?: emptyList()
                val filtered = SearchFilterHelper.filterPosts(
                    allPosts,
                    currentQuery,
                    currentSortType
                )
                adapter.submitList(filtered)
                updateEmptyState(filtered)
            } catch (e: Exception) {
                e.printStackTrace()
                binding.textEmpty.visibility = View.VISIBLE
                binding.textEmpty.text = "Ошибка поиска"
            }
        }
    }

    private fun updateEmptyState(posts: List<com.example.daily_photo_day.data.entity.PhotoPost>) {
        if (posts.isEmpty()) {
            binding.textEmpty.visibility = View.VISIBLE
            binding.textEmpty.text = if (!currentQuery.isNullOrBlank()) {
                "По запросu \"$currentQuery\" ничего не найдено"
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