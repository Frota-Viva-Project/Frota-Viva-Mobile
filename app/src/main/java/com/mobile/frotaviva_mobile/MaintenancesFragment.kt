package com.mobile.frotaviva_mobile

import VerticalSpaceItemDecoration
import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.util.Locale

class MaintenancesFragment : Fragment() {

    companion object {
        const val TRUCK_ID_KEY = "truckId"
        const val RELOAD_KEY = "RELOAD_MAINTENANCES"
    }

    private var _binding: FragmentMaintenancesBinding? = null
    private val binding get() = _binding!!

    private lateinit var maintenanceAdapter: MaintenanceAdapter

    private lateinit var originalData: List<Maintenance>

    private val insertMaintenanceLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val shouldReload = data?.getBooleanExtra(RELOAD_KEY, false) ?: false

            if (shouldReload) {
                fetchTruckIdAndLoadData()
                Toast.makeText(requireContext(), "Lista de manutenções atualizada.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMaintenancesBinding.inflate(inflater, container, false)
        binding.buttonAddMaintenance.setOnClickListener {
            val context = requireContext()
            val truckIdFromBundle = arguments?.getInt(TRUCK_ID_KEY, 0)

            val intent = Intent(context, InsertMaintenance::class.java).apply {
                putExtra(TRUCK_ID_KEY, truckIdFromBundle)
            }

            insertMaintenanceLauncher.launch(intent)
        }

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filter(s.toString().lowercase(Locale.getDefault()))
            }
        })

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(emptyList())

        fetchTruckIdAndLoadData()
    }

    private fun showLoading(isLoading: Boolean) {
        if (_binding == null) return

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

                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    val maintenancesList = response.body()

                    maintenancesList?.let {
                        originalData = it
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

    private fun setupRecyclerView(data: List<Maintenance>) {
        if (_binding == null) return

        val recyclerView = binding.maintenancesRecyclerView

        if (recyclerView.adapter == null) {
            maintenanceAdapter = MaintenanceAdapter(data)

            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))
            recyclerView.adapter = maintenanceAdapter

        } else {
            (recyclerView.adapter as? MaintenanceAdapter)?.updateData(data)
        }
    }

    private fun filter(text: String) {
        val filteredList: MutableList<Maintenance> = mutableListOf()
        val query = text.lowercase()

        for (item in originalData){
            if (item.titulo.lowercase().contains(query) ||
                item.info.lowercase().contains(query)) {

                filteredList.add(item)
            }
        }

        maintenanceAdapter.updateData(filteredList)
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