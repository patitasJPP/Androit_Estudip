package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.LoginRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAuthRepositoryTest {
    private val repository = DemoAuthRepository()

    @Test
    fun preservesExistingDemoCredentialsForEachRole() {
        assertTrue(repository.authenticate(LoginRole.HALLADOR, "jair", "jair"))
        assertTrue(repository.authenticate(LoginRole.SERVICIO, "jair", "jair123"))
        assertFalse(repository.authenticate(LoginRole.HALLADOR, "jair", "jair123"))
        assertFalse(repository.authenticate(LoginRole.SERVICIO, "jair", "jair"))
    }
}
