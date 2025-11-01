# 🚛 Frota Viva Mobile

O **Frota Viva Mobile** é o aplicativo do ecossistema **Frota Viva**, criado para ajudar caminhoneiros e empresas a gerirem suas frotas com eficiência.  
Conectado ao veículo via Arduino OBD-II e à API da plataforma, o app permite monitorar o caminhão em tempo real, receber alertas automáticos de falhas e manutenções, e se comunicar diretamente com a central.

---

## 📱 Sobre o Projeto

O app é desenvolvido em **Kotlin (Android nativo)**, com integração ao backend do Frota Viva.  
Ele oferece uma experiência simples e intuitiva para o motorista, garantindo acesso rápido a dados técnicos, histórico de manutenções e alertas críticos.

### Principais Funcionalidades

- **📡 Monitoramento do Veículo** – Dados técnicos e status em tempo real  
- **🔧 Gestão de Manutenções** – Agendamento e histórico de serviços  
- **🚨 Alertas Automáticos** – Notificações push via **Firebase Cloud Messaging (FCM)**  
- **🔒 Autenticação Segura** – Login via API (JWT) e integração com Firebase Auth  
- **🧠 Integração Arduino OBD-II** – Leitura direta dos sensores do caminhão  
- **🗺️ Mapa e Localização** – Exibição da posição atual e trajetos  
- **🧾 Painel do Motorista** – Acesso às informações do veículo e manutenções  

---

## 🧰 Tecnologias Utilizadas

### Mobile
- **Kotlin** – Linguagem principal  
- **Android SDK + Jetpack Components**  
- **Retrofit 2** – Requisições HTTP à API  
- **Gson** – Conversão JSON  
- **Firebase Cloud Messaging (FCM)** – Notificações Push  
- **Firebase Auth** – Autenticação  

### Integrações
- **API Frota Viva (Spring Boot)**  
- **Firebase Console** – Gerenciamento de tokens e mensagens  
- **Arduino OBD-II** – Coleta de dados técnicos  

---

## ⚙️ Pré-requisitos

Antes de rodar o projeto, verifique se possui:

- **Android Studio Jellyfish (ou superior)**  
- **SDK Android 34+**  
- **Emulador Android** (ou dispositivo físico conectado)  
- **Conta Firebase** configurada  

---

## 🚀 Como Executar o Projeto

### 1️⃣ Clonar o Repositório
```bash
git clone https://github.com/Frota-Viva-Project/Frota-Viva-Mobile.git
cd Frota-Viva-Mobile 
```

### 2️⃣ Configurar o Firebase

- Crie um projeto no Firebase Console

- Baixe o arquivo google-services.json

- Coloque-o dentro da pasta:
    -app/google-services.json

- Ative o Firebase Cloud Messaging e o Firebase Authentication no painel

### 3️⃣ Configurar o Backend

Certifique-se de que o Frota Viva API está rodando localmente ou em produção.
Atualize a URL base no Retrofit (arquivo ApiClient.kt):

```basg
  private const val BASE_URL = "http://<SEU_SERVIDOR>:8080/v1/api/"
```

### 4️⃣ Executar o App

No Android Studio:

Selecione o dispositivo ou emulador

Clique em ▶️ Run App

### 🔔 Configuração de Notificações

As notificações push são enviadas via Firebase Cloud Messaging (FCM).

O token FCM é gerado automaticamente na inicialização do app.

Ele é enviado à API via endpoint:

```bash
  PUT /v1/api/fcm/register
```

O backend utiliza o token para enviar alertas de manutenção, falhas e eventos importantes.

### 🧑‍💻 Estrutura de Pastas
```bash
  app/
   ├── java/com/mobile/frotaviva_mobile/
   │   ├── adapter/              # Adapters das Recycler View
   │   ├── api/                  # Integração com o retrofit para chamadas ao back
   │   ├── auth/                 # Serviço de autenticação
   │   ├── model/                # Modelos de dados
   │   ├── storage/              # Armazenamento de segurança
   │   ├── worker/               # Worker para armazenar a localização do motorista
   │   └── MainActivity.kt
   ├── res/                   # Layouts, ícones e temas
   └── google-services.json   # Configuração Firebase
```
### 🔐 Autenticação

A autenticação combina Firebase Auth e JWT (via API).

O usuário faz login com email e senha.

O Firebase retorna o token de autenticação.

O app envia o token à API do Frota Viva, que valida e gera o JWT.

### 🧭 Principais Telas

Tela de Login – Autenticação via Firebase

Tela Inicial – Status do caminhão, alertas recentes e mapa

Manutenções – Histórico e criação de manutenções

Alertas – Recebimento em tempo real das notificações enviadas pelo OBDII

Perfil do Motorista – Dados e configurações

### 📄 Licença

Este projeto é distribuído sob a licença MIT.
Consulte o arquivo LICENSE para mais detalhes.
