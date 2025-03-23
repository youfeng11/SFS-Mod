package com.youfeng.sfsmod.domain.usecase

import com.youfeng.sfsmod.data.VerifySignatureStates
import com.youfeng.sfsmod.utils.SignUtil
import okio.Path
import javax.inject.Inject

/**
 * 负责 APK 签名验证的 Use Case
 */
class VerifySignatureUseCase @Inject constructor(
    private val signUtil: SignUtil
) {
    operator fun invoke(externalCachePath: Path?): VerifySignatureStates {
        externalCachePath ?: return VerifySignatureStates.SignatureUnavailable
        val thisMD5 = signUtil.getCurrentAppSignatureMD5()
        val apkMD5 = signUtil.getApkSignatureMD5(externalCachePath.resolve("temp.apk").toString())
        return when {
            thisMD5.isNullOrEmpty() || apkMD5.isNullOrEmpty() -> VerifySignatureStates.SignatureUnavailable
            thisMD5 == apkMD5 -> VerifySignatureStates.SignatureValid
            else -> VerifySignatureStates.SignatureMismatch
        }
    }
}
