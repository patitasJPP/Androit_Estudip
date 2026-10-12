package com.Jair.practica.pantalla_hallazgo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class PantallaHallazgoActivity : AppCompatActivity() {
    private lateinit var controller: PantallaHallazgoController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_hallazgo)
        controller = PantallaHallazgoController(this)
        controller.iniciar()
    }
}
