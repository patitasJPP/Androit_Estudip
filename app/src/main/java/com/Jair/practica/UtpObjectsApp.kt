package com.Jair.practica

import android.app.Application

class UtpObjectsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PersistenciaLocal.inicializar(this)
    }
}
