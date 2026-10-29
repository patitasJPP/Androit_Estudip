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
    val contactoHalladorId: String? = null
)
