package com.mobile.frotaviva_mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.databinding.MaintenanceItemBinding
import com.mobile.frotaviva_mobile.model.Maintenance

class MaintenanceAdapter (
    private var items: List<Maintenance>,
    private val onMaintenanceDone: (maintenanceId: Int) -> Unit
) :
    RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder>() {

    inner class MaintenanceViewHolder(val binding: MaintenanceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        val binding = MaintenanceItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MaintenanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        val maintenance = items[position]

        holder.binding.manTitulo.text = maintenance.titulo
        holder.binding.manInfo.text = maintenance.info
        holder.binding.manStatus.text = maintenance.status

        val isDone = maintenance.status == "CONCLUIDO"

        val isServiceAsked = maintenance.status == "SERVICO"

        holder.binding.buttonMarkAsFixed.visibility =
            if (isDone) View.GONE else View.VISIBLE

        holder.binding.buttonAskForService.visibility =
            if (isDone || isServiceAsked) View.GONE else View.VISIBLE

        if (!isDone) {
            holder.binding.buttonMarkAsFixed.setOnClickListener {
                onMaintenanceDone(maintenance.id)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newItems: List<Maintenance>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}