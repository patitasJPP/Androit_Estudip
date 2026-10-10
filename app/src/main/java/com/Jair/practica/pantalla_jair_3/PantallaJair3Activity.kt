package com.Jair.practica.pantalla_jair_3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class PantallaJair3Activity : AppCompatActivity() {
    private lateinit var controller: PantallaJair3Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_jair_3)
        controller = PantallaJair3Controller(this)
        controller.iniciar()
    }
}