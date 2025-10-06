package com.mobile.frotaviva_mobile.fragments

import VerticalSpaceItemDecoration
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobile.frotaviva_mobile.adapter.MaintenanceAdapter
import com.mobile.frotaviva_mobile.databinding.FragmentMaintenancesBinding // Assume o nome do Binding
import com.mobile.frotaviva_mobile.model.Maintenance
import java.util.Calendar
import java.util.Date

// CORRIGIDO: Agora herda de Fragment()
class ManutencoesFragment : Fragment() {

    // 1. Setup do View Binding
    private var _binding: FragmentMaintenancesBinding? = null
    // Propriedade para acessar o binding de forma segura
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla o layout usando View Binding
        _binding = FragmentMaintenancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // O código de inicialização da Activity vem para cá:
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Usa o binding para acessar o RecyclerView
        val recyclerView = binding.maintenancesRecyclerView

        // Usa requireContext() ou context para obter o contexto dentro de um Fragment
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))

        // Lógica de dados fake
        val fakeMaintenanceData = listOf(
            Maintenance(
                titulo = "Oil Change",
                info = "Routine maintenance. Oil",
                dataOcorrido = createDate(2025, 9, 20, 10, 30),
                status = "Completed"
            ),
            Maintenance(
                titulo = "Electrical System Problem",
                info = "Left headlight is not working.",
                dataOcorrido = createDate(2025, 9, 23, 15, 0),
                status = "In Progress"
            ),
            Maintenance(
                titulo = "Brake Repair",
                info = "Rear brake pads ",
                dataOcorrido = createDate(2025, 9, 24, 9, 0),
                status = "Pending"
            ),
            Maintenance(
                titulo = "Engine Noise",
                info = "Strange noise detected",
                dataOcorrido = createDate(2025, 9, 25, 11, 0),
                status = "Pending"
            )
        )

        recyclerView.adapter = MaintenanceAdapter(fakeMaintenanceData)
    }

    // As funções utilitárias permanecem no Fragment
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun createDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        // O mês é baseado em zero (0=Janeiro, 11=Dezembro), por isso usamos month - 1
        calendar.set(year, month - 1, day, hour, minute, 0)
        return calendar.time
    }

    // 2. Limpeza de Memória
    override fun onDestroyView() {
        super.onDestroyView()
        // Libera a referência do binding para evitar memory leaks
        _binding = null
    }
}