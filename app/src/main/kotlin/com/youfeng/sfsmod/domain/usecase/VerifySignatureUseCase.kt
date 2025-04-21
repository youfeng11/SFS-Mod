package com.youfeng.sfsmod.domain.usecase

import com.youfeng.sfsmod.data.model.VerifySignatureStates
import com.youfeng.sfsmod.utils.SignatureUtil
import com.youfeng.sfsmod.utils.sha256
import okio.Path
import javax.inject.Inject

/**
 * APK 签名验证用例
 *
 * 负责验证指定APK文件签名与当前应用签名的一致性
 * @property signatureUtil 签名工具类，用于获取签名信息
 */
class VerifySignatureUseCase @Inject constructor(
    private val signatureUtil: SignatureUtil
) {
    /**
     * 执行签名验证操作
     * @param externalCachePath 待验证APK的文件路径（可为空）
     * @return 验证结果状态，包含以下可能情况：
     *         - SignatureValid: 签名一致
     *         - SignatureMismatch: 签名不一致
     *         - SignatureUnavailable: 签名获取失败（附带具体错误类型）
     */
    operator fun invoke(externalCachePath: Path?): VerifySignatureStates {
        // 参数有效性检查
        externalCachePath
            ?: return VerifySignatureStates.SignatureUnavailablePath

        // 获取当前应用签名
        val currentSignature = signatureUtil.getCurrentAppSignature()
            ?: return VerifySignatureStates.SignatureUnavailableThis

        // 获取目标APK签名
        val targetSignature = signatureUtil.getApkSignature(externalCachePath)
            ?: return VerifySignatureStates.SignatureUnavailableApk

        // 计算签名SHA-256值
        val currentSha256 = currentSignature.toByteArray().sha256()
        val targetSha256 = targetSignature.toByteArray().sha256()

        return when {
            currentSha256.contentEquals(targetSha256) -> VerifySignatureStates.SignatureValid
            else -> VerifySignatureStates.SignatureMismatch
        }
    }
}