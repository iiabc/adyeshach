package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import ink.ptms.adyeshach.core.MinecraftMeta

/**
 * PacketEvents 实现的 MinecraftMeta，包装 EntityData
 */
class PEMinecraftMeta(private val entityData: EntityData<*>) : MinecraftMeta {

    override fun source(): Any = entityData
}
