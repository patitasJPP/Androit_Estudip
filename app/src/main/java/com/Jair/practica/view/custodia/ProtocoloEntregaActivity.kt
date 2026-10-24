package com.Jair.practica.view.custodia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class ProtocoloEntregaActivity : AppCompatActivity() {
    private lateinit var controller: ProtocoloEntregaController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_protocolo_entrega)
        controller = ProtocoloEntregaController(this)
        controller.iniciar()
    }
}
