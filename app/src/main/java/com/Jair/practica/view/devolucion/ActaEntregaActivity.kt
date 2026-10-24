package com.Jair.practica.view.devolucion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.view.common.NavegacionPrincipal

class ActaEntregaActivity : AppCompatActivity() {
    private lateinit var controller: ActaEntregaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acta_entrega)
        NavegacionPrincipal.configurar(this, R.id.rootActaDevolucion)
        controller = ActaEntregaController(this)
        controller.iniciar()
    }
}
