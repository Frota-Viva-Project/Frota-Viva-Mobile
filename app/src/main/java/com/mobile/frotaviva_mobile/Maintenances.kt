package com.mobile.frotaviva_mobile

import VerticalSpaceItemDecoration
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.adapter.MaintenanceAdapter
import com.mobile.frotaviva_mobile.model.Maintenance
import java.util.Calendar
import java.util.Date

class Maintenances : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_maintenances)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val recyclerView = findViewById<RecyclerView>(R.id.maintenances_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(VerticalSpaceItemDecoration(dpToPx(24)))

        val fakeMaintenanceData = listOf(
            Maintenance(
                titulo = "Oil Change",
                info = "Routine maintenance. Oil",
                dataOcorrido = createDate(2025, 9, 20, 10, 30),
                status = "Completed"
            ),
            Maintenance(
                titulo = "Electrical System Problem",
                info = "Left headlight is not working.",
                dataOcorrido = createDate(2025, 9, 23, 15, 0),
                status = "In Progress"
            ),
            Maintenance(
                titulo = "Brake Repair",
                info = "Rear brake pads ",
                dataOcorrido = createDate(2025, 9, 24, 9, 0),
                status = "Pending"
            ),
            Maintenance(
                titulo = "Engine Noise",
                info = "Strange noise detected",
                dataOcorrido = createDate(2025, 9, 25, 11, 0),
                status = "Pending"
            )
        )

        recyclerView.adapter = MaintenanceAdapter(fakeMaintenanceData)
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        ).toInt()
    }
    private fun createDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day, hour, minute, 0)
        return calendar.time
    }

}