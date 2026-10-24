package com.Jair.practica.view.custodia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class InventarioActivity : AppCompatActivity() {
    private lateinit var controller: InventarioController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)
        controller = InventarioController(this)
        controller.iniciar()
    }
}
