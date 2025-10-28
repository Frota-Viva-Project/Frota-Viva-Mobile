package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UserTermsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuração de tela inteira (se aplicável, mantenho o padrão do seu projeto)
        // enableEdgeToEdge()

        setContentView(R.layout.activity_user_terms)

        // Adiciona padding para barras do sistema, se necessário
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0) // Aplica padding apenas em cima/lados
            insets
        }

        // 1. Funcionalidade do Botão Voltar (seta)
        val backButton: ImageButton = findViewById(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Fecha a Activity e volta para a tela anterior
        }

        // 2. Inserção do Conteúdo
        val termosDeUsoText: TextView = findViewById(R.id.textTermosDeUso)

        // O texto com a formatação básica (quebras de linha e parágrafos)
        val textoCompleto = """
            Bem-vindo ao aplicativo que vai te ajudar a manter seu caminhão sempre em dia. Antes de começar a usar, é importante que você leia e entenda estes termos.

            1. Sobre o Aplicativo e a Ferramenta OBDII
            Este aplicativo foi criado para ajudar você, caminhoneiro, a fazer a manutenção preventiva do seu veículo. Ele funciona em conjunto com um leitor OBDII (On-Board Diagnostics, ou Diagnóstico de Bordo) que se conecta à central eletrônica do caminhão.
            A ferramenta OBDII coleta dados importantes sobre o estado do veículo e nos permite identificar possíveis problemas antes que eles se tornem graves.

            2. O que você pode fazer
            Você poderá visualizar informações de diagnóstico, acompanhar o estado do seu caminhão e receber alertas de manutenção. As informações são fornecidas para sua conveniência e como uma ferramenta de apoio ao seu trabalho.

            3. O que você não deve fazer
            • Não tente alterar, copiar ou fazer engenharia reversa do aplicativo.
            • Não use o aplicativo para fins ilegais ou para prejudicar o caminhão ou a empresa.
            • Não compartilhe seus dados de acesso com outras pessoas.

            4. Responsabilidade e Dados
            O aplicativo é uma ferramenta de apoio. As informações de diagnóstico não substituem a avaliação de um mecânico ou a manutenção recomendada pela Empresa.
            Os dados coletados pelo OBDII e pelo aplicativo, como informações de diagnóstico e códigos de falha, são propriedade da Empresa. Esses dados serão usados para fins de manutenção, análise de desempenho e gestão de frota.

            5. Sua conta e acesso
            Sua conta é pessoal e intransferível. O acesso ao aplicativo é fornecido pela Empresa e pode ser revogado a qualquer momento, conforme as políticas internas.
            Ao usar este aplicativo, você concorda com estes termos.
            Se tiver alguma dúvida, fale com seu supervisor ou com o departamento de manutenção.
            Agradecemos sua colaboração e bom trabalho!
        """.trimIndent()

        termosDeUsoText.text = textoCompleto
    }
}