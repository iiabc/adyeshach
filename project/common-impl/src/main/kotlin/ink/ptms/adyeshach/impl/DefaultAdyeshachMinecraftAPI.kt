package ink.ptms.adyeshach.impl

import ink.ptms.adyeshach.core.*
import ink.ptms.adyeshach.impl.pe.*
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformFactory

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.DefaultAdyeshachMinecraftAPI
 *
 * 使用 PacketEvents 实现，需单独安装 PacketEvents 插件
 *
 * @author 坏黑
 * @since 2022/6/28 00:06
 */
class DefaultAdyeshachMinecraftAPI : AdyeshachMinecraftAPI {

    private val peHelper = PEHelper()
    private val peEntitySpawner = PEEntitySpawner()
    private val peEntityOperator = PEEntityOperator()
    private val peEntityMetadataHandler = PEEntityMetadataHandler()
    private val peEntityPlayerHandler = PEEntityPlayerHandler()
    private val peScoreboardOperator = PEScoreboardOperator()
    private val pePacketHandler = PEPacketHandler()
    private val peWorldAccess = PEMinecraftWorldAccess()

    override fun getHelper(): MinecraftHelper = peHelper
    override fun getEntitySpawner(): MinecraftEntitySpawner = peEntitySpawner
    override fun getEntityOperator(): MinecraftEntityOperator = peEntityOperator
    override fun getEntityMetadataHandler(): MinecraftEntityMetadataHandler = peEntityMetadataHandler
    override fun getEntityPlayerHandler(): MinecraftEntityPlayerHandler = peEntityPlayerHandler
    override fun getScoreboardOperator(): MinecraftScoreboardOperator = peScoreboardOperator
    override fun getPacketHandler(): MinecraftPacketHandler = pePacketHandler
    override fun getWorldAccess(): MinecraftWorldAccess = peWorldAccess

    companion object {
        @Awake(LifeCycle.CONST)
        fun init() {
            PlatformFactory.registerAPI<AdyeshachMinecraftAPI>(DefaultAdyeshachMinecraftAPI())
        }
    }
}
