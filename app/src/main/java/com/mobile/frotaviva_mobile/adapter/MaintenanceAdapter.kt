package com.mobile.frotaviva_mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.model.Maintenance

class MaintenanceAdapter (private val items: List<Maintenance>)  :
    RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder>() {

    class MaintenanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val manTitulo: TextView = itemView.findViewById(R.id.manTitulo)
        val manInfo: TextView = itemView.findViewById(R.id.manInfo)
        val manDate: TextView = itemView.findViewById(R.id.manDate)
        val manStatus: TextView = itemView.findViewById(R.id.manStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.maintenance_item, parent, false)
        return MaintenanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        val maintenance = items[position]
        holder.manTitulo.text = maintenance.titulo
        holder.manInfo.text = maintenance.info
        holder.manDate.text = maintenance.dataOcorrido.toString()
    }

    override fun getItemCount() = items.size
}