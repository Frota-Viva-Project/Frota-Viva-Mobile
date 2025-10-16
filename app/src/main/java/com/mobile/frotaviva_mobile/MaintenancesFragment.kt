package com.mobile.frotaviva_mobile.fragments

import VerticalSpaceItemDecoration
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.adapter.MaintenanceAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentMaintenancesBinding
import com.mobile.frotaviva_mobile.model.Maintenance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class MaintenancesFragment : Fragment() {

    companion object {
        const val TRUCK_ID_KEY = "truckId"
    }

    private var _binding: FragmentMaintenancesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(emptyList())

        fetchTruckIdAndLoadData()
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return // **CORREÇÃO: Evita acessar binding após onDestroyView**

        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.maintenancesRecyclerView.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.maintenancesRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun fetchTruckIdAndLoadData() {
        val truckIdFromBundle = arguments?.getInt(TRUCK_ID_KEY, 0)

        if (truckIdFromBundle != null && truckIdFromBundle > 0) {
            fetchMaintenances(truckIdFromBundle)
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user == null) {
            // Usa requireContext() para garantir que o Context é válido.
            Toast.makeText(requireContext(), "Usuário não autenticado", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
            return
        }

        val userId = user.uid

        showLoading(true)

        lifecycleScope.launch {
            try {
                val snapshot = db.collection("driver").document(userId).get().await()

                // ** CORREÇÃO 1: Checa se o Fragment ainda está anexado após a operação assíncrona **
                if (!isAdded) return@launch

                if (snapshot.exists()) {
                    val truckId = snapshot.getLong("truckId")?.toInt() ?: 0

                    if (truckId > 0) {
                        fetchMaintenances(truckId)
                    } else {
                        Toast.makeText(requireContext(), "ID do caminhão não encontrado.", Toast.LENGTH_LONG).show()
                        setupRecyclerView(emptyList())
                        showLoading(false)
                    }
                } else {
                    Toast.makeText(requireContext(), "Motorista não encontrado.", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                    showLoading(false)
                }
            } catch (e: Exception) {
                // ** CORREÇÃO 2: Checa se o Fragment está anexado antes de mostrar o Toast **
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro ao buscar motorista: ${e.message}", Toast.LENGTH_LONG).show()
                }
                if (_binding != null) {
                    setupRecyclerView(emptyList())
                    showLoading(false)
                }
            }
        }
    }


    private fun fetchMaintenances(truckId: Int) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMaintenances(truckId)

                // ** CORREÇÃO 3: Checa se o Fragment ainda está anexado após a requisição de rede **
                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    val maintenancesList = response.body()
                    maintenancesList?.let {
                        setupRecyclerView(it)
                    } ?: run {
                        setupRecyclerView(emptyList())
                        Toast.makeText(requireContext(), "Nenhuma manutenção encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar manutenções: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                // ** CORREÇÃO 4: Checa se o Fragment está anexado antes de mostrar o Toast **
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro de conexão/API: ${e.message}", Toast.LENGTH_LONG).show()
                }
                if (_binding != null) {
                    setupRecyclerView(emptyList())
                }
            } finally {
                // ** CORREÇÃO 5: Garante que só acessa o binding se a View não foi destruída **
                if (_binding != null) {
                    showLoading(false)
                }
            }
        }
    }

    private fun setupRecyclerView(data: List<Maintenance>) {
        if (_binding == null) return // **CORREÇÃO: Evita acessar binding após onDestroyView**

        if (binding.maintenancesRecyclerView.adapter == null) {
            val recyclerView = binding.maintenancesRecyclerView
            // Usa requireContext() para garantir um Context válido.
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))
            recyclerView.adapter = MaintenanceAdapter(data)
        } else {
            (binding.maintenancesRecyclerView.adapter as? MaintenanceAdapter)?.updateData(data)
        }
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
        // Este é o ponto onde o binding é limpo.
        _binding = null
    }
}