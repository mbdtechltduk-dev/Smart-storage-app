package com.appzee.smartstorage.activities

import com.appzee.smartstorage.utils.ScanRepository

class CategoryFilesActivity : BaseFileActivity() {
    override val activityTitle get() = ScanRepository.filteredCategory.lowercase()
        .replaceFirstChar { it.uppercase() } + " Files"
    override val emptyMessage = "No files found in this category"
    override val showDateInSubtitle = false

    override fun loadData() {
        showFiles(ScanRepository.filteredFiles)
    }
}
