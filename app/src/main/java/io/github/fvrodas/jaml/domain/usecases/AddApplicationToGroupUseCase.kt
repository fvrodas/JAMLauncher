package io.github.fvrodas.jaml.domain.usecases

import android.content.SharedPreferences
import io.github.fvrodas.jaml.core.common.usecases.UseCase
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import kotlinx.serialization.json.Json

class AddApplicationToGroupUseCase(
    private val sharedPreferences: SharedPreferences
) : UseCase<List<PackageInfo>, PackageInfo>() {
    override suspend fun invoke(params: PackageInfo): List<PackageInfo> {
        val groupedAppList = sharedPreferences.getString(LauncherPreferences.APP_FOLDERS, null)?.let { apps ->
            Json.decodeFromString(apps) as MutableList<PackageInfo>
        } ?: mutableListOf()

        groupedAppList.add(params)

        sharedPreferences.edit().apply {
            putString(LauncherPreferences.APP_FOLDERS, Json.encodeToString(groupedAppList))
            apply()
        }

        return groupedAppList.toList()
    }
}
