package com.example.gamequest.ui.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"


    // Contenedor único de las 5 pestañas (carrusel deslizable). Las constantes
    // HOME/MISSIONS/SCANNER/BADGES/PROFILE identifican cada pestaña dentro de él.
    const val MAIN = "main"
    const val KEY_TAB_SOLICITADA = "tab_solicitada"
    const val KEY_PUNTO_DESTINO = "punto_destino_id"

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
