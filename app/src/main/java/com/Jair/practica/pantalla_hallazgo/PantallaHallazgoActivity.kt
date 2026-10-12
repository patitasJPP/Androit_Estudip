package com.Jair.practica.pantalla_hallazgo

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class PantallaHallazgoActivity : AppCompatActivity() {
    private lateinit var controller: PantallaHallazgoController

    private val tomarFoto = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        controller.mostrarFoto(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_hallazgo)
        controller = PantallaHallazgoController(this) { tomarFoto.launch(null) }
        controller.iniciar()
    }
}
