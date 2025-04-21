package com.youfeng.sfsmod.data.model

// 对比签名结果
sealed class VerifySignatureStates {
    /** 签名一致，正常 */
    data object SignatureValid : VerifySignatureStates()

    /** 签名不匹配 */
    data object SignatureMismatch : VerifySignatureStates()

    /** 获取目标APK签名的路径为空 */
    data object SignatureUnavailablePath : VerifySignatureStates()

    /** 无法获取本应用的签名 */
    data object SignatureUnavailableThis : VerifySignatureStates()

    /** 无法获取目标APK的签名 */
    data object SignatureUnavailableApk : VerifySignatureStates()
}