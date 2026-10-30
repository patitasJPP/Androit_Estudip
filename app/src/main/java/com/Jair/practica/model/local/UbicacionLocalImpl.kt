package com.Jair.practica.model.local

import com.Jair.practica.model.entity.UbicacionAlmacenamiento
import com.Jair.practica.model.repository.UbicacionRepository

class UbicacionLocalImpl : UbicacionRepository {

    // Lista mutable para simular la base de datos temporal
    private val ubicaciones = mutableListOf<UbicacionAlmacenamiento>()

    override fun findById(id: String): UbicacionAlmacenamiento? {
        // Nota: Si el identificador en la entidad no se llama 'id', cámbialo aquí
        return ubicaciones.find { it.id == id }
    }

    override fun listActive(): List<UbicacionAlmacenamiento> {
        // Retorna las ubicaciones disponibles para mostrarlas al Administrador SAE en el RF-03.
        // Si tu entidad tiene un campo booleano como 'activo', podrías usar: return ubicaciones.filter { it.activo }
        return ubicaciones.toList()
    }

    override fun save(ubicacion: UbicacionAlmacenamiento): UbicacionAlmacenamiento {
        val index = ubicaciones.indexOfFirst { it.id == ubicacion.id }
        if (index != -1) {
            ubicaciones[index] = ubicacion // Actualiza si ya existe
        } else {
            ubicaciones.add(ubicacion) // Agrega si es nueva
        }
        return ubicacion
    }
}