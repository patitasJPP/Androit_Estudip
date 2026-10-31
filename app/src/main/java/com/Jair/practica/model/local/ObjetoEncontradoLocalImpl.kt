package com.Jair.practica.model.local

import com.Jair.practica.model.entity.ObjetoEncontrado
import com.Jair.practica.model.repository.ObjetoEncontradoRepository
import java.util.UUID

class ObjetoEncontradoLocalImpl : ObjetoEncontradoRepository {

    // Simulación de base de datos temporal en memoria
    private val objetos = mutableListOf<ObjetoEncontrado>()

    override fun findById(id: String): ObjetoEncontrado? {
        return objetos.find { it.id == id }
    }

    override fun findByCodigo(codigo: String): ObjetoEncontrado? {
        // Corregido a 'codigoCustodia' según la data class
        return objetos.find { it.codigoCustodia == codigo }
    }

    override fun list(): List<ObjetoEncontrado> {
        return objetos.toList()
    }

    override fun save(objeto: ObjetoEncontrado): ObjetoEncontrado {
        // RF-02: Se aplica el guardado del Hallazgo.
        // Si el id viene vacío desde el ViewModel, le asignamos un UUID.
        val nuevoObjeto = if (objeto.id.isEmpty()) {
            objeto.copy(id = UUID.randomUUID().toString())
        } else {
            objeto
        }
        objetos.add(nuevoObjeto)
        return nuevoObjeto
    }

    override fun update(objeto: ObjetoEncontrado): ObjetoEncontrado {
        // RF-03: Aquí se reemplaza el objeto cuando pasa a "Bajo custodia"
        val index = objetos.indexOfFirst { it.id == objeto.id }
        if (index != -1) {
            objetos[index] = objeto
            return objeto
        } else {
            throw IllegalArgumentException("El objeto no existe en el registro")
        }
    }
}