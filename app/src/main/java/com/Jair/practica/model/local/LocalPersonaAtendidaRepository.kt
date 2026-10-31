package com.Jair.practica.model.local

import com.Jair.practica.model.entity.PersonaAtendida
import com.Jair.practica.model.repository.PersonaAtendidaRepository

class LocalPersonaAtendidaRepository(
    private val store: LocalDataStore = AlmacenLocal.instancia
) : PersonaAtendidaRepository {

    override fun findById(id: String): PersonaAtendida? =
        store.personas.firstOrNull { it.id == id }

    override fun save(persona: PersonaAtendida): PersonaAtendida {
        val index = store.personas.indexOfFirst { it.id == persona.id }
        if (index >= 0) {
            store.personas[index] = persona
        } else {
            store.personas.add(persona)
        }
        return persona
    }

    override fun update(persona: PersonaAtendida): PersonaAtendida {
        val index = store.personas.indexOfFirst { it.id == persona.id }
        if (index >= 0) {
            store.personas[index] = persona
        }
        return persona
    }
}
