# **IMemórias**
**📱 Aplicativo Android para organizar momentos especiais através de vídeos do YouTube!**

<div align="center">
  <img src="https://img.shields.io/badge/Kotlin-100%25-blue?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/Architecture-MVVM%20%2B%20Clean-orange" alt="Arquitetura">
  <img src="https://img.shields.io/badge/YouTube-API-red?logo=youtube" alt="YouTube API">
</div>

---

## **🔍 Sobre o Projeto**
O **IMemórias** é um aplicativo Android que transforma a maneira como você guarda seus momentos especiais através de vídeos do YouTube. Com ele, você pode:

- Buscar e salvar vídeos do YouTube relacionados a eventos importantes
- Criar playlists personalizadas para cada ocasião especial usando a API do YouTube
- Marcar vídeos como favoritos para acesso rápido
- Agendar lembretes para assistir vídeos mais tarde

Perfeito para eternizar memórias de casamentos, aniversários, formaturas e outros momentos únicos da sua vida através da plataforma YouTube.

---

## **✨ Funcionalidades Principais**
- **🔐 Login com Google**
  Autenticação segura via OAuth 2.0 do Google.
- **🎥 Player de Vídeos Integrado**
  Reprodução de vídeos diretamente no app usando YouTube iFrame Player API.
- **❤️ Favoritos e Playlists**
  Gerenciamento de vídeos e playlists com sincronização local (Room) e YouTube Data API.
- **🔔 Notificações Push**
  Sistema inteligente de notificações com deep linking para vídeos.
- **📖 Política de Privacidade**
  Conformidade com as diretrizes do YouTube e Google Play Store.

---

## **🛠 Tecnologias e Bibliotecas**
- **Linguagem:** Kotlin
- **Arquitetura:** MVVM + Clean Architecture
- **APIs e SDKs:**
  - YouTube Data API v3
  - YouTube iFrame Player API
  - Google Sign-In SDK
- **Bibliotecas:**
  - **Jetpack:** Room, Navigation Component, ViewModel, LiveData
  - **Firebase:** Authentication, Cloud Messaging
  - **Retrofit + Gson:** Integração com APIs REST
  - **Coil:** Carregamento eficiente de imagens
  - **Hilt:** Injeção de dependências

---

## **⚙️ Como Configurar**
1. **Credenciais do YouTube:**
   - Crie um projeto no Google Cloud Console
   - Habilite a YouTube Data API v3
   - Adicione no `local.properties`:
     ```properties
     YOUTUBE_API_KEY="SUA_CHAVE_AQUI"
     ```
2. **Firebase:**
   - Configure o `google-services.json` para autenticação e notificações