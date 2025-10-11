package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobile.frotaviva_mobile.databinding.ActivityMainBinding
import com.mobile.frotaviva_mobile.fragments.AvisosFragment
import com.mobile.frotaviva_mobile.fragments.HomeFragment
import com.mobile.frotaviva_mobile.fragments.ManutencoesFragment

class MainActivity : AppCompatActivity() {
    // Adiciona uma TAG para logs, facilitando a filtragem no Logcat
    companion object {
        private const val TAG = "MainActivityLog"
    }

    lateinit var binding: ActivityMainBinding
    var truckId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: Iniciando MainActivity.") // Log de Início


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate: Layout inflado com sucesso.")

        val bottomNavigationView = binding.navbarInclude.bottomNavigation

        // Garante que a HomeFragment só seja carregada uma vez (na primeira inicialização)
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
            Log.d(TAG, "onCreate: Carregando HomeFragment inicial.") // Log do Fragment inicial
            loadFragment(HomeFragment())
        }

        // Configura a navegação da Bottom Bar
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    Log.d(TAG, "Navegação: Selecionado 'Home'.")
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_avisos -> {
                    val id = truckId
                    if (id != null && id > 0) {
                        Log.d(TAG, "Navegação: Selecionado 'Alertas'. Truck ID: $id.")
                        val fragment = AvisosFragment().apply {
                            arguments = Bundle().apply {
                                putInt(AvisosFragment.TRUCK_ID_KEY, id)
                            }
                        }
                        loadFragment(fragment)
                    } else {
                        Log.w(TAG, "Navegação: Alertas falhou. Truck ID é nulo/inválido.") // Log de Aviso
                        Toast.makeText(this, "Caminhão não encontrado", Toast.LENGTH_SHORT).show()
                    }
                    true

                }
                R.id.nav_manutencoes -> {
                    Log.d(TAG, "Navegação: Selecionado 'Manutençoes'.")
                    loadFragment(ManutencoesFragment())
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

    fun navigateToAlerts() {
        val menuItem = binding.navbarInclude.bottomNavigation.menu.findItem(R.id.nav_avisos)
        if (menuItem != null) {
            binding.navbarInclude.bottomNavigation.selectedItemId = menuItem.itemId
        }
    }
}