package com.youfeng.sfsmod.domain.usecase

import kotlinx.coroutines.delay
import javax.inject.Inject

// 新建CountdownUseCase.kt
class CountdownUseCase @Inject constructor() {
    suspend operator fun invoke(
        onTick: (Int) -> Unit,
        onFinish: () -> Unit
    ) {
        for (i in 3 downTo 0) {
            onTick(i)
            delay(1000)
        }
        onFinish()
    }
}
