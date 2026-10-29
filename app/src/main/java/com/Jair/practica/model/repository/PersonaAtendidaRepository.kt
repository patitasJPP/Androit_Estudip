package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.PersonaAtendida

interface PersonaAtendidaRepository {
    fun findById(id: String): PersonaAtendida?
    fun save(persona: PersonaAtendida): PersonaAtendida
    fun update(persona: PersonaAtendida): PersonaAtendida
}
