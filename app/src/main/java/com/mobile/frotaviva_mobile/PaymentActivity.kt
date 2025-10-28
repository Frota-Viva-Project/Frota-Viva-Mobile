package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var contentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        backButton = findViewById(R.id.backButton)
        contentTextView = findViewById(R.id.contentTextView)

        setupBackButton()
        setupContent()
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupContent() {
        val paymentInfo = """
            Sem preocupações para você!
            
            Este aplicativo não gerencia pagamentos nem cobra por nenhum serviço. Todos os custos relacionados à manutenção do caminhão, peças, diagnósticos via OBDII e uso do aplicativo são de responsabilidade da Empresa.
            
            Sua única responsabilidade é usar o aplicativo para acompanhar o estado do veículo e nos ajudar a manter a frota em perfeito funcionamento.
            
            Em caso de dúvidas sobre despesas de manutenção, fale com seu supervisor ou com o departamento responsável na empresa.
        """.trimIndent()

        contentTextView.text = paymentInfo
    }
}