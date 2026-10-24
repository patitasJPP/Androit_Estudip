package com.Jair.practica.view.devolucion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class HistorialEntregasActivity : AppCompatActivity() {
    private lateinit var controller: HistorialEntregasController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_entregas)
        controller = HistorialEntregasController(this)
        controller.iniciar()
    }
}