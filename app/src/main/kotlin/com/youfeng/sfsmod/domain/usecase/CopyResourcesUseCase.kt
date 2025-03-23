package com.youfeng.sfsmod.domain.usecase

import com.youfeng.sfsmod.data.MainRepository
import com.youfeng.sfsmod.data.VerifySignatureStates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 复制资源文件的 Use Case
 */
class CopyResourcesUseCase @Inject constructor(
    private val repository: MainRepository,
    private val verifySignatureUseCase: VerifySignatureUseCase
) {
    suspend operator fun invoke(): VerifySignatureStates = withContext(Dispatchers.IO) {
        // 复制资源
        val externalCachePath = repository.copyResources()
        // 验证签名
        return@withContext verifySignatureUseCase(externalCachePath)
    }
}
