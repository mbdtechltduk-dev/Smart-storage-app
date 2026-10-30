package com.appzee.smartstorage.activities

import androidx.lifecycle.lifecycleScope
import com.appzee.smartstorage.utils.ScanRepository
import com.appzee.smartstorage.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RecentFilesActivity : BaseFileActivity() {
    override val activityTitle = "Recent Files"
    override val emptyMessage = "No recent files found"
    override val showDateInSubtitle = true

    override fun loadData() {
        showLoading()
        lifecycleScope.launch {
            val result = ScanRepository.lastResult ?: withContext(Dispatchers.IO) {
                StorageUtils.scanDevice(this@RecentFilesActivity) { _, _ -> }
                    .also { ScanRepository.lastResult = it }
            }
            val files = withContext(Dispatchers.Default) {
                StorageUtils.getRecentFiles(result, 100)
            }
            showFiles(files)
        }
    }
}
