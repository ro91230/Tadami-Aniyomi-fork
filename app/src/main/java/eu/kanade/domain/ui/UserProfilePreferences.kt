package eu.kanade.domain.ui

import tachiyomi.core.common.preference.PreferenceStore

class UserProfilePreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun name() = preferenceStore.getString("user_profile_name", DEFAULT_NAME)
    fun avatarUrl() = preferenceStore.getString("user_profile_avatar_url", "")
    fun lastOpenedTime() = preferenceStore.getLong("user_profile_last_opened", 0L)
    fun nameEdited() = preferenceStore.getBoolean("user_profile_name_edited", false)

    fun nicknameFont() = preferenceStore.getString("user_profile_name_font", "default")
    fun nicknameColor() = preferenceStore.getString("user_profile_name_color", "theme")
    fun nicknameCustomColorHex() = preferenceStore.getString("user_profile_name_custom_color_hex", "#FFFFFF")
    fun nicknameOutline() = preferenceStore.getBoolean("user_profile_name_outline", false)
    fun nicknameOutlineWidth() = preferenceStore.getInt("user_profile_name_outline_width", 2)
    fun nicknameGlow() = preferenceStore.getBoolean("user_profile_name_glow", false)
    fun nicknameEffect() = preferenceStore.getString("user_profile_name_effect", "none")

    companion object {
        const val DEFAULT_NAME = "\u0417\u0440\u0438\u0442\u0435\u043b\u044c"
    }
}
