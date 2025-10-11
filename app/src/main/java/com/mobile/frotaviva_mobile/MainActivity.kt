package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.mobile.frotaviva_mobile.databinding.ActivityMainBinding
import com.mobile.frotaviva_mobile.fragments.AvisosFragment
import com.mobile.frotaviva_mobile.fragments.HomeFragment
import com.mobile.frotaviva_mobile.fragments.ManutencoesFragment

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var truckId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.navbarInclude.bottomNavigation

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.nav_home
            loadFragment(HomeFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_avisos -> {
                    val id = truckId
                    if (id != null && id > 0) {
                        val fragment = AvisosFragment().apply {
                            arguments = Bundle().apply {
                                putInt(AvisosFragment.TRUCK_ID_KEY, id)
                            }
                        }
                        loadFragment(fragment)
                    } else {
                        Toast.makeText(this, "Caminhão não encontrado", Toast.LENGTH_SHORT).show()
                    }
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

    fun navigateToAlerts() {
        val menuItem = binding.navbarInclude.bottomNavigation.menu.findItem(R.id.nav_avisos)
        if (menuItem != null) {
            binding.navbarInclude.bottomNavigation.selectedItemId = menuItem.itemId
        }
    }
}