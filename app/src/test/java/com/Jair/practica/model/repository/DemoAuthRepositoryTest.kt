package com.Jair.practica.model.repository

import com.Jair.practica.model.entity.LoginRole
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoAuthRepositoryTest {
    private val repository = DemoAuthRepository()

    @Test
    fun preservesExistingDemoCredentialsForEachRole() {
        assertTrue(repository.authenticate(LoginRole.PERSONAL_REGISTRADOR, "jair", "jair"))
        assertTrue(repository.authenticate(LoginRole.ADMINISTRADOR_SAE, "jair", "jair123"))
        assertFalse(repository.authenticate(LoginRole.PERSONAL_REGISTRADOR, "jair", "jair123"))
        assertFalse(repository.authenticate(LoginRole.ADMINISTRADOR_SAE, "jair", "jair"))
    }
}
