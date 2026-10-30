package com.appzee.smartstorage.activities

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.card.MaterialCardView
import com.appzee.smartstorage.R
import com.appzee.smartstorage.ads.AdManager
import com.appzee.smartstorage.databinding.ActivityMainBinding
import com.appzee.smartstorage.utils.CircularProgressView
import com.appzee.smartstorage.utils.PermissionUtils
import com.appzee.smartstorage.utils.StorageUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    data class ToolConfig(
        val iconRes: Int,
        val iconBgRes: Int,
        val iconTintRes: Int,
        val title: String,
        val desc: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupToolCards()
        updateStorageInfo()
        AdManager.loadBannerAd(this, binding.bannerAdContainer)
        AdManager.loadInterstitial(this)
    }

    override fun onResume() {
        super.onResume()
        updateStorageInfo()
    }

    private fun shortSize(bytes: Long): String {
        val gb = bytes / (1024.0 * 1024.0 * 1024.0)
        val mb = bytes / (1024.0 * 1024.0)

        return when {
            gb >= 10 -> "${gb.toInt()} GB"
            gb >= 1 -> String.format("%.1f GB", gb)
            else -> "${mb.toInt()} MB"
        }
    }


    private fun updateStorageInfo() {
        val total = StorageUtils.getTotalStorage()
        val used = StorageUtils.getUsedStorage()
        val free = StorageUtils.getFreeStorage()
        val usedPct = if (total > 0) (used * 100 / total).toInt() else 0
        val freePct = 100 - usedPct

        binding.tvTotalStorage.text = StorageUtils.formatSize(total)
        binding.tvUsedStorage.text = StorageUtils.formatSize(used)
        binding.tvTotalVal.text = shortSize(total)
        binding.tvUsedVal.text = shortSize(used)
        binding.tvFreeStorage.text = shortSize(free)
        binding.storageProgressBar.progress = usedPct
        binding.tvUsedPercent.text = "$usedPct% Used"
        binding.tvFreePercent.text = "$freePct% Free"
        binding.circularProgress.setProgress(usedPct, animate = true)
    }

    private fun setupToolCards() {
        configureToolCard(
            binding.cardAnalyze, binding.rowAnalyze.root,
            ToolConfig(R.drawable.ic_analyze, R.drawable.bg_icon_circle_analyze,
                R.color.iconTintAnalyze, "Analyze Device", "Scan all files")
        ) { launchWithPermission { startActivityForFeature(AnalyzeActivity::class.java) } }

        configureToolCard(
            binding.cardReport, binding.rowReport.root,
            ToolConfig(R.drawable.ic_report, R.drawable.bg_icon_circle_report,
                R.color.iconTintReport, "Storage Report", "Usage breakdown")
        ) { launchWithPermission { startActivityForFeature(StorageReportActivity::class.java) } }

        configureToolCard(
            binding.cardLargeFiles, binding.rowLarge.root,
            ToolConfig(R.drawable.ic_large_files, R.drawable.bg_icon_circle_large,
                R.color.iconTintLarge, "Large Files", "Files over 10 MB")
        ) { launchWithPermission { startActivityForFeature(LargeFilesActivity::class.java) } }

        configureToolCard(
            binding.cardRecentFiles, binding.rowRecent.root,
            ToolConfig(R.drawable.ic_recent, R.drawable.bg_icon_circle_recent,
                R.color.iconTintRecent, "Recent Files", "Recently modified")
        ) { launchWithPermission { startActivityForFeature(RecentFilesActivity::class.java) } }

        configureToolCard(
            binding.cardEmptyFolders, binding.rowFolders.root,
            ToolConfig(R.drawable.ic_folder_empty, R.drawable.bg_icon_circle_folders,
                R.color.iconTintFolders, "Empty Folders", "Unused directories")
        ) { launchWithPermission { startActivityForFeature(EmptyFoldersActivity::class.java) } }

        configureToolCard(
            binding.cardRecovery, binding.rowRecovery.root,
            ToolConfig(R.drawable.ic_recovery, R.drawable.bg_icon_circle_recovery,
                R.color.iconTintRecovery, "Recovery Center", "Recovery tools")
        ) { launchWithPermission { startActivityForFeature(MediaRecoveryActivity::class.java) } }
    }

    private fun configureToolCard(
        card: MaterialCardView,
        rowView: android.view.View,
        config: ToolConfig,
        onClick: () -> Unit
    ) {
        rowView.findViewById<FrameLayout>(R.id.iconContainer)
            .setBackgroundResource(config.iconBgRes)
        rowView.findViewById<ImageView>(R.id.toolIcon).apply {
            setImageResource(config.iconRes)
            imageTintList = resources.getColorStateList(config.iconTintRes, theme)
        }
        rowView.findViewById<TextView>(R.id.toolTitle).text = config.title
        rowView.findViewById<TextView>(R.id.toolDesc).text = config.desc
        card.setOnClickListener { onClick() }
    }

    private fun startActivityForFeature(cls: Class<*>) {
        startActivity(Intent(this, cls))
    }

    private fun launchWithPermission(action: () -> Unit) {
        if (PermissionUtils.hasStoragePermission(this)) {
            action()
        } else {
            ActivityCompat.requestPermissions(
                this,
                PermissionUtils.getRequiredPermissions(),
                PermissionUtils.PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onBackPressed() {
        // Show exit screen instead of closing directly
        startActivity(Intent(this, ExitActivity::class.java))
    }
}
