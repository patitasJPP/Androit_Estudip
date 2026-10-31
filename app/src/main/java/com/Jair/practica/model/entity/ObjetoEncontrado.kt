package com.Jair.practica.model.entity

data class ObjetoEncontrado(
    val id: String,
    val codigoCustodia: String,
    val categoria: String,
    val descripcion: String,
    val caracteristicasReservadas: String? = null,
    val lugarHallazgo: String? = null,
    val fechaHallazgo: String? = null,
    val registradoPor: String,
    val estado: EstadoObjeto = EstadoObjeto.HALLAZGO_REPORTADO,
    val ubicacionId: String? = null,
    val contactoHalladorId: String? = null,
    // --- Integrante 3 (RF-06): datos PÚBLICOS para filtrar coincidencias ---
    val color: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val pabellon: String? = null,
    val piso: String? = null,
    val aula: String? = null
)
