package com.example.gamequest.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"


    const val HOME = "home"
    const val MISSIONS = "missions"
    const val SCANNER = "scanner"
    const val BADGES = "badges"
    const val PROFILE = "profile"

    const val MISSION_DETAIL = "mission_detail/{misionId}"
    fun missionDetail(misionId: Int) = "mission_detail/$misionId"

    const val BADGE_EARNED = "badge_earned/{misionId}"
    fun badgeEarned(misionId: Int) = "badge_earned/$misionId"

    const val SETTINGS = "settings"
    const val MISSION_MANAGEMENT = "mission_management"

    const val MISSION_FORM = "mission_form?puntoId={puntoId}"
    fun missionFormNuevo() = "mission_form"
    fun missionFormEditar(puntoId: Int) = "mission_form?puntoId=$puntoId"
}
