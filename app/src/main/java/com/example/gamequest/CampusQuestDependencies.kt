package com.example.gamequest
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.util.SoundEffectManager
import com.example.gamequest.data.repository.TutorSetupRepository
interface CampusQuestDependencies {
    val sessionCoordinator: com.example.gamequest.data.session.SessionCoordinator
    val authRepository: AuthRepository
    val campusRepository: CampusRepository
    val preferencesRepository: UserPreferencesRepository
    val soundEffectManager: SoundEffectManager
    val tutorSetupRepository: TutorSetupRepository? get() = null
}
