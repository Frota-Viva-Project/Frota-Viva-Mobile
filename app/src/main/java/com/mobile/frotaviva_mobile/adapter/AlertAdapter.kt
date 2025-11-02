package com.mobile.frotaviva_mobile.adapter

import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.databinding.AlertItemBinding
import com.mobile.frotaviva_mobile.model.Alert

class AlertAdapter(
    private var items: List<Alert>,
    private val onAlertDone: (alertId: Int) -> Unit,
    private val onSendToMaintenance: (alertId: Int, alertTitle: String, alertDetails: String) -> Unit,
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    inner class AlertViewHolder(val binding: AlertItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): AlertViewHolder {
        val binding = AlertItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = items[position]

        val statusText = when (alert.status) {
            "CONCLUIDO" -> "Alerta Concluído"
            "PENDENTE" -> "Alerta para Resolver"
            "MANUTENÇÃO" -> "Alerta Enviado para Manutenção"
            else -> alert.status
        }

        val categoryText = when (alert.categoria) {
            "URGENTE" -> "Urgente"
            "SIMPLES" -> "Simples"
            "INTERMEDIARIO" -> "Intermediário"
            else -> alert.categoria
        }

        holder.binding.alertTitle.text = alert.titulo
        holder.binding.alertCategory.text = categoryText
        holder.binding.alertDescription.text = alert.descricao
        holder.binding.alertStatusReport.text = statusText

        val isDone = alert.status == "CONCLUIDO"
        val isServiceAsked = alert.status == "MANUTENÇÃO"

        holder.binding.buttonMarkAsDone.visibility =
            if (isDone) View.GONE else View.VISIBLE

        holder.binding.buttonSendtoMaintenance.visibility =
            if (isDone || isServiceAsked) View.GONE else View.VISIBLE

        if (!isDone) {
            holder.binding.buttonMarkAsDone.setOnClickListener {
                onAlertDone(alert.id)
            }

            holder.binding.buttonSendtoMaintenance.setOnClickListener {
                onSendToMaintenance(alert.id, alert.titulo, alert.descricao)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Alert>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
