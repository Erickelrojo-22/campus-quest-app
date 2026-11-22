package com.example.gamequest.data.repository

/**
 * Contrato para crear el tutor de una instalación sin backend. El registro público
 * solo crea estudiantes; esta es la única vía para obtener el rol de tutor en modo local.
 */
interface TutorSetupRepository {
    /** `true` mientras no exista ningún usuario con rol de tutor. */
    suspend fun isSetupRequired(): Boolean

    /**
     * Valida los datos, crea el tutor en una transacción e inicia su sesión.
     * Devuelve [AuthResult.Error] si ya existe un tutor, el correo está en uso o algún
     * campo no es válido.
     */
    suspend fun configureTutor(nombres: String, correo: String, carrera: String, contrasena: String): AuthResult
}
