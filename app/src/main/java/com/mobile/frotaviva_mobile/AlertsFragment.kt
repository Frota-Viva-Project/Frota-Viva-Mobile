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
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.adapter.AlertAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentAlertsBinding
import com.mobile.frotaviva_mobile.model.Alert
import kotlinx.coroutines.launch

class AvisosFragment : Fragment() {
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    // Chave para obter o ID do caminhão (reaproveitada do outro Fragment, por convenção)
    companion object {
        const val TRUCK_ID_KEY = "truck_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdown()

        // 1. Obtém o ID do caminhão dos argumentos
        val truckId = arguments?.getInt(TRUCK_ID_KEY)

        if (truckId != null && truckId > 0) {
            fetchAlerts(truckId)
        } else {
            // Caso o ID não seja válido, exibe feedback e esconde o ProgressBar
            Toast.makeText(context, "ID do caminhão não encontrado para buscar alertas.", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
        }
    }

    // Função de busca que agora recebe o ID
    private fun fetchAlerts(truckId: Int) {
        lifecycleScope.launch {
            try {
                // 2. Chama a API passando o ID
                val response = RetrofitClient.instance.getAlerts(truckId)

                if (response.isSuccessful) {
                    val alertsList = response.body()
                    alertsList?.let {
                        setupRecyclerView(it)
                    } ?: run {
                        setupRecyclerView(emptyList())
                        Toast.makeText(context, "Nenhum aviso encontrado.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Erro ao buscar alertas: ${response.code()}. Body: $errorBody")
                    Toast.makeText(context, "Erro ao carregar avisos: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Falha na chamada da API de alertas", e)
                Toast.makeText(context, "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
                setupRecyclerView(emptyList())
            } finally {
            }
        }
    }

    private fun setupRecyclerView(data: List<Alert>) {
        binding.alertRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.alertRecyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))

        binding.alertRecyclerView.adapter = AlertAdapter(data)
    }

    private fun setupDropdown() {
        val placeholder = binding.dropdownContainer
        layoutInflater.inflate(R.layout.dropdown, placeholder, true)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}