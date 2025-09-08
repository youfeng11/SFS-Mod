package com.youfeng.sfsmod.data.repository

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface InstallPermissionRepository {
    fun hasInstallPermission(): Boolean
}

@Singleton
class InstallPermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : InstallPermissionRepository {

    override fun hasInstallPermission(): Boolean =
        // API 26 (Android O) 以下的版本，不需要此权限，默认拥有
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else true

}