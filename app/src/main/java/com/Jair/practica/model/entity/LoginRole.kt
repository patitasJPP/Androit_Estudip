package com.Jair.practica.model.entity

enum class LoginRole(val code: String) {
    PERSONAL_REGISTRADOR("personal_registrador"),
    ADMINISTRADOR_SAE("administrador_sae");

    companion object {
        fun fromCode(code: String?): LoginRole =
            entries.firstOrNull { it.code == code } ?: PERSONAL_REGISTRADOR
    }
}
