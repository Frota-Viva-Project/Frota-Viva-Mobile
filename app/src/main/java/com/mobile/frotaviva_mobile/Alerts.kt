package com.mobile.frotaviva_mobile

import VerticalSpaceItemDecoration
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.View.generateViewId
import android.widget.FrameLayout
import android.widget.ScrollView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.internal.ViewUtils.dpToPx
import com.mobile.frotaviva_mobile.adapter.AlertAdapter
import com.mobile.frotaviva_mobile.model.Alert

class Alerts : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_alerts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.alertRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))

        val dadosFake = listOf(
            Alert("Aviso urgente", "Temperatura do Motor maior que 105 °C"),
            Alert("Aviso intermediário", "Nível de combustível entre 20% e 25%"),
            Alert("Aviso simples", "Nível de água entre 40% e 50%"),
            Alert("Aviso urgente", "Temperatura do Motor maior que 105 °C"),
            Alert("Aviso intermediário", "Nível de combustível entre 20% e 25%"),
            Alert("Aviso simples", "Nível de água entre 40% e 50%")
        )

        recyclerView.adapter = AlertAdapter(dadosFake)

        setupDropdown()
    }

    private fun setupDropdown() {
        val placeholder = findViewById<FrameLayout>(R.id.dropdownContainer)

        // Inflar o layout do dropdown **dentro do placeholder**
        layoutInflater.inflate(R.layout.dropdown, placeholder, true)
    }
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }
}