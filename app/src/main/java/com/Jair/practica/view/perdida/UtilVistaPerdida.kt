package com.Jair.practica.view.perdida

import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

fun AppCompatActivity.ajustarInsets(raiz: View) {
    ViewCompat.setOnApplyWindowInsetsListener(raiz) { vista, insets ->
        val m = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
        )
        vista.setPadding(m.left, m.top, m.right, m.bottom)
        insets
    }
    ViewCompat.requestApplyInsets(raiz)
}

/** Carga las opciones de un desplegable (los valores salen de CatalogoPerdidas). */
fun AutoCompleteTextView.cargarOpciones(opciones: List<String>) {
    setAdapter(ArrayAdapter(context, android.R.layout.simple_list_item_1, opciones))
}

/** Muestra un valor sin abrir ni filtrar la lista desplegable. */
fun AutoCompleteTextView.mostrarValor(valor: String?) {
    setText(valor.orEmpty(), false)
}
