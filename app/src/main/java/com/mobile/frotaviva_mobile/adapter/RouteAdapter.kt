package com.mobile.frotaviva_mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.databinding.RouteItemBinding
import com.mobile.frotaviva_mobile.model.Route

class RouteAdapter(
    private var routes: List<Route>,
    private val onRouteDone: (routeId: Int) -> Unit
) : RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {
    inner class RouteViewHolder(val binding: RouteItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val binding = RouteItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]

        holder.binding.routeItemArrival.text = "Destino: ${route.destinoFinal}"
        holder.binding.routeItemDeparture.text = route.destinoInicial
        holder.binding.routeItemDistance.text = route.distancia
        holder.binding.routeItemStatus.text = route.status

        val isFinished = route.status == "FINALIZADA" || route.status == "CONCLUIDA"

        holder.binding.concluirRota.visibility =
            if (isFinished) View.GONE else View.VISIBLE

        if (!isFinished) {
            holder.binding.concluirRota.setOnClickListener {
                onRouteDone(route.id)
            }
        }
    }

    override fun getItemCount(): Int = routes.size

    fun updateData(newRoutes: List<Route>) {
        routes = newRoutes
        notifyDataSetChanged()
    }
}