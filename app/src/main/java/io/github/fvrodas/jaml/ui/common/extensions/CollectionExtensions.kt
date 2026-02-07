package io.github.fvrodas.jaml.ui.common.extensions

import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry

fun Set<LauncherEntry>.updateAppEntry(
    packageName: String,
    notificationTitle: String?
): Set<LauncherEntry> = this.map {
    if (packageName == it.packageInfo.packageName) {
        it.copy(notificationTitle = notificationTitle)
    } else {
        it
    }
}.toSet()

fun Set<PackageInfo>.exclude(collection: Collection<PackageInfo>): Set<PackageInfo> =
    this.filter { c -> collection.none { it.packageName == c.packageName } }.toSet()
