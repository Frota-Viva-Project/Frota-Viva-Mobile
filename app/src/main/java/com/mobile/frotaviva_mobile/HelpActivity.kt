package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var contentTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

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
        val helpInfo = """
            Olá! Precisa de uma mão? Aqui estão as respostas para as perguntas mais comuns.
            
            Como o aplicativo funciona?
            • Este aplicativo se conecta a um leitor OBDII instalado no seu caminhão. Ele lê informações de diagnóstico do veículo e nos ajuda a identificar problemas de forma preventiva, antes que eles causem paradas não programadas.
            
            O que significam os alertas de manutenção?
            • Os alertas são avisos sobre o estado do seu caminhão. Eles podem indicar a necessidade de uma troca de óleo, a verificação de um sensor ou outros detalhes importantes para a manutenção preventiva. Ao receber um alerta, fale com seu supervisor.
            
            O aplicativo substitui o mecânico?
            • Não. O aplicativo é uma ferramenta de apoio. As informações que ele fornece ajudam a equipe de manutenção a se preparar melhor, mas a palavra final sobre qualquer reparo é sempre do mecânico.
            
            O que faço se o aplicativo não funcionar?
            • Primeiro, verifique se a conexão com a internet está estável. Se o problema persistir, entre em contato com seu supervisor ou com o suporte técnico.
            
            Seus dados estão seguros! Os dados coletados são usados apenas para a manutenção e gestão de frota, conforme os Termos de Uso que você aceitou.
            
            Ainda com dúvidas? Fale diretamente com seu supervisor para qualquer questão relacionada ao seu trabalho e à manutenção do veículo.
        """.trimIndent()

        contentTextView.text = helpInfo
    }
}