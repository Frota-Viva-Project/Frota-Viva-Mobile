package com.mobile.frotaviva_mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.adapter.RouteAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.DialogRoutesVisualizationBinding
import com.mobile.frotaviva_mobile.databinding.RouteItemBinding
import kotlinx.coroutines.launch

class RoutesDialogFragment : DialogFragment() {

    companion object {
        const val TAG = "RoutesDialogFragment"
        private const val TRUCK_ID_ARG = "truck_id_arg"

        fun newInstance(truckId: Int): RoutesDialogFragment {
            val fragment = RoutesDialogFragment()
            val args = Bundle()
            args.putInt(TRUCK_ID_ARG, truckId)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: DialogRoutesVisualizationBinding? = null
    private val binding get() = _binding!!
    private lateinit var routeAdapter: RouteAdapter
    private var truckId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(STYLE_NORMAL, R.style.CustomDialogTheme)
        truckId = arguments?.getInt(TRUCK_ID_ARG) ?: 0
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT, // Largura: Ajusta ao tamanho do CardView
            ViewGroup.LayoutParams.WRAP_CONTENT  // Altura: Ajusta ao tamanho do CardView
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Assume que o CardView fornecido está em um layout chamado 'dialog_routes.xml'
        _binding = DialogRoutesVisualizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        if (truckId > 0) {
            fetchRoutes(truckId)
        } else {
            Toast.makeText(requireContext(), "ID do caminhão inválido.", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        routeAdapter = RouteAdapter(emptyList())
        binding.routeRecycler.apply{
            layoutManager = LinearLayoutManager(context)
            adapter = routeAdapter
        }
    }

    private fun fetchRoutes(truckId: Int) {
        // showLoading(true) // Opcional: Adicionar ProgressBar no Dialog para loading

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getRoutes(truckId)

                if (!isAdded) return@launch

                if (response.isSuccessful) {
                    val routesList = response.body() ?: emptyList()
                    routeAdapter.updateData(routesList)

                    if (routesList.isEmpty()) {
                        Toast.makeText(requireContext(), "Nenhuma rota encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Erro ao carregar rotas: ${response.code()}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                if (isAdded) {
                    Toast.makeText(requireContext(), "Erro de conexão/API: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            // finally {
            //     showLoading(false)
            // }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}