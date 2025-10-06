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
import com.example.daily_photo_day.utils.ImageCacheHelper

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
        supportActionBar?.hide()

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbarTitle.text = "Ежедневный фотодневник"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        updateSortMenuCheckedState(menu)

        setupSearchView(menu)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        updateSortMenuCheckedState(menu)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupSearchView(menu: Menu) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as? android.widget.SearchView

        searchView?.let { sv ->
            if (!currentQuery.isNullOrBlank()) {
                searchItem.expandActionView()
                sv.setQuery(currentQuery, false)
            }

            sv.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    currentQuery = query
                    performSearch()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    currentQuery = newText
                    if (newText.isNullOrBlank()) {
                        filterAndSortPosts()
                    } else {
                        performSearch()
                    }
                    return true
                }
            })

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
        menu.findItem(R.id.action_sort_date_desc)?.isChecked = false
        menu.findItem(R.id.action_sort_date_asc)?.isChecked = false
        menu.findItem(R.id.action_sort_title_asc)?.isChecked = false
        menu.findItem(R.id.action_sort_title_desc)?.isChecked = false

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
        performSearch()

        invalidateOptionsMenu()
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

                    if (posts.isNotEmpty()) {
                        val imageUris = posts.map { it.imageUri }
                        ImageCacheHelper.preloadImages(this@MainActivity, imageUris)
                    }
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
        binding.bottomNavigation.selectedItemId = R.id.navigation_home
    }
}