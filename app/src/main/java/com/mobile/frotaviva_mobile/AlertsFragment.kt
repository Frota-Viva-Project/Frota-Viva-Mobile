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
import com.mobile.frotaviva_mobile.adapter.AlertAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.auth.JwtUtils
import com.mobile.frotaviva_mobile.databinding.FragmentAlertsBinding
import com.mobile.frotaviva_mobile.model.Alert
import com.mobile.frotaviva_mobile.model.MaintenanceRequest
import com.mobile.frotaviva_mobile.storage.SecureStorage
import kotlinx.coroutines.launch
import java.util.Locale

class AlertsFragment : Fragment() {

    companion object {
        const val RELOAD_KEY = "RELOAD_MAINTENANCES"
    }

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var secureStorage: SecureStorage
    private lateinit var alertAdapter: AlertAdapter
    private lateinit var originalAlerts: List<Alert>

    private val insertAlertLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val shouldReload = result.data?.getBooleanExtra(RELOAD_KEY, false) ?: false
            if (shouldReload) {
                fetchAlerts()
                Toast.makeText(requireContext(), "Lista de alertas atualizada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)

        val userName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Motorista"

        binding.buttonAddAlert.setOnClickListener {
            val truckId = secureStorage.getTruckId()
            if (truckId == null || truckId <= 0) {
                Toast.makeText(requireContext(), "ID do caminhão não encontrado.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(requireContext(), InsertAlert::class.java).apply {
                putExtra(InsertAlert.TRUCK_ID_KEY, truckId)
            }
            insertAlertLauncher.launch(intent)
        }

        binding.alertCount.text = "$userName, carregando alertas..."
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        secureStorage = SecureStorage(requireContext())

        setupRecyclerView(emptyList())
        setupDropdown()
    }

    override fun onResume() {
        super.onResume()
        if (isUserAuthenticated()) {
            fetchAlerts()
        } else {
            redirectToLogin()
        }
    }

    private fun isUserAuthenticated(): Boolean {
        val token = secureStorage.getToken()
        return !token.isNullOrEmpty() && !JwtUtils.isTokenExpired(token) || auth.currentUser != null
    }

    private fun redirectToLogin() {
        Toast.makeText(requireContext(), "Sessão expirada. Faça login novamente.", Toast.LENGTH_LONG).show()
        secureStorage.clearToken()
        auth.signOut()
        val intent = Intent(requireContext(), Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun fetchAlerts() {
        val truckId = secureStorage.getTruckId()

        if (truckId == null || truckId <= 0) {
            Toast.makeText(requireContext(), "ID do caminhão não encontrado.", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
            return
        }

        showLoading(true)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAlerts(truckId)
                if (!isAdded) return@launch

                when {
                    response.isSuccessful -> {
                        val alertsList = response.body() ?: emptyList()
                        if (alertsList.isEmpty()) {
                            Toast.makeText(requireContext(), "Nenhum alerta disponível para este caminhão.", Toast.LENGTH_LONG).show()
                            setupRecyclerView(emptyList())
                        } else {
                            originalAlerts = alertsList.sortedByDescending { it.id }
                            setupRecyclerView(originalAlerts)

                            val pendingCount = originalAlerts.count { it.status.equals("PENDENTE", ignoreCase = true) }
                            val driverName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Motorista"

                            binding.alertCount.text = if (pendingCount == 1)
                                "$driverName, você tem $pendingCount alerta pendente"
                            else
                                "$driverName, você tem $pendingCount alertas pendentes"
                        }
                    }
                    response.code() == 404 -> {
                        Toast.makeText(requireContext(), "Nenhum alerta encontrado", Toast.LENGTH_LONG).show()
                        setupRecyclerView(emptyList())
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Erro ao carregar alertas: ${response.code()}", Toast.LENGTH_LONG).show()
                        setupRecyclerView(emptyList())
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro de conexão/API: ${e.message}", Toast.LENGTH_LONG).show()
                setupRecyclerView(emptyList())
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.alertRecyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun setupRecyclerView(data: List<Alert>) {
        val recyclerView = binding.alertRecyclerView
        val truckId = secureStorage.getTruckId() ?: 0

        val onDoneCallback: (Int) -> Unit = { alertId -> markAlertAsDone(truckId, alertId) }
        val onSendToMaintenanceCallback: (Int, String, String) -> Unit =
            { alertId, alertTitle, alertDetails -> sendAlertToMaintenance(truckId, alertId, alertTitle, alertDetails) }

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
                    fetchAlerts()
                } else {
                    Toast.makeText(requireContext(), "Falha ao finalizar alerta: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro de conexão ao finalizar alerta: ${e.message}", Toast.LENGTH_LONG).show()
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
                        fetchAlerts()
                    } else {
                        Toast.makeText(requireContext(), "Alerta marcado, mas falha ao criar Manutenção: ${maintenanceResponse.code()}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Falha ao enviar pra manutenção: ${markResponse.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro de conexão/API ao processar manutenção: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupDropdown() {
        val dropdownContainer = binding.dropdownContainer
        dropdownContainer.removeAllViews()
        layoutInflater.inflate(R.layout.dropdown, dropdownContainer, true)

        val dropdownHeader = dropdownContainer.getChildAt(0) as View
        val displayText = dropdownHeader.findViewById<TextView>(R.id.textView2)
        val categories = listOf("SIMPLES", "INTERMEDIÁRIO", "URGENTE")

        displayText.text = "Selecionar"
        displayText.setTextColor(resources.getColor(R.color.primary_default, null))

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
