package com.appzee.smartstorage.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.appzee.smartstorage.ads.AdManager
import com.appzee.smartstorage.databinding.ActivityAnalyzeBinding
import com.appzee.smartstorage.utils.ScanRepository
import com.appzee.smartstorage.utils.StorageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AnalyzeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnalyzeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnalyzeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        AdManager.loadBannerAd(this, binding.bannerAdContainer)
        startScan()
    }

    private fun startScan() {
        reset()
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                StorageUtils.scanDevice(this@AnalyzeActivity) { progress, message ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.progressBar.progress = progress
                        binding.tvProgressPct.text = "$progress%"
                        binding.tvStatus.text = message
                    }
                }
            }

            ScanRepository.lastResult = result

            binding.progressBar.progress = 100
            binding.tvProgressPct.text = "100%"
            binding.tvStatus.text = "Scan complete ✓"
            binding.tvImages.text = result.images.size.toString()
            binding.tvVideos.text = result.videos.size.toString()
            binding.tvAudios.text = result.audios.size.toString()
            binding.tvDocuments.text = result.documents.size.toString()
            binding.tvOthers.text = result.others.size.toString()
            binding.tvTotal.text = result.totalFiles.toString()
            binding.tvTotalSize.text = StorageUtils.formatSize(result.totalSize)
            binding.btnViewReport.isEnabled = true
            binding.btnViewReport.setOnClickListener {
                AdManager.showInterstitial(this@AnalyzeActivity) {
                    startActivity(Intent(this@AnalyzeActivity, StorageReportActivity::class.java))
                }
            }
        }
    }

    private fun reset() {
        binding.progressBar.progress = 0
        binding.tvProgressPct.text = "0%"
        binding.tvStatus.text = "Preparing…"
        listOf(binding.tvImages, binding.tvVideos, binding.tvAudios,
            binding.tvDocuments, binding.tvOthers, binding.tvTotal).forEach { it.text = "—" }
        binding.tvTotalSize.text = "—"
        binding.btnViewReport.isEnabled = false
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed(); return true
    }
}
