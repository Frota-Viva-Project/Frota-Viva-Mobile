package com.mobile.frotaviva_mobile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.protobuf.Api
import com.mobile.frotaviva_mobile.adapter.MaintenanceAdapter
import com.mobile.frotaviva_mobile.api.ApiService
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.ActivityInsertMaintenanceBinding
import com.mobile.frotaviva_mobile.databinding.ActivityLoginBinding
import com.mobile.frotaviva_mobile.fragments.MaintenancesFragment
import com.mobile.frotaviva_mobile.fragments.MaintenancesFragment.Companion.TRUCK_ID_KEY
import com.mobile.frotaviva_mobile.model.MaintenanceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class InsertMaintenance : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityInsertMaintenanceBinding

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    companion object {
        const val TRUCK_ID_KEY = "truckId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsertMaintenanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBack.setOnClickListener {
            finish()
        }

        binding.buttonSendMaintenance.setOnClickListener {
            val titulo = binding.titleManInput.text.toString()
            val info = binding.descriptionManInput.text.toString()

            if (titulo.isEmpty() || info.isEmpty()) {
                Toast.makeText(this, "Preencha título e descrição.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val maintenance = MaintenanceRequest(
                titulo = titulo,
                info = info
            )

            sendMaintenanceToBackend(maintenance)
        }
    }

    private fun sendMaintenanceToBackend(maintenance: MaintenanceRequest) {
        val truckIdFromBundle = intent.getIntExtra(TRUCK_ID_KEY, 0)
        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSendMaintenance.isEnabled = false

        launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.sendMaintenance(truckIdFromBundle, maintenance)
                }
                if (response.isSuccessful) {
                    val resultIntent = Intent().apply {
                        putExtra(MaintenancesFragment.RELOAD_KEY, true)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    Toast.makeText(this@InsertMaintenance, "Manutenção enviada com sucesso!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    Toast.makeText(this@InsertMaintenance, "Erro ao enviar: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_CANCELED)
                }
            } catch (e: Exception) {
                Toast.makeText(this@InsertMaintenance, "Falha de rede: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                setResult(Activity.RESULT_CANCELED)
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.buttonSendMaintenance.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }
}