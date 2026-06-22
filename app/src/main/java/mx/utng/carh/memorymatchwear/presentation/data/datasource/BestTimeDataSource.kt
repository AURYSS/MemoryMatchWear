package mx.utng.carh.memorymatchwear.presentation.data.datasource

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey

class BestTimeDataSource(context: Context) {
    private val dataStore = context.createDataStore("memory_match_prefs")
    private object Keys { val BEST_TIME = longPreferencesKey("best_time_seconds") }

    suspend fun getBestTime(): Long =
        dataStore.data.map { it[Keys.BEST_TIME] ?: Long.MAX_VALUE }.first()

    suspend fun saveBestTime(seconds: Long) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.BEST_TIME] ?: Long.MAX_VALUE
            if (seconds < current) prefs[Keys.BEST_TIME] = seconds
        }
    }
}
