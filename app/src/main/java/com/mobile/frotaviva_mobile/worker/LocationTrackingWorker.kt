package com.mobile.frotaviva_mobile.worker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.model.LocationUpdateRequest
import kotlinx.coroutines.tasks.await
import retrofit2.HttpException

class LocationTrackingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val db = FirebaseFirestore.getInstance()
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    private val sharedPref = appContext.getSharedPreferences("truck_prefs", Context.MODE_PRIVATE)

    companion object {
        const val WORK_TAG = "LocationTrackingWork"
        private const val TRUCK_ID_PREF_KEY = "TRUCK_ID"
        private const val ID_MAPS_PREF_KEY = "ID_MAPS"
        private const val LOG_TAG_DATA = "TRACK_DATA" // Nova tag para dados de localização (Substitui "RUBERTS")
    }

    override suspend fun doWork(): Result {
        val truckId: Int = sharedPref.getInt(TRUCK_ID_PREF_KEY, 0)
        val idMapsInt: Int = sharedPref.getInt(ID_MAPS_PREF_KEY, 0)

        // Converte o ID_MAPS para String se for um valor válido, caso contrário, será null
        val idMaps: String? = if (idMapsInt > 0) idMapsInt.toString() else null

        Log.d(WORK_TAG, "Iniciando doWork(). Truck ID: $truckId. ID Maps: $idMaps")

        if (truckId <= 0) {
            Log.e(WORK_TAG, "Truck ID não encontrado nas preferências. Falha.")
            return Result.failure()
        }

        try {
            val location: Location? = getLastLocation()
            if (location == null) {
                Log.w(WORK_TAG, "Localização indisponível. Tentando novamente na próxima execução.")
                // Retorna sucesso para não tentar imediatamente, mas sim no próximo ciclo periódico
                return Result.success()
            }

            val locationData = LocationUpdateRequest(
                truckId = truckId,
                latitude = location.latitude,
                longitude = location.longitude,
                capturaLocalizacaoMl = System.currentTimeMillis()
            )

            // Log de Dados de Localização (Seu antigo "RUBERTS")
            Log.i(LOG_TAG_DATA, "Lat/Lng obtida: ${locationData.latitude}, ${locationData.longitude}")

            if (idMaps.isNullOrEmpty()) {
                handlePostLocation(locationData)
            } else {
                handlePutLocation(idMaps, locationData)
            }

            return Result.success()

        } catch (e: HttpException) {
            Log.e(WORK_TAG, "Erro HTTP: ${e.code()}. Tentando novamente.", e)
            return Result.retry()
        } catch (e: Exception) {
            Log.e(WORK_TAG, "Erro geral no Worker: ${e.message}", e)
            return Result.failure()
        }
    }

    // Removendo o truckId do parâmetro pois ele já está em locationData
    private suspend fun handlePostLocation(locationData: LocationUpdateRequest) {
        val response = RetrofitClient.instance.postLocation(locationData)
        val truckId = locationData.truckId

        if (response.isSuccessful) {
            val newIdMaps = response.body()

            if (newIdMaps != null) {
                with(sharedPref.edit()) {
                    putInt(ID_MAPS_PREF_KEY, newIdMaps)
                    apply()
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (!userId.isNullOrEmpty()) {
                    db.collection("driver").document(userId).update("idMaps", newIdMaps).await()
                }

                Log.i(WORK_TAG, "Primeiro POST bem-sucedido. ID Maps salvo: $newIdMaps")
            } else {
                Log.e(WORK_TAG, "POST bem-sucedido, mas ID Maps não retornado no corpo.")
                throw IllegalStateException("ID Maps não recebido do servidor.")
            }
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e(WORK_TAG, "POST falhou: ${response.code()}, Body: $errorBody")
            throw HttpException(response)
        }
    }

    private suspend fun handlePutLocation(idMaps: String, locationData: LocationUpdateRequest) {
        val response = RetrofitClient.instance.putLocation(idMaps, locationData)

        if (response.isSuccessful) {
            // Logs de dados transferidos
            Log.i(LOG_TAG_DATA, "PUT: Lat/Lng: ${locationData.latitude}, ${locationData.longitude}")
            Log.i(WORK_TAG, "PUT bem-sucedido para ID Maps: $idMaps")
        } else {
            val errorBody = response.errorBody()?.string()
            Log.e(WORK_TAG, "PUT falhou: ${response.code()}, Body: $errorBody")

            if (response.code() == 404 || response.code() == 410) {
                with(sharedPref.edit()) {
                    remove(ID_MAPS_PREF_KEY)
                    apply()
                }
                Log.w(WORK_TAG, "ID Maps removido das SharedPreferences. Forçando POST na próxima execução.")
            }
            throw HttpException(response)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLastLocation(): Location? {
        if (!checkLocationPermission()) {
            Log.e(WORK_TAG, "Permissão de localização negada.")
            return null
        }

        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            Log.e(WORK_TAG, "Falha ao obter última localização: ${e.message}")
            null
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}