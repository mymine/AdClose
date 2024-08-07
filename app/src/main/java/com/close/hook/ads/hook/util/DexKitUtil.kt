package com.close.hook.ads.hook.util

import java.util.concurrent.TimeUnit
import android.content.Context
import com.google.common.cache.CacheBuilder
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.result.MethodData

object DexKitUtil {
    @Volatile 
    private var bridge: DexKitBridge? = null
    private val methodCache = CacheBuilder.newBuilder()
        .maximumSize(100)
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build<String, List<MethodData>>()

    val context: Context
        get() = ContextUtil.appContext

    init {
        System.loadLibrary("dexkit")
    }

    @Synchronized
    fun initializeDexKitBridge() {
        if (bridge == null) {
            val apkPath = context.applicationInfo.sourceDir
            bridge = DexKitBridge.create(apkPath)
        }
    }

    fun getBridge(): DexKitBridge {
        return bridge ?: throw IllegalStateException("DexKitBridge not initialized")
    }

    @Synchronized
    fun releaseBridge() {
        bridge?.close()
        bridge = null
    }

    fun getCachedOrFindMethods(key: String, findMethodLogic: () -> List<MethodData>?): List<MethodData>? {
        return methodCache.get(key, findMethodLogic)
    }
}
