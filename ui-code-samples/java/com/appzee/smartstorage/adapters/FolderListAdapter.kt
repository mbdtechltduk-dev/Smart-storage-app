package com.appzee.smartstorage.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.appzee.smartstorage.R
import java.io.File

class FolderListAdapter(private val folders: List<File>) :
    RecyclerView.Adapter<FolderListAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivFileIcon)
        val name: TextView = view.findViewById(R.id.tvFileName)
        val path: TextView = view.findViewById(R.id.tvFilePath)
        val size: TextView = view.findViewById(R.id.tvFileSize)
        val date: TextView = view.findViewById(R.id.tvFileDate)
        val checkbox: CheckBox = view.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_file, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val folder = folders[position]
        holder.name.text = folder.name
        holder.path.text = folder.absolutePath
        holder.size.text = "Empty"
        holder.date.text = ""
        holder.checkbox.visibility = View.GONE
        holder.icon.setImageResource(R.drawable.ic_folder_empty)
        holder.icon.imageTintList = holder.itemView.context.resources
            .getColorStateList(R.color.colorOthers, holder.itemView.context.theme)
    }

    override fun getItemCount() = folders.size
}
