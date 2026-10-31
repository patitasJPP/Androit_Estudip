package com.Jair.practica.view.common

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.Jair.practica.model.entity.LoginRole

class AdministrarAccesosActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (LoginRole.fromCode(intent.getStringExtra("rol") ?: "") != LoginRole.ADMINISTRADOR_SAE) { finish(); return }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 32, 32, 32) }
        root.addView(TextView(this).apply { text = "Administrar accesos"; textSize = 24f })
        root.addView(TextView(this).apply { text = "Personal registrador: jair\nEstado: Habilitado" })
        listOf("Habilitar acceso", "Deshabilitar acceso", "Restablecer acceso").forEach { accion ->
            root.addView(Button(this).apply { text = accion; setOnClickListener { Toast.makeText(this@AdministrarAccesosActivity, "$accion aplicado", Toast.LENGTH_SHORT).show() } })
        }
        setContentView(root)
    }
}
