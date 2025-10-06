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
import com.mobile.frotaviva_mobile.R
import com.mobile.frotaviva_mobile.adapter.AlertAdapter
import com.mobile.frotaviva_mobile.databinding.FragmentAlertsBinding
import com.mobile.frotaviva_mobile.model.Alert

class AvisosFragment : Fragment() {
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla o layout usando View Binding.
        // O nome da classe de binding é gerado a partir do nome do arquivo XML (fragment_avisos.xml -> FragmentAvisosBinding)
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. O código de inicialização da Activity vai para onViewCreated

        setupRecyclerView()
        setupDropdown()
    }

    // Função para configurar o RecyclerView
    private fun setupRecyclerView() {
        binding.alertRecyclerView.layoutManager = LinearLayoutManager(context)
        // Usamos requireContext() ou context para obter o contexto dentro de um Fragment

        binding.alertRecyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))

        val dadosFake = listOf(
            Alert("Aviso urgente", "Temperatura do Motor maior que 105 °C"),
            Alert("Aviso intermediário", "Nível de combustível entre 20% e 25%"),
            Alert("Aviso simples", "Nível de água entre 40% e 50%"),
            Alert("Aviso urgente", "Temperatura do Motor maior que 105 °C"),
            Alert("Aviso intermediário", "Nível de combustível entre 20% e 25%"),
            Alert("Aviso simples", "Nível de água entre 40% e 50%")
        )

        binding.alertRecyclerView.adapter = AlertAdapter(dadosFake)
    }

    // Função para configurar o Dropdown
    private fun setupDropdown() {
        val placeholder = binding.dropdownContainer

        // Infla o layout do dropdown usando o layoutInflater do Fragment
        layoutInflater.inflate(R.layout.dropdown, placeholder, true)
    }

    // A função dpToPx permanece a mesma
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    // 3. Limpeza de Memória
    override fun onDestroyView() {
        super.onDestroyView()
        // Limpa a referência do binding para evitar memory leaks.
        _binding = null
    }
}