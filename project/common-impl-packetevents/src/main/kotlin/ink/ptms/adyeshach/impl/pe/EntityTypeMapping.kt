package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.protocol.player.ClientVersion
import ink.ptms.adyeshach.core.entity.EntityTypes as AdyEntityTypes

/**
 * Adyeshach EntityTypes 到 PacketEvents EntityType 的映射
 */
object EntityTypeMapping {

    private val cache = mutableMapOf<AdyEntityTypes, EntityType>()

    fun toPacketEvents(type: AdyEntityTypes): EntityType {
        return cache.getOrPut(type) {
            val name = when (type) {
                AdyEntityTypes.ZOMBIE_PIGMAN -> "zombified_piglin"
                AdyEntityTypes.MINECART_CHEST -> "chest_minecart"
                AdyEntityTypes.MINECART_COMMAND -> "command_block_minecart"
                AdyEntityTypes.MINECART_FURNACE -> "furnace_minecart"
                AdyEntityTypes.MINECART_HOPPER -> "hopper_minecart"
                AdyEntityTypes.MINECART_MOB_SPAWNER -> "spawner_minecart"
                AdyEntityTypes.MINECART_TNT -> "tnt_minecart"
                AdyEntityTypes.THROWN_EGG -> "egg"
                AdyEntityTypes.THROWN_ENDER_PEARL -> "ender_pearl"
                AdyEntityTypes.THROWN_EXPERIENCE_BOTTLE -> "experience_bottle"
                AdyEntityTypes.THROWN_POTION -> "potion"
                AdyEntityTypes.THROWN_TRIDENT -> "trident"
                AdyEntityTypes.MUSHROOM -> "mooshroom"
                AdyEntityTypes.FISHING_HOOK -> "fishing_bobber"
                AdyEntityTypes.PRIMED_TNT -> "tnt"
                AdyEntityTypes.FIREWORK_ROCKET -> "firework_rocket"
                else -> type.name.lowercase()
            }
            EntityTypes.getByName("minecraft:$name") ?: EntityTypes.ZOMBIE
        }
    }

    fun getClientVersion(): ClientVersion {
        return PacketEvents.getAPI().serverManager.version.toClientVersion()
    }
}
