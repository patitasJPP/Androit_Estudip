package com.Jair.practica.view.hallazgo

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.R

class RegistroHallazgoActivity : AppCompatActivity() {
    private lateinit var controller: RegistroHallazgoController

    private val tomarFoto = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        controller.mostrarFoto(bitmap)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_hallazgo)
        controller = RegistroHallazgoController(this) { tomarFoto.launch(null) }
        controller.iniciar()
    }
}
