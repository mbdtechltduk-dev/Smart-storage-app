package com.appzee.smartstorage.activities

import androidx.lifecycle.lifecycleScope
import com.appzee.smartstorage.utils.ScanRepository
import com.appzee.smartstorage.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LargeFilesActivity : BaseFileActivity() {
    override val activityTitle = "Large Files"
    override val emptyMessage = "No large files found (≥10 MB)"
    override val showDateInSubtitle = false

    override fun loadData() {
        showLoading()
        lifecycleScope.launch {
            val result = ScanRepository.lastResult ?: withContext(Dispatchers.IO) {
                StorageUtils.scanDevice(this@LargeFilesActivity) { _, _ -> }
                    .also { ScanRepository.lastResult = it }
            }
            val files = withContext(Dispatchers.Default) {
                StorageUtils.findLargeFiles(result, 10)
            }
            showFiles(files)
        }
    }
}
