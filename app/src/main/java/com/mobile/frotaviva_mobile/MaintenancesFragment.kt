package com.mobile.frotaviva_mobile.fragments

import VerticalSpaceItemDecoration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.frotaviva_mobile.adapter.MaintenanceAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentMaintenancesBinding
import com.mobile.frotaviva_mobile.model.Maintenance
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ManutencoesFragment : Fragment() {

    private var _binding: FragmentMaintenancesBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TRUCK_ID_KEY = "truckId"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val truckId = arguments?.getInt(TRUCK_ID_KEY)

        if (truckId != null && truckId > 0) {
            fetchMaintenances(truckId)
        } else {
            Toast.makeText(context, "ID do caminhão não encontrado.", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
        }
    }

    private fun fetchMaintenances(truckId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMaintenances(truckId)

                if (response.isSuccessful) {
                    val maintenancesList = response.body()
                    maintenancesList?.let {
                        setupRecyclerView(it)
                    } ?: run {
                        setupRecyclerView(emptyList())
                        Toast.makeText(context, "Nenhuma manutenção encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Erro na resposta: ${response.code()}. Body: $errorBody")
                    Toast.makeText(context, "Erro ao carregar manutenções: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Falha na chamada da API", e)
                Toast.makeText(context, "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                setupRecyclerView(emptyList())
            } finally {
            }
        }
    }

    private fun setupRecyclerView(data: List<Maintenance>) {
        val recyclerView = binding.maintenancesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))

        recyclerView.adapter = MaintenanceAdapter(data)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun createDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute, 0)
        return calendar.time
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}