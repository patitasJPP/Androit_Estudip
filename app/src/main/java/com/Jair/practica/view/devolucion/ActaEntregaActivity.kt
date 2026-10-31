package com.Jair.practica.view.devolucion

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.view.common.NavegacionPrincipal

class ActaEntregaActivity : AppCompatActivity() {
    private lateinit var controller: ActaEntregaController
    var idEntrega: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        idEntrega = intent?.getStringExtra(EXTRA_ID_ENTREGA)
        setContentView(R.layout.activity_acta_entrega)
        NavegacionPrincipal.configurar(this, R.id.rootActaDevolucion)
        controller = ActaEntregaController(this)
        controller.iniciar()
    }

    companion object {
        const val EXTRA_ID_ENTREGA = "entregaId"
    }
}
