package com.mobile.frotaviva_mobile.adapter

import android.util.Log // Importação necessária para o Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.model.Maintenance

class MaintenanceAdapter (private var items: List<Maintenance>)  : // Mudado para var para poder ser atualizado
    RecyclerView.Adapter<MaintenanceAdapter.MaintenanceViewHolder>() {

    // Adiciona a TAG para logs
    companion object {
        private const val TAG = "MaintenanceAdapterLog"
    }

    init {
        Log.d(TAG, "Adapter inicializado com ${items.size} itens.")
    }

    class MaintenanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Log aqui pode ser útil se o R.id.manTitulo/Info/Date estiverem errados
        val manTitulo: TextView = itemView.findViewById(R.id.manTitulo)
        val manInfo: TextView = itemView.findViewById(R.id.manInfo)
        val manDate: TextView = itemView.findViewById(R.id.manDate)

        init {
            // Confirma se as Views foram encontradas
            if (manDate == null) {
                Log.e(TAG, "ERRO CRÍTICO: Uma ou mais views (manTitulo/Info/Date) não foram encontradas no layout maintenance_item!")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaintenanceViewHolder {
        Log.d(TAG, "onCreateViewHolder: Inflando um novo ViewHolder.")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.maintenance_item, parent, false)
        return MaintenanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaintenanceViewHolder, position: Int) {
        val maintenance = items[position]

        // Log para ver se os dados estão sendo recebidos e atribuídos
        Log.v(TAG, "onBindViewHolder: Posição $position. Título: ${maintenance.titulo}")

        holder.manTitulo.text = maintenance.titulo
        holder.manInfo.text = maintenance.info

        // Nota: maintenance.dataOcorrido.toString() pode ser muito verboso.
        // Você pode querer formatar essa data para algo mais legível (ex: SimpleDateFormat)
        holder.manDate.text = maintenance.dataOcorrido.toString()
    }

    override fun getItemCount(): Int {
        val count = items.size
        Log.d(TAG, "getItemCount: Retornando $count itens.")
        return count
    }

    /**
     * Método adicionado para atualizar a lista de itens e notificar o RecyclerView.
     */
    fun updateData(newItems: List<Maintenance>) {
        this.items = newItems
        Log.i(TAG, "updateData: Lista de manutenções atualizada com ${newItems.size} itens.")
        notifyDataSetChanged()
    }
}