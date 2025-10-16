package com.mobile.frotaviva_mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.model.Alert

class AlertAdapter(private var items: List<Alert>) :
    RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    class AlertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.alertTitle)
        val tvCategoria: TextView = itemView.findViewById(R.id.alertCatInfo)
        val tvDescricao: TextView = itemView.findViewById(R.id.alertCatDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.alert_item, parent, false)
        return AlertViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        val alert = items[position]
        holder.tvTitulo.text = alert.titulo
        holder.tvCategoria.text = alert.categoria
        holder.tvDescricao.text = alert.descricao
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newItems: List<Alert>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}


