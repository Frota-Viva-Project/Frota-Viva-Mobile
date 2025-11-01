package com.mobile.frotaviva_mobile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.auth.JwtUtils
import com.mobile.frotaviva_mobile.databinding.ActivityMainBinding
import com.mobile.frotaviva_mobile.storage.SecureStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    private lateinit var secureStorage: SecureStorage

    private lateinit var toggle: ActionBarDrawerToggle
    var truckId: Int? = null

    private lateinit var headerTitle: TextView
    private lateinit var navHeaderUserName: TextView
    private lateinit var headerSubtitle: TextView
    private var navHeaderDate: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance()
        )

        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_default)
        ViewCompat.getWindowInsetsController(window.decorView)
            ?.isAppearanceLightStatusBars = false

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        secureStorage = SecureStorage(this)

        val toolbar = binding.headerInclude.root
        setSupportActionBar(toolbar)

        headerTitle = toolbar.findViewById(R.id.headerTitle)
        headerSubtitle = toolbar.findViewById(R.id.headerSubtitle)

        val navHeaderView = binding.navView.getHeaderView(0)
        navHeaderUserName = navHeaderView.findViewById(R.id.textViewUserName)
        navHeaderDate = navHeaderView.findViewById(R.id.textViewDate)

        val iconNotifications = toolbar.findViewById<ImageView>(R.id.icon_notifications)
        Log.d("DropdownDebug", "iconNotifications encontrado? ${iconNotifications != null}")

        iconNotifications.setOnClickListener {
            Log.d("DropdownDebug", "Clicou no ícone de notificações")
            showNotificationsDropdown(it)
        }

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

        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)
        headerSubtitle.text = formattedDate
        navHeaderDate?.text = formattedDate

        val bottomNavigationView = binding.navbarInclude.bottomNavigation

        requestNotificationPermission()

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

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onResume() {
        super.onResume()

        val token = secureStorage.getToken()
        if (token.isNullOrEmpty() || JwtUtils.isTokenExpired(token)) {
            redirectToLogin()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        val userName = user?.displayName
        val defaultName = "Usuário"

        if (!userName.isNullOrEmpty()) {
            updateHeaderName(userName)
        } else {
            updateHeaderName(defaultName)
        }
    }

    fun updateHeaderName(name: String) {
        headerTitle.text = "Olá, $name"
        navHeaderUserName.text = name
    }

    private fun redirectToLogin() {
        val intent = Intent(this, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun showNotificationsDropdown(anchorView: View) {
        val inflater = layoutInflater
        val popupView = inflater.inflate(R.layout.layout_notifications_dropdown, null)
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.elevation = 10f
        popupWindow.isOutsideTouchable = true

        val container = popupView.findViewById<LinearLayout>(R.id.containerNotifications)
        val txtEmpty = popupView.findViewById<TextView>(R.id.txtEmpty)

        lifecycleScope.launch {
            try {
                val userId = fetchTruckId()
                if (userId != null) {
                    val response = RetrofitClient.instance.getNotificationHistory(userId)
                    if (response.isSuccessful) {
                        val notifications = response.body().orEmpty()
                        if (notifications.isEmpty()) {
                            txtEmpty.visibility = View.VISIBLE
                        } else {
                            notifications.take(5).forEach { notif ->
                                val item = TextView(this@MainActivity).apply {
                                    text = "Nova Notificação: ${notif.title ?: "Sem título"}"
                                    setPadding(8, 8, 8, 8)
                                    setTextColor(ContextCompat.getColor(context, R.color.black))
                                    setOnClickListener {
                                        Toast.makeText(context, notif.title, Toast.LENGTH_SHORT).show()
                                        popupWindow.dismiss()
                                    }
                                }
                                container.addView(item)
                            }
                        }
                    } else {
                        txtEmpty.visibility = View.VISIBLE
                    }
                } else {
                    txtEmpty.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                txtEmpty.visibility = View.VISIBLE
            }
        }

        popupWindow.showAsDropDown(anchorView, -280, 5)
    }

    private fun fetchTruckId(): Int? {
        if (truckId != null && truckId!! > 0) {
            return truckId
        }

        val sharedPref = getSharedPreferences("truck_prefs", Context.MODE_PRIVATE)
        val savedId = sharedPref.getInt("TRUCK_ID", -1)

        return if (savedId > 0) {
            truckId = savedId
            savedId
        } else {
            null
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

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.d("Permissao", "Permissão de notificação concedida.")
                } else {
                    Log.w("Permissao", "Permissão de notificação negada.")
                }
            }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}