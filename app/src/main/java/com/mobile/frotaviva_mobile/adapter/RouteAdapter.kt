package com.mobile.frotaviva_mobile.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.model.Route

class RouteAdapter(private var routes: List<Route>) :
    RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val arrival: TextView = itemView.findViewById(R.id.routeItemArrival)
        val departure: TextView = itemView.findViewById(R.id.routeItemDeparture)
        val distance: TextView = itemView.findViewById(R.id.routeItemDistance)
        val status: TextView = itemView.findViewById(R.id.routeItemStatus)
        val finishButton: Button = itemView.findViewById(R.id.buttonLogin2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.route_item, parent, false)
        return RouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val route = routes[position]

        // Preenche os dados
        holder.arrival.text = "Destino: ${route.destinoFinal}"
        holder.departure.text = route.destinoInicial
        holder.distance.text = route.distancia
        holder.status.text = route.status

        holder.finishButton.setOnClickListener {
            Toast.makeText(holder.itemView.context, "Funcionalidade de Concluir Rota em desenvolvimento.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int = routes.size

    fun updateData(newRoutes: List<Route>) {
        routes = newRoutes
        notifyDataSetChanged()
    }
}