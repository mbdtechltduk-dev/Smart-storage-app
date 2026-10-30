package com.appzee.smartstorage.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.appzee.smartstorage.R
import com.appzee.smartstorage.adapters.FileListAdapter
import com.appzee.smartstorage.ads.AdManager
import com.appzee.smartstorage.models.FileModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

abstract class BaseFileActivity : AppCompatActivity() {

    protected lateinit var adapter: FileListAdapter
    protected abstract val activityTitle: String
    protected abstract val emptyMessage: String
    protected abstract val showDateInSubtitle: Boolean

    private lateinit var toolbar: Toolbar
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: View
    private lateinit var emptyState: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var selectionBar: LinearLayout
    private lateinit var tvSelectionCount: TextView
    private lateinit var bannerAdContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        toolbar = findViewById(R.id.toolbar)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyState = findViewById(R.id.emptyState)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        selectionBar = findViewById(R.id.selectionBar)
        tvSelectionCount = findViewById(R.id.tvSelectionCount)
        bannerAdContainer = findViewById(R.id.bannerAdContainer)

        setSupportActionBar(toolbar)
        supportActionBar?.title = activityTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        tvEmptyState.text = emptyMessage
        recyclerView.layoutManager = LinearLayoutManager(this)

        AdManager.loadBannerAd(this, bannerAdContainer)

        setupSelectionButtons()
        loadData()
    }

    private fun setupSelectionButtons() {
        findViewById<View>(R.id.btnDelete).setOnClickListener { confirmDelete() }
        findViewById<View>(R.id.btnShare).setOnClickListener { shareSelected() }
    }

    protected fun showFiles(files: List<FileModel>) {
        progressBar.visibility = View.GONE
        if (files.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        adapter = FileListAdapter(
            files.toMutableList(),
            showDate = showDateInSubtitle,
            onItemClick = { file ->
                val intent = Intent(this, FileDetailActivity::class.java)
                intent.putExtra(FileDetailActivity.EXTRA_FILE_PATH, file.path)
                intent.putExtra(FileDetailActivity.EXTRA_FILE_NAME, file.name)
                intent.putExtra(FileDetailActivity.EXTRA_FILE_SIZE, file.size)
                intent.putExtra(FileDetailActivity.EXTRA_FILE_DATE, file.lastModified)
                intent.putExtra(FileDetailActivity.EXTRA_FILE_CATEGORY, file.category.name)
                startActivity(intent)
            },
            onSelectionChanged = { count ->
                if (count > 0) {
                    selectionBar.visibility = View.VISIBLE
                    tvSelectionCount.text = "$count selected"
                } else {
                    selectionBar.visibility = View.GONE
                }
            }
        )
        recyclerView.adapter = adapter
    }

    protected fun showLoading() {
        progressBar.visibility = View.VISIBLE
        emptyState.visibility = View.GONE
        recyclerView.visibility = View.GONE
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { onBackPressedDispatcher.onBackPressed(); true }
            R.id.action_select_all -> { adapter.selectAll(); true }
            R.id.sort_by_size -> { adapter.sortBySize(); recyclerView.adapter = adapter; true }
            R.id.sort_by_date -> { adapter.sortByDate(); recyclerView.adapter = adapter; true }
            R.id.sort_by_name -> { adapter.sortByName(); recyclerView.adapter = adapter; true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_file_list, menu)
        return true
    }

    private fun confirmDelete() {
        val selected = adapter.getSelectedFiles()
        if (selected.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("Delete Files")
            .setMessage("Delete ${selected.size} file(s)? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteFiles(selected) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteFiles(files: List<FileModel>) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                files.forEach { File(it.path).delete() }
            }
            adapter.clearSelection()
            com.google.android.material.snackbar.Snackbar.make(
                recyclerView,
                "${files.size} file(s) deleted",
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareSelected() {
        val selected = adapter.getSelectedFiles()
        if (selected.isEmpty()) return
        val uris = ArrayList(selected.map { Uri.fromFile(File(it.path)) })
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
        startActivity(Intent.createChooser(intent, "Share Files"))
    }

    abstract fun loadData()
}
