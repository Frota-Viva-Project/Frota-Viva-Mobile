package com.mobile.frotaviva_mobile.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mobile.frotaviva_mobile.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 2. Infla o layout usando View Binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Coloque aqui a lógica para buscar e exibir os dados
        // (ex: buscar nome do motorista, placa do Firebase, carregar rotas)

        // Exemplo: Atualizando dados do motorista
        // binding.textView5.text = "Nome do Motorista Aqui"
        // binding.textView8.text = "XXX-0000"

        // *****************************************************************
        // Se você usou o código da MainActivity original,
        // a lógica de carregamento dos dados de perfil deve vir para cá.
        // *****************************************************************
    }

    // 3. Limpeza de Memória
    override fun onDestroyView() {
        super.onDestroyView()
        // Libera a referência do binding para evitar memory leaks
        _binding = null
    }
}