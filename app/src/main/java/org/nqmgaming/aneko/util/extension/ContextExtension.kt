package org.nqmgaming.aneko.util.extension

import android.Manifest
import android.app.LocaleManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.LocaleList
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat

fun Context.checkNotificationPermission(
    requestPermissionLauncher: ActivityResultLauncher<String>,
    onGranted: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
    }
    onGranted()
}

fun Context.changeLanguage(languageCode: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags(languageCode)
    } else {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCode))
    }
}

fun Context.getLanguageCode(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getSystemService(LocaleManager::class.java).applicationLocales[0]?.toLanguageTag()
            ?.split("-")?.first() ?: "en"
    } else {
        AppCompatDelegate.getApplicationLocales()[0]?.toLanguageTag()?.split("-")?.first() ?: "en"
    }
}

