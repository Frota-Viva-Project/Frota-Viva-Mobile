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
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.databinding.ActivityInsertAlertBinding
import com.mobile.frotaviva_mobile.databinding.ActivityInsertMaintenanceBinding
import com.mobile.frotaviva_mobile.fragments.AlertsFragment
import com.mobile.frotaviva_mobile.fragments.MaintenancesFragment
import com.mobile.frotaviva_mobile.model.AlertRequest
import com.mobile.frotaviva_mobile.model.MaintenanceRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class InsertAlert : AppCompatActivity(), CoroutineScope{
    private lateinit var binding: ActivityInsertAlertBinding

    override val coroutineContext: CoroutineContext = Dispatchers.Main + Job()

    companion object {
        const val TRUCK_ID_KEY = "truckId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsertAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonBackAlert.setOnClickListener {
            finish()
        }

        binding.buttonSendAlert.setOnClickListener {
            val titulo = binding.titleAlertInput.text.toString()
            val descricao = binding.descriptionAlertInput.text.toString()
            val categoria = binding.categoryAlertInput.text.toString()

            if (titulo.isEmpty() || descricao.isEmpty() || categoria.isEmpty()) {
                Toast.makeText(this, "Preencha título e descrição.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val alert = AlertRequest(
                titulo = titulo,
                descricao = descricao,
                categoria = categoria,
            )

            sendAlertToBackend(alert)
        }
    }

    private fun sendAlertToBackend(alert: AlertRequest) {
        val truckIdFromBundle = intent.getIntExtra(TRUCK_ID_KEY, 0)
        binding.progressBar3.visibility = View.VISIBLE
        binding.buttonSendAlert.isEnabled = false

        launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.instance.sendAlert(truckIdFromBundle, alert)
                }
                if (response.isSuccessful) {
                    val resultIntent = Intent().apply {
                        putExtra(AlertsFragment.RELOAD_KEY, true)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    Toast.makeText(this@InsertAlert, "Aviso enviada com sucesso!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Erro desconhecido"
                    Toast.makeText(this@InsertAlert, "Erro ao enviar: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
                    setResult(Activity.RESULT_CANCELED)
                }
            } catch (e: Exception) {
                Toast.makeText(this@InsertAlert, "Falha de rede: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
                setResult(Activity.RESULT_CANCELED)
            } finally {
                binding.progressBar3.visibility = View.GONE
                binding.buttonSendAlert.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancel()
    }
}