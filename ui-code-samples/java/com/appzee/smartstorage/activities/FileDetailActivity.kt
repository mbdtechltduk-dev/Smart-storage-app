package com.appzee.smartstorage.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.appzee.smartstorage.R
import com.appzee.smartstorage.databinding.ActivityFileDetailBinding
import com.appzee.smartstorage.models.FileCategory
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFileDetailBinding
    private lateinit var filePath: String

    companion object {
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_FILE_NAME = "file_name"
        const val EXTRA_FILE_SIZE = "file_size"
        const val EXTRA_FILE_DATE = "file_date"
        const val EXTRA_FILE_CATEGORY = "file_category"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFileDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        filePath = intent.getStringExtra(EXTRA_FILE_PATH) ?: ""
        val name = intent.getStringExtra(EXTRA_FILE_NAME) ?: ""
        val size = intent.getLongExtra(EXTRA_FILE_SIZE, 0L)
        val date = intent.getLongExtra(EXTRA_FILE_DATE, 0L)
        val categoryStr = intent.getStringExtra(EXTRA_FILE_CATEGORY) ?: "OTHER"
        val category = try { FileCategory.valueOf(categoryStr) } catch (e: Exception) { FileCategory.OTHER }

        val ext = name.substringAfterLast('.', "").lowercase()
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
        val dateStr = SimpleDateFormat("MMM dd, yyyy  HH:mm", Locale.getDefault()).format(Date(date))
        val sizeFormatted = formatSizeDetail(size)

        binding.tvFileName.text = name
        binding.tvFileSize.text = sizeFormatted
        binding.tvFileType.text = "${ext.uppercase()} — $mime"
        binding.tvFileDate.text = dateStr
        binding.tvFilePath.text = filePath

        val iconRes = when (category) {
            FileCategory.IMAGE -> R.drawable.ic_analyze
            FileCategory.VIDEO -> R.drawable.ic_recent
            FileCategory.AUDIO -> R.drawable.ic_storage
            FileCategory.DOCUMENT -> R.drawable.ic_report
            FileCategory.OTHER -> R.drawable.ic_large_files
        }
        val colorRes = when (category) {
            FileCategory.IMAGE -> R.color.colorImages
            FileCategory.VIDEO -> R.color.colorVideos
            FileCategory.AUDIO -> R.color.colorAudio
            FileCategory.DOCUMENT -> R.color.colorDocuments
            FileCategory.OTHER -> R.color.colorOthers
        }
        binding.ivFileIcon.setImageResource(iconRes)
        binding.ivFileIcon.imageTintList = resources.getColorStateList(colorRes, theme)

        binding.btnOpen.setOnClickListener { openFile() }
        binding.btnShareFile.setOnClickListener { shareFile() }
    }

    private fun openFile() {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val ext = filePath.substringAfterLast('.', "").lowercase()
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            // File not openable
        }
    }

    private fun shareFile() {
        try {
            val file = File(filePath)
            val uri = FileProvider.getUriForFile(this, "$packageName.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "Share"))
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun formatSizeDetail(bytes: Long): String {
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1 -> "%.2f GB (%,d bytes)".format(gb, bytes)
            mb >= 1 -> "%.2f MB (%,d bytes)".format(mb, bytes)
            kb >= 1 -> "%.1f KB (%,d bytes)".format(kb, bytes)
            else -> "$bytes bytes"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}
