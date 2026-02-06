package ink.ptms.adyeshach.impl

import ink.ptms.adyeshach.core.Adyeshach
import taboolib.common.util.unsafeLazy

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.DefaultAdyeshachBooster
 *
 * @author 坏黑
 * @since 2022/6/19 17:12
 */
object DefaultAdyeshachBooster {

    val api by unsafeLazy { DefaultAdyeshachAPI() }

    /**
     * 启动 Adyeshach 服务
     */
    fun startup() {
        warmupPathfinding()
        Adyeshach.register(api)
    }

    /**
     * 避免首次寻路时的延迟
     */
    private fun warmupPathfinding() {
        try {
            Class.forName("taboolib.module.navigation.PathTypeFactory")
            
            val classesToWarmup = listOf(
                "taboolib.module.navigation.NodeReader",
                "taboolib.module.navigation.PathFinder",
                "taboolib.module.navigation.Node",
                "taboolib.module.navigation.Path",
                "taboolib.module.navigation.NodeEntity",
                "taboolib.module.navigation.RandomPositionGenerator"
            )
            
            classesToWarmup.forEach { className ->
                try {
                    Class.forName(className)
                } catch (e: ClassNotFoundException) {
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}