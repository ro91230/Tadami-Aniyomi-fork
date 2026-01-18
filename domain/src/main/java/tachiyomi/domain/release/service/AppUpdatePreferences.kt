package tachiyomi.domain.release.service

import tachiyomi.core.common.preference.Preference
import tachiyomi.core.common.preference.PreferenceStore

class AppUpdatePreferences(
    private val preferenceStore: PreferenceStore,
) {
    /**
     * App update check interval in hours.
     * -1 = on app start only
     * 0 = never (disabled)
     * 6, 12, 24, 168 = interval in hours
     */
    fun appUpdateInterval() = preferenceStore.getInt("pref_app_update_interval", -1)

    fun lastAppUpdateCheck() = preferenceStore.getLong(
        Preference.appStateKey("last_app_update_check"),
        0L,
    )
}
