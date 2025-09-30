package com.example.daily_photo_day

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.daily_photo_day.databinding.ActivityMainBinding
import com.example.daily_photo_day.ui.AddEditPostActivity
import com.example.daily_photo_day.ui.CalendarActivity
import com.example.daily_photo_day.ui.PostDetailActivity
import com.example.daily_photo_day.ui.adapter.PhotoPostAdapter
import com.example.daily_photo_day.viewmodel.PhotoViewModel
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import com.example.daily_photo_day.ui.SettingsActivity

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: PhotoViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PhotoPostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[PhotoViewModel::class.java]
        setupRecyclerView()
        observePosts()
        setupBottomNavigation()
        /*
        binding.fabAddPost.setOnClickListener {
            val intent = Intent(this, AddEditPostActivity::class.java)
            startActivity(intent)
        }
        */
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
                R.id.navigation_home -> {
                    // Уже на главной
                    true
                }
                R.id.navigation_add -> {
                    val intent = Intent(this, AddEditPostActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_calendar -> {
                    val intent = Intent(this, CalendarActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun observePosts() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allPosts.collect { posts ->
                    adapter.submitList(posts)
                }
            }
        }
    }
}