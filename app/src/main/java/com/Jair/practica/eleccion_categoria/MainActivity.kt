package com.Jair.practica.eleccion_categoria

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class MainActivity : AppCompatActivity() {
    private lateinit var controller: MainController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_eleccion_categoria)
        controller = MainController(this)
        controller.iniciar()
    }
}