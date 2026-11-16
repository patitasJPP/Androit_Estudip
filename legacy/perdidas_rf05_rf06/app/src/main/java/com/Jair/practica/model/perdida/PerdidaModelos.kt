package com.Jair.practica.model.perdida

/*
 * Tipos de datos del módulo de pérdidas
 * Son clases simples sin dependencias de Android, para poder probarlas con tests unitarios.
 */

/**
 * Lo que el Personal registrador escribe en el formulario de RF-05.
 * Todo es texto crudo ("" = no escrito); la normalización y la validación las hace
 * [ValidadorReportePerdida] y el repositorio.
 */
data class DatosReportePerdida(
    val descripcion: String = "",
    val lugarAproximado: String = "",
    val categoria: String = "",
    val color: String = "",
    val marca: String = "",
    val modelo: String = "",
    val pabellon: String = "",
    val piso: String = "",
    val aula: String = "",
    val contactoNombre: String = "",
    val contactoCorreo: String = "",
    val contactoTelefono: String = ""
) {

    fun estaVacio(): Boolean = listOf(
        descripcion, lugarAproximado, categoria, color, marca, modelo,
        pabellon, piso, aula, contactoNombre, contactoCorreo, contactoTelefono
    ).all { it.isBlank() }
}

/**
 * Filtros de RF-06: pabellón, piso, aula, categoría, color, marca y modelo.
 * Un filtro vacío no restringe. Todos los filtros escritos deben cumplirse a la vez (Y).
 */
data class FiltrosCoincidencia(
    val pabellon: String = "",
    val piso: String = "",
    val aula: String = "",
    val categoria: String = "",
    val color: String = "",
    val marca: String = "",
    val modelo: String = ""
) {
    fun hayAlguno(): Boolean = listOf(pabellon, piso, aula, categoria, color, marca, modelo)
        .any { it.isNotBlank() }
}

data class CoincidenciaSugerida(
    val objetoId: String,
    val codigoCustodia: String,
    val categoria: String,
    val descripcion: String,
    val color: String?,
    val marca: String?,
    val modelo: String?,
    val pabellon: String?,
    val piso: String?,
    val aula: String?,
    val lugarHallazgo: String?,
    val fechaHallazgo: String?
)

data class ErroresReporte(
    val descripcion: String? = null,
    val lugarAproximado: String? = null,
    val categoria: String? = null,
    val color: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val pabellon: String? = null,
    val piso: String? = null,
    val aula: String? = null,
    val contactoNombre: String? = null,
    val contactoCorreo: String? = null,
    val contactoTelefono: String? = null,
    val medioContacto: String? = null
) {
    val hayErrores: Boolean
        get() = listOf(
            descripcion, lugarAproximado, categoria, color, marca, modelo, pabellon, piso, aula,
            contactoNombre, contactoCorreo, contactoTelefono, medioContacto
        ).any { it != null }
}
