package com.Jair.practica.model.perdida

import java.text.Normalizer

object CatalogoPerdidas {
    val PABELLONES = listOf("A", "B", "C", "D")
    val PISOS = (1..8).map { it.toString() }
    val CATEGORIAS = listOf(
        "Laptop", "Celular", "Documentos", "Billetera", "Mochila", "Llaves", "Audífonos", "Otros"
    )
    val COLORES = listOf(
        "Negro", "Blanco", "Gris", "Azul", "Rojo", "Verde", "Amarillo", "Marrón", "Otro"
    )

    /** true si [valor] es vacío (no se eligió nada) o pertenece a [catalogo] sin importar mayúsculas/tildes. */
    fun estaEn(valor: String, catalogo: List<String>): Boolean =
        valor.isBlank() || catalogo.any { TextoPerdida.normalizar(it) == TextoPerdida.normalizar(valor) }
}

/** Utilidades de texto para comparar sin distinguir mayúsculas, tildes ni espacios repetidos. */
object TextoPerdida {
    private val MARCAS_DIACRITICAS = Regex("\\p{InCombiningDiacriticalMarks}+")
    private val ESPACIOS = Regex("\\s+")

    fun normalizar(texto: String?): String {
        if (texto == null) return ""
        val sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
            .replace(MARCAS_DIACRITICAS, "")
        return sinTildes.lowercase().replace(ESPACIOS, " ")
    }
}
