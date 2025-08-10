package com.youfeng.sfsmod.di

import com.youfeng.sfsmod.data.repository.InstallPermissionRepository
import com.youfeng.sfsmod.data.repository.InstallPermissionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 模块，用于提供仓库层的依赖关系
 */
@Module
@InstallIn(SingletonComponent::class) // 安装在 Application 级别的组件中
abstract class RepositoryModule {

    /**
     * 将 InstallPermissionRepository 接口与其实现类 InstallPermissionRepositoryImpl 绑定
     * 当有地方需要 @Inject InstallPermissionRepository 时，Hilt 会自动提供一个 InstallPermissionRepositoryImpl 的实例
     */
    @Binds
    @Singleton
    abstract fun bindInstallPermissionRepository(
        installPermissionRepositoryImpl: InstallPermissionRepositoryImpl
    ): InstallPermissionRepository

}