package com.Jair.practica.pantalla_noe_2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R
import com.Jair.practica.pantalla_noe.NavegacionNoe

class PantallaNoe2Activity : AppCompatActivity() {
    private lateinit var controller: PantallaNoe2Controller

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_noe_2)
        NavegacionNoe.configurar(this, R.id.rootActaDevolucion)
        controller = PantallaNoe2Controller(this)
        controller.iniciar()
    }
}
