package com.Jair.practica.view.devolucion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.view.common.NavegacionPrincipal

class VerificacionEntregaActivity : AppCompatActivity() {
    private lateinit var controller: VerificacionEntregaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verificacion_entrega)
        NavegacionPrincipal.configurar(this, R.id.rootEntregaObjeto)
        controller = VerificacionEntregaController(this)
        controller.iniciar()
    }
}
