package com.appzee.smartstorage.activities

import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appzee.smartstorage.R
import com.appzee.smartstorage.adapters.FolderListAdapter
import com.appzee.smartstorage.ads.AdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class EmptyFoldersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Empty Folders"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = findViewById<View>(R.id.progressBar)
        val emptyState = findViewById<LinearLayout>(R.id.emptyState)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyState)
        val banner = findViewById<LinearLayout>(R.id.bannerAdContainer)
        findViewById<LinearLayout>(R.id.selectionBar).visibility = View.GONE

        tvEmpty.text = "🎉 No empty folders found"
        recyclerView.layoutManager = LinearLayoutManager(this)
        AdManager.loadBannerAd(this, banner)

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val folders = withContext(Dispatchers.IO) { findEmptyFolders() }
            progressBar.visibility = View.GONE

            if (folders.isEmpty()) {
                emptyState.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                recyclerView.adapter = FolderListAdapter(folders)
            }
        }
    }

    private fun findEmptyFolders(): List<File> {
        val results = mutableListOf<File>()
        val roots = mutableListOf<File>()

        // External storage
        Environment.getExternalStorageDirectory()?.let { roots.add(it) }
        // Also check app-accessible external dirs
        externalMediaDirs.forEach { it?.let { d -> roots.add(d) } }

        for (root in roots) {
            scanForEmpty(root, results, depth = 0)
        }
        return results.distinctBy { it.absolutePath }.sortedBy { it.name }
    }

    private fun scanForEmpty(dir: File, results: MutableList<File>, depth: Int) {
        if (depth > 8) return  // safety cap
        if (!dir.isDirectory || !dir.canRead()) return
        if (dir.name.startsWith('.')) return  // skip hidden

        val children = dir.listFiles() ?: return
        if (children.isEmpty()) {
            results.add(dir)
            return
        }
        // Only recurse into subdirectories that are visible
        children.filter { it.isDirectory && !it.name.startsWith('.') }
            .forEach { scanForEmpty(it, results, depth + 1) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}
