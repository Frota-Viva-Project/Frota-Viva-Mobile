package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.mobile.frotaviva_mobile.databinding.ActivityMainBinding
import com.mobile.frotaviva_mobile.fragments.AlertsFragment
import com.mobile.frotaviva_mobile.fragments.MaintenancesFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var truckId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

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
                        val fragment = AlertsFragment().apply {
                            arguments = Bundle().apply {
                                putInt(AlertsFragment.TRUCK_ID_KEY, id)
                            }
                        }
                        loadFragment(fragment)
                    } else {
                        Toast.makeText(this, "Caminhão não encontrado", Toast.LENGTH_SHORT).show()
                    }
                    true

                }
                R.id.nav_manutencoes -> {
                    val id = truckId
                    if (id != null && id > 0) {
                        val fragment = MaintenancesFragment().apply {
                            arguments = Bundle().apply {
                                putInt(MaintenancesFragment.TRUCK_ID_KEY, id)
                            }
                        }
                        loadFragment(fragment)
                    } else {
                        Toast.makeText(this, "Caminhão não encontrado", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        val headerTitle: TextView = findViewById(R.id.headerTitle)
        val headerSubtitle: TextView = findViewById(R.id.headerSubtitle)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userName = user.displayName
            if (!userName.isNullOrEmpty()) {
                headerTitle.text = "Olá, $userName"
            } else {
                // Se o displayName estiver vazio, use o email ou um nome genérico
                headerTitle.text = "Olá, Usuário"
            }
        }
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        headerSubtitle.text = formattedDate
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    fun navigateToMaintenance() {
        val menuItem = binding.navbarInclude.bottomNavigation.menu.findItem(R.id.nav_manutencoes)

        if (menuItem != null) {
            binding.navbarInclude.bottomNavigation.selectedItemId = menuItem.itemId
        }
    }

    fun navigateToAlerts() {
        val menuItem = binding.navbarInclude.bottomNavigation.menu.findItem(R.id.nav_avisos)

        if (menuItem != null) {
            binding.navbarInclude.bottomNavigation.selectedItemId = menuItem.itemId
        }
    }
}
