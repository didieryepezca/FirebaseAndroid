package com.example.firebasetutorial

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService // se importa al momento de inyectar la dependencia de FirebaseMessagingService()
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    //Para mostrar notificaciones en primer plano (Cuando la app esta abierta)
    override fun onMessageReceived(mensajeRemoto: RemoteMessage) {
        super.onMessageReceived(mensajeRemoto)
        Looper.prepare()

        Handler().post(){
            Toast.makeText(baseContext,mensajeRemoto.notification?.title, Toast.LENGTH_LONG).show()
        }
        Looper.loop()
    }


}