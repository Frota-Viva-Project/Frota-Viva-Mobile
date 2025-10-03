package com.mobile.frotaviva_mobile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mobile.frotaviva_mobile.databinding.ActivityMainBinding
import com.mobile.frotaviva_mobile.fragments.AvisosFragment
import com.mobile.frotaviva_mobile.fragments.HomeFragment
import com.mobile.frotaviva_mobile.fragments.ManutencoesFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // CORREÇÃO AQUI: Acessando a BottomNavigationView diretamente pelo binding gerado.
        // O nome da propriedade é 'navbarInclude', e a view interna é 'bottomNavigation' (se o ID for 'bottom_navigation')
        val bottomNavigationView = binding.navbarInclude.bottomNavigation

        // Garante que a HomeFragment só seja carregada uma vez (na primeira inicialização)
        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
            loadFragment(HomeFragment())
        }

        // Configura a navegação da Bottom Bar
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_avisos -> {
                    loadFragment(AvisosFragment())
                    true
                }
                R.id.nav_manutencoes -> {
                    loadFragment(ManutencoesFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}