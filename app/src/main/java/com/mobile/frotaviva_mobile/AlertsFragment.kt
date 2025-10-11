package com.mobile.frotaviva_mobile.fragments

import VerticalSpaceItemDecoration
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
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
        private const val TAG = "ManutencoesFragmentLog"
        const val TRUCK_ID_KEY = "truckId"
    }

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Inflando layout.")
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: Fragment anexado.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment criado.")
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Chamando lógica de carregamento.")
        // Inicializa o RecyclerView com uma lista vazia para garantir que o Adapter esteja pronto
        setupRecyclerView(emptyList())
        setupDropdown()
        fetchTruckIdAndLoadData()
    }

    // Função de busca que agora recebe o ID
    private fun fetchTruckIdAndLoadData() {
        // 1. Tenta pegar o truckId do Bundle (passado pela MainActivity)
        val truckIdFromBundle = arguments?.getInt(TRUCK_ID_KEY, 0)

        if (truckIdFromBundle != null && truckIdFromBundle > 0) {
            Log.i(TAG, "truckId obtido do Bundle: $truckIdFromBundle")
            // Se o ID já está no Bundle, usa ele diretamente
            fetchAlerts(truckIdFromBundle)
            return
        }

        // 2. Se não veio pelo Bundle (ou é 0), tenta buscar no Firebase (lógica de fallback)
        Log.w(TAG, "truckId não encontrado no Bundle. Tentando buscar via Firebase...")

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user == null) {
            Log.e(TAG, "Usuário não autenticado. Abortando busca.")
            Toast.makeText(context, "Usuário não autenticado", Toast.LENGTH_LONG).show()
            setupRecyclerView(emptyList())
            return
        }

        val userId = user.uid
        Log.d(TAG, "Buscando motorista no Firebase com UID: $userId")

        showLoading(true) // ⚡ Inicia o Loading para a busca do Firebase

        lifecycleScope.launch {
            try {
                val snapshot = db.collection("driver").document(userId).get().await()
                if (snapshot.exists()) {
                    val truckId = snapshot.getLong("truckId")?.toInt() ?: 0

                    if (truckId > 0) {
                        Log.i(TAG, "truckId encontrado via Firebase: $truckId")
                        fetchAlerts(truckId)
                    } else {
                        Log.w(TAG, "Campo 'truckId' nulo ou inválido no Firebase.")
                        Toast.makeText(context, "ID do caminhão não encontrado.", Toast.LENGTH_LONG).show()
                        setupRecyclerView(emptyList())
                        showLoading(false) // ⚡ Termina o loading em caso de falha de dados
                    }
                } else {
                    Log.w(TAG, "Documento do motorista não encontrado no Firebase.")
                    Toast.makeText(context, "Motorista não encontrado.", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                    showLoading(false) // ⚡ Termina o loading em caso de falha de dados
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao buscar dados do motorista no Firebase", e)
                Toast.makeText(context, "Erro ao buscar motorista: ${e.message}", Toast.LENGTH_LONG).show()
                setupRecyclerView(emptyList())
                showLoading(false) // ⚡ Termina o loading em caso de exceção
            }
        }
    }


    private fun fetchAlerts(truckId: Int) {
        Log.d(TAG, "fetchAlerts: Iniciando chamada Retrofit para truckId: $truckId")
        showLoading(true) // ⚡ Inicia o Loading antes da chamada Retrofit

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getAlerts(truckId)

                if (response.isSuccessful) {
                    val alertsList = response.body()
                    alertsList?.let {
                        Log.i(TAG, "SUCESSO: ${it.size} alertas carregados.")
                        setupRecyclerView(it)
                    } ?: run {
                        Log.w(TAG, "RESPOSTA VAZIA: Body nulo.")
                        setupRecyclerView(emptyList())
                        Toast.makeText(context, "Nenhum alerta encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "ERRO API: Código ${response.code()}. Body: $errorBody")
                    Toast.makeText(context, "Erro ao carregar alertas: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                Log.e(TAG, "EXCEÇÃO API: Falha na chamada da API", e)
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
            Log.d(TAG, "showLoading: Exibindo ProgressBar.")
        } else {
            binding.progressBar.visibility = View.GONE
            binding.alertRecyclerView.visibility = View.VISIBLE
            Log.d(TAG, "showLoading: Escondendo ProgressBar e exibindo RecyclerView.")
        }
    }


    private fun setupRecyclerView(data: List<Alert>) {
        Log.d(TAG, "setupRecyclerView: Configurando com ${data.size} itens.")

        // Verifica se o adapter já existe para evitar re-criação
        if (binding.alertRecyclerView.adapter == null) {
            val recyclerView = binding.alertRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            // Adiciona o decoration apenas na primeira vez
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))
            recyclerView.adapter = AlertAdapter(data)
        } else {
            // Se já existe, apenas atualiza a lista (melhor prática)
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