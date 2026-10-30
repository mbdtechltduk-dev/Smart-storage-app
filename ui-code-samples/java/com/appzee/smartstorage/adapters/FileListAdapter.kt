package com.appzee.smartstorage.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appzee.smartstorage.R
import com.appzee.smartstorage.models.FileCategory
import com.appzee.smartstorage.models.FileModel
import com.appzee.smartstorage.utils.StorageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileListAdapter(
    private val files: MutableList<FileModel>,
    private val showDate: Boolean = false,
    private val onItemClick: ((FileModel) -> Unit)? = null,
    private val onSelectionChanged: ((Int) -> Unit)? = null
) : RecyclerView.Adapter<FileListAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val selectedPositions = mutableSetOf<Int>()
    var selectionMode = false
        private set

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFileIcon: ImageView = view.findViewById(R.id.ivFileIcon)
        val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        val tvFilePath: TextView = view.findViewById(R.id.tvFilePath)
        val tvFileSize: TextView = view.findViewById(R.id.tvFileSize)
        val tvFileDate: TextView = view.findViewById(R.id.tvFileDate)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.tvFileName.text = file.name
        holder.tvFileSize.text = StorageUtils.formatSize(file.size)
        holder.tvFilePath.text = if (showDate) dateFormat.format(Date(file.lastModified)) else file.path
        holder.tvFileDate.text = dateFormat.format(Date(file.lastModified))

        holder.ivFileIcon.setImageResource(getCategoryIcon(file.category))
        holder.ivFileIcon.imageTintList = holder.itemView.context.resources
            .getColorStateList(getCategoryColor(file.category), holder.itemView.context.theme)

        holder.checkbox.visibility = if (selectionMode) View.VISIBLE else View.GONE
        holder.checkbox.isChecked = position in selectedPositions

        holder.itemView.isSelected = position in selectedPositions
        holder.itemView.alpha = if (selectionMode && position !in selectedPositions) 0.8f else 1f

        holder.itemView.setOnLongClickListener {
            if (!selectionMode) {
                selectionMode = true
                toggleSelection(position)
                notifyDataSetChanged()
            }
            true
        }

        holder.itemView.setOnClickListener {
            if (selectionMode) {
                toggleSelection(position)
                notifyItemChanged(position)
            } else {
                onItemClick?.invoke(file)
            }
        }

        holder.checkbox.setOnClickListener {
            toggleSelection(position)
            notifyItemChanged(position)
        }
    }

    private fun toggleSelection(position: Int) {
        if (position in selectedPositions) selectedPositions.remove(position)
        else selectedPositions.add(position)
        onSelectionChanged?.invoke(selectedPositions.size)
        if (selectedPositions.isEmpty()) {
            selectionMode = false
            notifyDataSetChanged()
        }
    }

    fun getSelectedFiles(): List<FileModel> = selectedPositions.map { files[it] }

    fun clearSelection() {
        selectedPositions.clear()
        selectionMode = false
        notifyDataSetChanged()
        onSelectionChanged?.invoke(0)
    }

    fun selectAll() {
        selectedPositions.clear()
        files.indices.forEach { selectedPositions.add(it) }
        notifyDataSetChanged()
        onSelectionChanged?.invoke(selectedPositions.size)
    }

    fun sortBySize() {
        files.sortByDescending { it.size }
        clearSelection()
    }

    fun sortByDate() {
        files.sortByDescending { it.lastModified }
        clearSelection()
    }

    fun sortByName() {
        files.sortBy { it.name.lowercase() }
        clearSelection()
    }

    override fun getItemCount() = files.size

    private fun getCategoryIcon(cat: FileCategory) = when (cat) {
        FileCategory.IMAGE -> R.drawable.ic_analyze
        FileCategory.VIDEO -> R.drawable.ic_recent
        FileCategory.AUDIO -> R.drawable.ic_storage
        FileCategory.DOCUMENT -> R.drawable.ic_report
        FileCategory.OTHER -> R.drawable.ic_large_files
    }

    private fun getCategoryColor(cat: FileCategory) = when (cat) {
        FileCategory.IMAGE -> R.color.colorImages
        FileCategory.VIDEO -> R.color.colorVideos
        FileCategory.AUDIO -> R.color.colorAudio
        FileCategory.DOCUMENT -> R.color.colorDocuments
        FileCategory.OTHER -> R.color.colorOthers
    }
}
