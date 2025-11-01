package com.mobile.frotaviva_mobile.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.databinding.NotificationItemBinding
import com.mobile.frotaviva_mobile.model.Notification
import okhttp3.internal.notify

class NotificationAdapter (private var items: List<Notification>)
    : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    inner class NotificationViewHolder(val binding: NotificationItemBinding) :
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = NotificationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = items[position]
        holder.binding.notificationTitle.text = notification.title
        holder.binding.notificationDescription.text = notification.body
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newItens: List<Notification>) {
        this.items = newItens
        notifyDataSetChanged()
    }
}