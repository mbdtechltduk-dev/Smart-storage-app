package com.appzee.smartstorage.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appzee.smartstorage.ads.AdManager
import com.appzee.smartstorage.databinding.ActivityStorageReportBinding
import com.appzee.smartstorage.models.StorageScanResult
import com.appzee.smartstorage.utils.ScanRepository
import com.appzee.smartstorage.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StorageReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStorageReportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStorageReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        AdManager.loadBannerAd(this, binding.bannerAdContainer)

        val cached = ScanRepository.lastResult
        if (cached != null) displayReport(cached) else loadAndDisplay()
    }

    private fun loadAndDisplay() {
        binding.tvLoading.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                StorageUtils.scanDevice(this@StorageReportActivity) { _, _ -> }
            }
            ScanRepository.lastResult = result
            binding.tvLoading.visibility = View.GONE
            displayReport(result)
        }
    }

    private fun displayReport(result: StorageScanResult) {
        val total = result.totalSize.coerceAtLeast(1L)
        fun pct(s: Long) = (s * 100 / total).toInt().coerceIn(0, 100)

        val imgSz = result.images.sumOf { it.size }
        val vidSz = result.videos.sumOf { it.size }
        val audSz = result.audios.sumOf { it.size }
        val docSz = result.documents.sumOf { it.size }
        val othSz = result.others.sumOf { it.size }

        binding.tvTotalFiles.text = result.totalFiles.toString()
        binding.tvTotalSize.text = StorageUtils.formatSize(result.totalSize)

        binding.tvImageCount.text = "${result.images.size} files"
        binding.tvImageSize.text = StorageUtils.formatSize(imgSz)
        binding.tvImagePct.text = "${pct(imgSz)}%"
        binding.progressImages.progress = pct(imgSz)

        binding.tvVideoCount.text = "${result.videos.size} files"
        binding.tvVideoSize.text = StorageUtils.formatSize(vidSz)
        binding.tvVideoPct.text = "${pct(vidSz)}%"
        binding.progressVideos.progress = pct(vidSz)

        binding.tvAudioCount.text = "${result.audios.size} files"
        binding.tvAudioSize.text = StorageUtils.formatSize(audSz)
        binding.tvAudioPct.text = "${pct(audSz)}%"
        binding.progressAudios.progress = pct(audSz)

        binding.tvDocCount.text = "${result.documents.size} files"
        binding.tvDocSize.text = StorageUtils.formatSize(docSz)
        binding.tvDocPct.text = "${pct(docSz)}%"
        binding.progressDocs.progress = pct(docSz)

        binding.tvOtherCount.text = "${result.others.size} files"
        binding.tvOtherSize.text = StorageUtils.formatSize(othSz)
        binding.tvOtherPct.text = "${pct(othSz)}%"
        binding.progressOthers.progress = pct(othSz)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}
