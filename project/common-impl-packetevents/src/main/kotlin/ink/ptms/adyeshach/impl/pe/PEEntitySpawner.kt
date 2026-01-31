package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.entity.type.EntityType
import com.github.retrooper.packetevents.protocol.world.Direction
import com.github.retrooper.packetevents.protocol.world.PaintingType
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnExperienceOrb
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPainting
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnPlayer
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.MinecraftEntitySpawner
import ink.ptms.adyeshach.core.bukkit.BukkitDirection
import ink.ptms.adyeshach.core.bukkit.BukkitPaintings
import ink.ptms.adyeshach.core.entity.EntityTypes
import ink.ptms.adyeshach.core.util.fixYaw
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.material.MaterialData
import java.util.*

/**
 * PacketEvents 实现的 MinecraftEntitySpawner
 */
class PEEntitySpawner : MinecraftEntitySpawner {

    private val packetHandler
        get() = Adyeshach.api().getMinecraftAPI().getPacketHandler()

    private val helper
        get() = Adyeshach.api().getMinecraftAPI().getHelper() as PEHelper

    override fun spawnEntity(player: Player, entityType: EntityTypes, entityId: Int, uuid: UUID, location: Location, data: Int) {
        val peType = EntityTypeMapping.toPacketEvents(entityType)
        val yaw = entityType.fixYaw(location.yaw)
        val packet = WrapperPlayServerSpawnEntity(
            entityId,
            Optional.of(uuid),
            peType,
            Vector3d(location.x, location.y, location.z),
            location.pitch,
            yaw,
            yaw,
            data,
            Optional.of(Vector3d(0.0, 0.0, 0.0))
        )
        packetHandler.sendPacket(listOf(player), packet)
    }

    override fun spawnEntityLiving(player: Player, entityType: EntityTypes, entityId: Int, uuid: UUID, location: Location) {
        // 1.19+ 生物实体改用 SPAWN_ENTITY 数据包
        if (PacketEvents.getAPI().serverManager.version.isNewerThanOrEquals(ServerVersion.V_1_19)) {
            return spawnEntity(player, entityType, entityId, uuid, location, 0)
        }
        val peType = EntityTypeMapping.toPacketEvents(entityType)
        val yaw = entityType.fixYaw(location.yaw)
        val packet = WrapperPlayServerSpawnLivingEntity(
            entityId,
            uuid,
            peType,
            Vector3d(location.x, location.y, location.z),
            location.yaw,
            location.pitch,
            yaw,
            Vector3d(0.0, 0.0, 0.0),
            emptyList()
        )
        packetHandler.sendPacket(listOf(player), packet)
    }

    override fun spawnNamedEntity(player: Player, entityId: Int, uuid: UUID, location: Location) {
        // 1.20.2+ 玩家实体改用 SPAWN_ENTITY 数据包
        if (PacketEvents.getAPI().serverManager.version.isNewerThanOrEquals(ServerVersion.V_1_20_2)) {
            return spawnEntity(player, EntityTypes.PLAYER, entityId, uuid, location, 0)
        }
        val packet = WrapperPlayServerSpawnPlayer(
            entityId,
            uuid,
            Vector3d(location.x, location.y, location.z),
            location.yaw,
            location.pitch,
            emptyList()
        )
        packetHandler.sendPacket(listOf(player), packet)
    }

    override fun spawnEntityFallingBlock(player: Player, entityId: Int, uuid: UUID, location: Location, material: Material, data: Byte) {
        val materialData = MaterialData(material, data)
        val blockId = helper.getBlockId(materialData)
        spawnEntity(player, EntityTypes.FALLING_BLOCK, entityId, uuid, location, blockId)
    }

    override fun spawnEntityExperienceOrb(player: Player, entityId: Int, location: Location, amount: Int) {
        val packet = WrapperPlayServerSpawnExperienceOrb(
            entityId,
            location.x, location.y, location.z,
            amount.toShort()
        )
        packetHandler.sendPacket(listOf(player), packet)
    }

    override fun spawnEntityPainting(player: Player, entityId: Int, uuid: UUID, location: Location, direction: BukkitDirection, painting: BukkitPaintings) {
        val paintingType = helper.adapt(painting) as? PaintingType ?: PaintingType.KEBAB
        val pos = Vector3i(location.blockX, location.blockY, location.blockZ)
        val dir = helper.directionFromBukkit(direction)
        val packet = WrapperPlayServerSpawnPainting(entityId, uuid, paintingType, pos, dir)
        packetHandler.sendPacket(listOf(player), packet)
    }
}
