package com.mobile.frotaviva_mobile

import VerticalSpaceItemDecoration
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.adapter.AlertAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentAlertsBinding
import com.mobile.frotaviva_mobile.model.Alert
import com.mobile.frotaviva_mobile.model.MaintenanceRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class AlertsFragment : Fragment() {

    companion object {
        const val RELOAD_KEY = "RELOAD_MAINTENANCES"
        const val TRUCK_ID_KEY = "truckId"
    }

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var alertAdapter: AlertAdapter
    private lateinit var originalAlerts: List<Alert>

    private val insertAlertLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val shouldReload = data?.getBooleanExtra(RELOAD_KEY, false) ?: false

            if (shouldReload) {
                fetchTruckIdAndLoadData()
                Toast.makeText(requireContext(), "Lista de alertas atualizada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)

        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName ?: "Motorista"

        binding.buttonAddAlert.setOnClickListener {
            val context = requireContext()
            val truckIdFromBundle = arguments?.getInt(TRUCK_ID_KEY, 0)

            val intent = Intent(context, InsertAlert::class.java).apply {
                putExtra(TRUCK_ID_KEY, truckIdFromBundle)
            }

            insertAlertLauncher.launch(intent)
        }

        binding.alertCount.text = "$userName, carregando alertas..."

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(emptyList())
        setupDropdown()
        fetchTruckIdAndLoadData()
    }

    private fun fetchTruckIdAndLoadData() {
        val truckIdFromBundle = arguments?.getInt(TRUCK_ID_KEY, 0)

        val user = FirebaseAuth.getInstance().currentUser

        if (truckIdFromBundle != null && truckIdFromBundle > 0) {
            fetchAlerts(truckIdFromBundle)
            return
        }

        val db = FirebaseFirestore.getInstance()

        if (user == null) {
            Toast.makeText(requireContext(), "Usuário não autenticado", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
            return
        }

        val userId = user.uid

        showLoading(true)

        lifecycleScope.launch {
            try {
                val snapshot = db.collection("driver").document(userId).get().await()

                if (!isAdded) return@launch

                if (snapshot.exists()) {
                    val truckId = snapshot.getLong("truckId")?.toInt() ?: 0

                    if (truckId > 0) {
                        fetchAlerts(truckId)
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

    private fun fetchAlerts(truckId: Int) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAlerts(truckId)

                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    val alertsList = response.body()

                    alertsList?.let {
                        originalAlerts = it.sortedBy { alert -> alert.id }
                        setupRecyclerView(originalAlerts)

                        val pendingCount = originalAlerts.count { alert ->
                            alert.status.equals("PENDENTE", ignoreCase = true)
                        }

                        val user = FirebaseAuth.getInstance().currentUser
                        val db = FirebaseFirestore.getInstance()

                        if (user != null) {
                            val snapshot = db.collection("driver").document(user.uid).get().await()
                            val driverName = snapshot.getString("name") ?: "Motorista"

                            if (_binding != null && isAdded && pendingCount == 1) {
                                binding.alertCount.text = "$driverName, você tem $pendingCount alerta pendente"
                            }
                            else if (_binding != null && isAdded) {
                                binding.alertCount.text = "$driverName, você tem $pendingCount alertas pendentes"
                            }
                        } else {
                            if (_binding != null && isAdded) {
                                binding.alertCount.text = "Você tem $pendingCount alertas pendentes"
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar alertas: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro de conexão/API: ${e.message}", Toast.LENGTH_LONG).show()
                }
                if (_binding != null) {
                    setupRecyclerView(emptyList())
                }
            } finally {
                if (_binding != null) {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return

        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.alertRecyclerView.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.alertRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView(data: List<Alert>) {
        if (_binding == null) return

        val recyclerView = binding.alertRecyclerView
        val truckId = arguments?.getInt(TRUCK_ID_KEY, 0) ?: 0

        val onDoneCallback: (Int) -> Unit = { alertId ->
            markAlertAsDone(truckId, alertId)
        }

        val onSendToMaintenanceCallback: (Int, String, String) -> Unit =
            { alertId, alertTitle, alertDetails ->
                sendAlertToMaintenance(truckId, alertId, alertTitle, alertDetails)
            }

        if (recyclerView.adapter == null) {
            alertAdapter = AlertAdapter(
                items = data,
                onAlertDone = onDoneCallback,
                onSendToMaintenance = onSendToMaintenanceCallback
            )

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))
            recyclerView.adapter = alertAdapter

        } else {
            (recyclerView.adapter as? AlertAdapter)?.updateData(data)
        }
    }

    private fun markAlertAsDone(truckId: Int, alertId: Int) {
        if (truckId <= 0) {
            Toast.makeText(requireContext(), "ID do caminhão não disponível.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.markAlertAsDone(truckId, alertId)

                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Alerta finalizado com sucesso!", Toast.LENGTH_SHORT).show()
                    fetchAlerts(truckId)
                } else {
                    Toast.makeText(requireContext(), "Falha ao finalizar alerta: ${response.code()}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro de conexão ao finalizar alerta: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendAlertToMaintenance(truckId: Int, alertId: Int, alertTitle: String, alertDetails: String) {
        if (truckId <= 0) {
            Toast.makeText(requireContext(), "ID do caminhão não disponível.", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val markResponse = RetrofitClient.instance.markAlertForMaintenance(truckId, alertId)

                if (!isAdded) return@launch

                if (markResponse.isSuccessful) {
                    val maintenanceRequest = MaintenanceRequest(
                        titulo = "Manutenção (Alerta ID $alertId): $alertTitle",
                        info = alertDetails,
                    )

                    val maintenanceResponse = RetrofitClient.instance.sendMaintenance(truckId, maintenanceRequest)

                    if (maintenanceResponse.isSuccessful) {
                        Toast.makeText(requireContext(), "Alerta marcado e Manutenção criada com sucesso!", Toast.LENGTH_LONG).show()
                        fetchAlerts(truckId)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Alerta marcado, mas falha ao criar Manutenção: ${maintenanceResponse.code()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                } else {
                    Toast.makeText(requireContext(), "Falha ao enviar pra manutenção: ${markResponse.code()}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro de conexão/API ao processar manutenção: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupDropdown() {
        if (_binding == null) return
        val dropdownContainer = binding.dropdownContainer

        dropdownContainer.removeAllViews()
        layoutInflater.inflate(R.layout.dropdown, dropdownContainer, true)

        val dropdownHeader = dropdownContainer.getChildAt(0) as View
        val displayText = dropdownHeader.findViewById<TextView>(R.id.textView2)
        val categories = listOf("SIMPLES", "INTERMEDIÁRIO", "URGENTE")

        dropdownHeader.setOnClickListener {
            val popup = PopupMenu(requireContext(), dropdownHeader)
            categories.forEachIndexed { index, category ->
                popup.menu.add(0, index, index, category)
            }
            popup.setOnMenuItemClickListener { item ->
                val selectedCategory = item.title.toString()
                displayText.text = selectedCategory
                filterByCategory(selectedCategory)
                true
            }
            popup.show()
        }
    }

    private fun filterByCategory(category: String) {
        if (!::originalAlerts.isInitialized) return

        val query = category.lowercase(Locale.getDefault())

        val filteredList = if (query == "selecionar") {
            originalAlerts
        } else {
            originalAlerts.filter { alert ->
                alert.categoria.lowercase(Locale.getDefault()).contains(query)
            }
        }
        alertAdapter.updateData(filteredList)
        if (filteredList.isEmpty()) {
            Toast.makeText(requireContext(), "Nenhum alerta encontrado na categoria '$category'.", Toast.LENGTH_SHORT).show()
        }
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
