package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.UbicacionAlmacenamiento

interface UbicacionRepository {
    fun findById(id: String): UbicacionAlmacenamiento?
    fun listActive(): List<UbicacionAlmacenamiento>
    fun save(ubicacion: UbicacionAlmacenamiento): UbicacionAlmacenamiento
}
