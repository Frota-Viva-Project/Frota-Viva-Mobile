package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.util.Log // Importação necessária para usar Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.mobile.frotaviva_mobile.databinding.ActivityMainBinding
import com.mobile.frotaviva_mobile.fragments.AvisosFragment
import com.mobile.frotaviva_mobile.fragments.HomeFragment
import com.mobile.frotaviva_mobile.fragments.ManutencoesFragment

class MainActivity : AppCompatActivity() {

    // Adiciona uma TAG para logs, facilitando a filtragem no Logcat
    companion object {
        private const val TAG = "MainActivityLog"
    }

    private lateinit var binding: ActivityMainBinding
    var truckId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando MainActivity.") // Log de Início

        // Inicialização do Firebase AppCheck (mantido)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Layout inflado com sucesso.")

        val bottomNavigationView = binding.navbarInclude.bottomNavigation

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
            Log.d(TAG, "onCreate: Carregando HomeFragment inicial.") // Log do Fragment inicial
            loadFragment(HomeFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d(TAG, "Navegação: Selecionado 'Home'.")
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_avisos -> {
                    Log.d(TAG, "Navegação: Selecionado 'Avisos'.")
                    loadFragment(AvisosFragment())
                    true
                }
                R.id.nav_manutencoes -> {
                    val id = truckId
                    if (id != null && id > 0) {
                        Log.d(TAG, "Navegação: Selecionado 'Manutenções'. Truck ID: $id.")
                        val fragment = ManutencoesFragment().apply {
                            arguments = Bundle().apply {
                                putInt(ManutencoesFragment.TRUCK_ID_KEY, id)
                            }
                        }
                        loadFragment(fragment)
                    } else {
                        Log.w(TAG, "Navegação: Manutenções falhou. Truck ID é nulo/inválido.") // Log de Aviso
                        Toast.makeText(this, "Caminhão não encontrado", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> {
                    Log.e(TAG, "Navegação: Item desconhecido selecionado: ${item.itemId}")
                    false
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // Usa o nome simples da classe do Fragment para o log
        val fragmentName = fragment::class.simpleName
        Log.d(TAG, "loadFragment: Substituindo container pelo Fragment: $fragmentName")

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Opcional: Adicione mais logs do ciclo de vida se precisar rastrear mais
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: MainActivity visível.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: MainActivity destruída.")
    }
}