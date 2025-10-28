package com.mobile.frotaviva_mobile

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.mobile.frotaviva_mobile.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    private lateinit var toggle: ActionBarDrawerToggle
    var truckId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.headerInclude.root
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeButtonEnabled(false)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        val headerTitle: TextView = toolbar.findViewById(R.id.headerTitle)
        val headerSubtitle: TextView = toolbar.findViewById(R.id.headerSubtitle)

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
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_home -> {
                binding.navbarInclude.bottomNavigation.selectedItemId = R.id.nav_home
            }
            R.id.nav_avisos -> {
                binding.navbarInclude.bottomNavigation.selectedItemId = R.id.nav_avisos
            }
            R.id.nav_manutencoes -> {
                binding.navbarInclude.bottomNavigation.selectedItemId = R.id.nav_manutencoes
            }
            R.id.nav_perfil -> {
                val intent = Intent(this, Profile::class.java)
                startActivity(intent)
            }
            R.id.nav_ajuda -> {
                val intent = Intent(this, HelpActivity::class.java)
                startActivity(intent)
            }
        }

        // Fechar a sidebar após o clique
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
