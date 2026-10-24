package com.Jair.practica.model.entity

enum class LoginRole(val code: String) {
    HALLADOR("hallador"),
    SERVICIO("servicio");

    companion object {
        fun fromCode(code: String?): LoginRole =
            entries.firstOrNull { it.code == code } ?: HALLADOR
    }
}
