package com.mobile.frotaviva_mobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentHomeBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    companion object {
        const val TRUCK_ID_KEY = "truck_id"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val truckId = arguments?.getInt(TRUCK_ID_KEY)

        if (truckId != null && truckId > 0) {
            fetchRoutes(truckId)
        } else {
            Toast.makeText(context, "ID do caminhão não encontrado para buscar rotas.", Toast.LENGTH_LONG).show()
        }
    }

    private fun fetchRoutes(truckId: Int) {
        // Você pode adicionar um ProgressBar aqui, se o layout tiver um
        // binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getRoutes(truckId)

                if (response.isSuccessful) {
                    val routesList = response.body()
                    routesList?.let {
                        // Lógica para exibir a rota (ex: atualizar TextViews, se houver)
                        if (it.isNotEmpty()) {
                            // Exemplo: exibe o ponto de partida da primeira rota no log/Toast
                            Log.i("API_SUCCESS", "Rotas carregadas. Partida: ${it.first().partida}")
                            Toast.makeText(context, "Rotas carregadas com sucesso.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Nenhuma rota encontrada.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_ERROR", "Erro ao buscar rotas: ${response.code()}. Body: $errorBody")
                    Toast.makeText(context, "Erro ao carregar rotas: ${response.code()}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                Log.e("API_EXCEPTION", "Falha na chamada da API de rotas", e)
                Toast.makeText(context, "Erro de conexão: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}