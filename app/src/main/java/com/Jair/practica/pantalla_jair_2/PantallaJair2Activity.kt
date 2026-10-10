package com.Jair.practica.pantalla_jair_2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class PantallaJair2Activity : AppCompatActivity() {
    private lateinit var controller: PantallaJair2Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_jair_2)
        controller = PantallaJair2Controller(this)
        controller.iniciar()
    }
}
