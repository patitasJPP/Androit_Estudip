package com.Jair.practica.pantalla_jair_1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class PantallaJair1Activity : AppCompatActivity() {
    private lateinit var controller: PantallaJair1Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_jair_1)
        controller = PantallaJair1Controller(this)
        controller.iniciar()
    }
}
