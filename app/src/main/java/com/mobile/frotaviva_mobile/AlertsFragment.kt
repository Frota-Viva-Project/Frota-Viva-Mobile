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
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.adapter.AlertAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentAlertsBinding
import com.mobile.frotaviva_mobile.model.Alert
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AvisosFragment : Fragment() {

    companion object {
        const val TRUCK_ID_KEY = "truckId"
    }

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
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
            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
            return
        }

        val userId = user.uid

        showLoading(true)

        lifecycleScope.launch {
            try {
                val snapshot = db.collection("driver").document(userId).get().await()
                if (snapshot.exists()) {
                    val truckId = snapshot.getLong("truckId")?.toInt() ?: 0

                    if (truckId > 0) {
                        fetchAlerts(truckId)
                    } else {
                        Toast.makeText(context, "ID do caminhão não encontrado.", Toast.LENGTH_LONG).show()
                        setupRecyclerView(emptyList())
                        showLoading(false)
                    }
                } else {
                    Toast.makeText(context, "Motorista não encontrado.", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                    showLoading(false)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao buscar motorista: ${e.message}", Toast.LENGTH_LONG).show()
                setupRecyclerView(emptyList())
                showLoading(false)
            }
        }
    }


    private fun fetchAlerts(truckId: Int) {
        showLoading(true)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAlerts(truckId)

                if (response.isSuccessful) {
                    val alertsList = response.body()
                    alertsList?.let {
                        setupRecyclerView(it)
                    } ?: run {
                        setupRecyclerView(emptyList())
                        Toast.makeText(context, "Nenhum alerta encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Erro ao carregar alertas: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Erro de conexão/API: ${e.message}", Toast.LENGTH_LONG).show()
                setupRecyclerView(emptyList())
            } finally {
                showLoading(false)
            }
        }
    }


    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.alertRecyclerView.visibility = View.GONE
        } else {
            binding.progressBar.visibility = View.GONE
            binding.alertRecyclerView.visibility = View.VISIBLE
        }
    }


    private fun setupRecyclerView(data: List<Alert>) {
        if (binding.alertRecyclerView.adapter == null) {
            val recyclerView = binding.alertRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))
            recyclerView.adapter = AlertAdapter(data)
        } else {
            (binding.alertRecyclerView.adapter as? AlertAdapter)?.updateData(data)
        }
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