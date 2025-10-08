package com.mobile.frotaviva_mobile.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.databinding.FragmentHomeBinding
import com.mobile.frotaviva_mobile.MainActivity

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            Log.d("DEBUG", "ID do usuário: $userId")

            db.collection("drivers").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val truckId = document.getLong("truckId")?.toInt()
                        val name = document.getString("name")
                        val plate = document.getString("carPlate")

                        binding.collaboratorName.text = name ?: "Sem nome"
                        binding.collaboratorPlate.text = plate ?: "Sem placa"

                        // Salva o truckId no MainActivity para reuso
                        (requireActivity() as? MainActivity)?.truckId = truckId
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DEBUG", "Erro ao buscar dados do motorista", e)
                }
        } else {
            Log.e("DEBUG", "Usuário não logado")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
