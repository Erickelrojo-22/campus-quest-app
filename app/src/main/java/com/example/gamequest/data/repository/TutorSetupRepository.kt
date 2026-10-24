package com.example.gamequest.data.repository

interface TutorSetupRepository {
    suspend fun isSetupRequired(): Boolean
    suspend fun configureTutor(nombres: String, correo: String, carrera: String, contrasena: String): AuthResult
}
