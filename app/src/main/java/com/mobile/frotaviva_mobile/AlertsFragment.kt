package com.mobile.frotaviva_mobile

import VerticalSpaceItemDecoration
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.mobile.frotaviva_mobile.InsertAlert
import com.mobile.frotaviva_mobile.InsertMaintenance
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.adapter.AlertAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentAlertsBinding
import com.mobile.frotaviva_mobile.databinding.FragmentMaintenancesBinding
import com.mobile.frotaviva_mobile.model.Alert
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AlertsFragment : Fragment() {

    companion object {
        const val RELOAD_KEY = "RELOAD_MAINTENANCES"
        const val TRUCK_ID_KEY = "truckId"
    }

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    private val insertAlertLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val shouldReload = data?.getBooleanExtra(AlertsFragment.Companion.RELOAD_KEY, false) ?: false

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
        binding.buttonAddAlert.setOnClickListener {
            val context = requireContext()
            val truckIdFromBundle = arguments?.getInt(AlertsFragment.Companion.TRUCK_ID_KEY, 0)

            val intent = Intent(context, InsertAlert::class.java).apply {
                putExtra(AlertsFragment.Companion.TRUCK_ID_KEY, truckIdFromBundle)
            }

            insertAlertLauncher.launch(intent)
        }
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

        if (truckIdFromBundle != null && truckIdFromBundle > 0) {
            fetchAlerts(truckIdFromBundle)
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user == null) {
            // Usa requireContext() pois estamos em onViewCreated, o fragment está anexado.
            Toast.makeText(requireContext(), "Usuário não autenticado", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
            return
        }

        val userId = user.uid

        showLoading(true)

        lifecycleScope.launch {
            try {
                val snapshot = db.collection("driver").document(userId).get().await()

                // ** CORREÇÃO 1: Checa se o Fragment ainda está anexado antes de prosseguir com UI **
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
                // ** CORREÇÃO 2: Checa se o Fragment ainda está anexado antes de mostrar o Toast **
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro ao buscar motorista: ${e.message}", Toast.LENGTH_LONG).show()
                }
                // O `_binding != null` é a checagem implícita feita pelo `binding get() = _binding!!`
                // Como esta checagem está dentro do `lifecycleScope`, se o job não foi cancelado,
                // significa que a View provavelmente ainda existe, mas a checagem `!isAdded` acima é a mais segura.
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

                // ** CORREÇÃO 3: Checa se o Fragment ainda está anexado após a requisição **
                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    val alertsList = response.body()
                    alertsList?.let {
                        setupRecyclerView(it)
                    } ?: run {
                        setupRecyclerView(emptyList())
                        Toast.makeText(requireContext(), "Nenhum alerta encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar alertas: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                // ** CORREÇÃO 4: Checa se o Fragment ainda está anexado antes de mostrar o Toast **
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


    private fun showLoading(isLoading: Boolean) {
        // Esta função usa 'binding' e, portanto, só deve ser chamada se '_binding' não for nulo.
        // As chamadas externas já foram verificadas (Correção 5), mas se for chamada diretamente,
        // a verificação `_binding != null` pode ser adicionada aqui para máxima segurança.
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
        // Similarmente, esta função usa 'binding'
        if (_binding == null) return

        if (binding.alertRecyclerView.adapter == null) {
            val recyclerView = binding.alertRecyclerView
            // Usa requireContext() para a criação do LayoutManager, que precisa de um Context válido
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))
            recyclerView.adapter = AlertAdapter(data)
        } else {
            (binding.alertRecyclerView.adapter as? AlertAdapter)?.updateData(data)
        }
    }


    private fun setupDropdown() {
        if (_binding == null) return
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