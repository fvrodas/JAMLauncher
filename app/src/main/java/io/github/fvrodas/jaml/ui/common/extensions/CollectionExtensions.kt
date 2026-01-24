package io.github.fvrodas.jaml.ui.common.extensions

import io.github.fvrodas.jaml.core.domain.entities.PackageInfo

fun Set<PackageInfo>.updateAppEntry(
    packageName: String,
    hasNotification: Boolean,
    notificationTitle: String?
): Set<PackageInfo> = this.map {
    if (packageName == it.packageName) {
        it.copy(hasNotification = hasNotification, notificationTitle = notificationTitle)
    } else {
        it
    }
}.toSet()

fun Set<PackageInfo>.exclude(collection: Collection<PackageInfo>): Set<PackageInfo> =
    this.filter { c -> collection.none { it.packageName == c.packageName } }.toSet()
