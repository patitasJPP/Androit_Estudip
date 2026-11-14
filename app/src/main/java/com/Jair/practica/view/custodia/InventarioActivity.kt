package com.Jair.practica.view.custodia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import android.os.Handler
import android.os.Looper

class InventarioActivity : AppCompatActivity() {
    private lateinit var controller: InventarioController
    private val refresco = Handler(Looper.getMainLooper())
    private val tareaRefresco = object : Runnable {
        override fun run() {
            controller.actualizarDesdeSupabase()
            refresco.postDelayed(this, 5_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventario)
        controller = InventarioController(this)
        controller.iniciar()
    }

    override fun onResume() {
        super.onResume()
        refresco.post(tareaRefresco)
    }

    override fun onPause() {
        refresco.removeCallbacks(tareaRefresco)
        super.onPause()
    }
}
