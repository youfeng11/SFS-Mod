package com.youfeng.sfsmod.domain.state

// 对比签名结果
sealed class VerifySignatureState {
    /** 签名一致，正常 */
    data object SignatureValid : VerifySignatureState()

    /** 签名不匹配 */
    data object SignatureMismatch : VerifySignatureState()

    /** 无法获取本应用的签名 */
    data object SignatureUnavailableThis : VerifySignatureState()

    /** 无法获取目标APK的签名 */
    data object SignatureUnavailableApk : VerifySignatureState()
}