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
import com.mobile.frotaviva_mobile.adapter.MaintenanceAdapter
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.FragmentMaintenancesBinding
import com.mobile.frotaviva_mobile.model.Maintenance
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class ManutencoesFragment : Fragment() {

    // Adiciona uma TAG para logs
    companion object {
        private const val TAG = "ManutencoesFragmentLog"
        const val TRUCK_ID_KEY = "truckId"
    }

    private var _binding: FragmentMaintenancesBinding? = null
    private val binding get() = _binding!!

    // Logs de Ciclo de Vida
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: Fragment anexado.")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Fragment criado.")
    }
    // Fim dos Logs de Ciclo de Vida

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView: Inflando layout.")
        _binding = FragmentMaintenancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: Chamando lógica de carregamento.")
        // Inicializa o RecyclerView com uma lista vazia para garantir que o Adapter esteja pronto
        setupRecyclerView(emptyList())

        fetchTruckIdAndLoadData()
    }

    // --- FUNÇÃO PARA MOSTRAR/ESCONDER O LOADING ---
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
            binding.maintenancesRecyclerView.visibility = View.GONE
            Log.d(TAG, "showLoading: Exibindo ProgressBar.")
        } else {
            binding.progressBar.visibility = View.GONE
            binding.maintenancesRecyclerView.visibility = View.VISIBLE
            Log.d(TAG, "showLoading: Escondendo ProgressBar e exibindo RecyclerView.")
        }
    }
    // ----------------------------------------------


    private fun fetchTruckIdAndLoadData() {
        // 1. Tenta pegar o truckId do Bundle (passado pela MainActivity)
        val truckIdFromBundle = arguments?.getInt(TRUCK_ID_KEY, 0)

        if (truckIdFromBundle != null && truckIdFromBundle > 0) {
            Log.i(TAG, "truckId obtido do Bundle: $truckIdFromBundle")
            // Se o ID já está no Bundle, usa ele diretamente
            fetchMaintenances(truckIdFromBundle)
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
                        fetchMaintenances(truckId)
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


    private fun fetchMaintenances(truckId: Int) {
        Log.d(TAG, "fetchMaintenances: Iniciando chamada Retrofit para truckId: $truckId")
        showLoading(true) // ⚡ Inicia o Loading antes da chamada Retrofit

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getMaintenances(truckId)

                if (response.isSuccessful) {
                    val maintenancesList = response.body()
                    maintenancesList?.let {
                        Log.i(TAG, "SUCESSO: ${it.size} manutenções carregadas.")
                        setupRecyclerView(it)
                    } ?: run {
                        Log.w(TAG, "RESPOSTA VAZIA: Body nulo.")
                        setupRecyclerView(emptyList())
                        Toast.makeText(context, "Nenhuma manutenção encontrada.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "ERRO API: Código ${response.code()}. Body: $errorBody")
                    Toast.makeText(context, "Erro ao carregar manutenções: ${response.code()}", Toast.LENGTH_LONG).show()
                    setupRecyclerView(emptyList())
                }

            } catch (e: Exception) {
                Log.e(TAG, "EXCEÇÃO API: Falha na chamada da API", e)
                Toast.makeText(context, "Erro de conexão/API: ${e.message}", Toast.LENGTH_LONG).show()
                setupRecyclerView(emptyList())
            } finally {
                showLoading(false) // ⚡ Garante que o loading para, independentemente do resultado
            }
        }
    }

    private fun setupRecyclerView(data: List<Maintenance>) {
        Log.d(TAG, "setupRecyclerView: Configurando com ${data.size} itens.")

        // Verifica se o adapter já existe para evitar re-criação
        if (binding.maintenancesRecyclerView.adapter == null) {
            val recyclerView = binding.maintenancesRecyclerView
            recyclerView.layoutManager = LinearLayoutManager(context)
            // Adiciona o decoration apenas na primeira vez
            recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))
            recyclerView.adapter = MaintenanceAdapter(data)
        } else {
            // Se já existe, apenas atualiza a lista (melhor prática)
            (binding.maintenancesRecyclerView.adapter as? MaintenanceAdapter)?.updateData(data)
        }
    }

    // [Manter o dpToPx e createDate]
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }

    private fun createDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute, 0)
        return calendar.time
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: Limpando binding.")
        _binding = null
    }
}