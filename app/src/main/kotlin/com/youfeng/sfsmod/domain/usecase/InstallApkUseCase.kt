package com.youfeng.sfsmod.domain.usecase

import com.youfeng.sfsmod.data.MainRepository
import javax.inject.Inject

/**
 * 负责 APK 安装的 Use Case
 */
class InstallApkUseCase @Inject constructor(
    private val repository: MainRepository
) {
    operator fun invoke() {
        repository.installApk()
    }
}
