package com.mobile.frotaviva_mobile

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.model.ChatMessage
import com.mobile.frotaviva_mobile.model.ChatRequest
import com.mobile.frotaviva_mobile.adapter.ChatAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChatBotActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var inputMessage: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton
    private val messageList = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        window.statusBarColor = ContextCompat.getColor(this, R.color.primary_default)
        ViewCompat.getWindowInsetsController(window.decorView)
            ?.isAppearanceLightStatusBars = false

        recyclerView = findViewById(R.id.recyclerChat)
        inputMessage = findViewById(R.id.inputMessage)
        sendButton = findViewById(R.id.btnSend)
        backButton = findViewById(R.id.btnBack)

        chatAdapter = ChatAdapter(messageList)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatAdapter

        backButton.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val userMessage = inputMessage.text.toString().trim()
            if (userMessage.isNotEmpty()) {
                addMessage(ChatMessage(userMessage, "user"))
                inputMessage.setText("")
                sendMessageToBot(userMessage)
            }
        }

        addMessage(ChatMessage("Bem Vindo! No que posso ajudar?", "bot"))
    }

    private fun addMessage(message: ChatMessage) {
        messageList.add(message)
        chatAdapter.notifyItemInserted(messageList.size - 1)
        recyclerView.scrollToPosition(messageList.size - 1)
    }

    private fun formatBotResponse(jsonText: String?): String {
        if (jsonText.isNullOrEmpty()) return "Erro ao processar resposta"

        return try {
            val cleanedJson = jsonText
                .removePrefix("json")
                .removeSuffix("")
                .trim()

            val jsonObj = JSONObject(cleanedJson)

            val resposta = when {
                jsonObj.has("resposta") -> jsonObj.getString("resposta")
                jsonObj.has("recomendacao") -> jsonObj.getString("recomendacao")
                else -> cleanedJson
            }

            resposta.replace(Regex("\n+"), "\n").trim()
        } catch (e: Exception) {
            jsonText.trim()
        }
    }

    private fun sendMessageToBot(userMessage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val chatRequest = ChatRequest(mensagem = userMessage)
                val response = RetrofitClient.chatbotInstance.sendMessageToChatbot(chatRequest)

                if (response.isSuccessful) {
                    val rawBotReply = response.body()?.resposta ?: "Erro ao processar resposta"
                    val formattedReply = formatBotResponse(rawBotReply)

                    withContext(Dispatchers.Main) {
                        addMessage(ChatMessage(formattedReply, "bot"))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        addMessage(ChatMessage("Erro: ${response.code()}", "bot"))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    addMessage(ChatMessage("Erro de conexão: ${e.message}", "bot"))
                }
            }
        }
    }
}