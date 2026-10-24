package com.Jair.practica.view.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class SeleccionRolActivity : AppCompatActivity() {
    private lateinit var controller: SeleccionRolController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion_rol)
        controller = SeleccionRolController(this)
        controller.iniciar()
    }
}