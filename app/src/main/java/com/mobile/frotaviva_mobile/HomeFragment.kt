package com.mobile.frotaviva_mobile.fragments

import android.content.Context
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

    // Adiciona uma TAG para logs
    companion object {
        private const val TAG = "HomeFragmentLog"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()

    // --- Logs de Ciclo de Vida (Adicionados) ---
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: Fragment anexado à Activity.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment criado.")
    }
    // ---------------------------------------------

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Inflando layout.")
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val app = db.app
        // Mantido seu log original do Firebase, mas usando a TAG da classe
        Log.d(TAG, "Firestore App: ${app.name}, Project ID: ${app.options.projectId}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: View criada.")

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            Log.d(TAG, "Usuário autenticado. UID: ${user.uid}")
            user.getIdToken(true).addOnSuccessListener {
                Log.d(TAG, "Token atualizado com sucesso: ${it.token?.substring(0, 30)}...")

                // *** CHAVE PARA O SEU PROBLEMA: BUSCAR O truckId ***
                fetchTruckIdAndSetInActivity(user.uid)

            }.addOnFailureListener {
                Log.e(TAG, "Erro ao atualizar token", it)
            }
        } else {
            Log.e(TAG, "Usuário não autenticado. Não é possível buscar truckId.")
            // Você pode adicionar uma navegação para a tela de Login aqui se for necessário
        }
    }

    /**
     * Função que busca o truckId no Firestore baseado no UID do usuário
     * e o armazena na MainActivity.
     */
    private fun fetchTruckIdAndSetInActivity(uid: String) {
        // ASSUMÇÃO: O truckId está armazenado em uma coleção chamada 'users'
        // e o documento é o próprio UID, com um campo chamado 'truckId'.
        Log.d(TAG, "Buscando truckId para o UID: $uid")

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Tenta obter o 'truckId'. Assumindo que é um Long no Firestore (pode ser Int ou String)
                    val truckId = document.getLong("truckId")?.toInt()

                    if (truckId != null && truckId > 0) {
                        Log.i(TAG, "SUCESSO! truckId encontrado: $truckId")
                        // Passa o ID do caminhão para a MainActivity
                        (activity as? MainActivity)?.truckId = truckId
                    } else {
                        Log.w(TAG, "WARN: Campo 'truckId' nulo ou inválido no Firestore para o UID: $uid")
                        (activity as? MainActivity)?.truckId = null // Limpa se for inválido
                    }
                } else {
                    Log.w(TAG, "WARN: Documento do usuário não encontrado no Firestore para o UID: $uid")
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "ERRO ao buscar documento do usuário no Firestore: $exception")
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Limpando binding.")
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: Fragment desanexado da Activity.")
    }
}