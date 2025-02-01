package com.github.ParagonVirtuoso.memorias.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // TODO: Implementar o tratamento das mensagens
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Implementar o envio do token para o servidor
    }
} 