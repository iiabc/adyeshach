package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.manager.server.ServerVersion
import com.github.retrooper.packetevents.protocol.player.Equipment
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot
import com.github.retrooper.packetevents.protocol.world.Location
import com.github.retrooper.packetevents.util.Vector3d
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAttachEntity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation.EntityAnimationType
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityEquipment
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityHeadLook
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerHurtAnimation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMove
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRelativeMoveAndRotation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetPassengers
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUseBed
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.MinecraftEntityOperator
import ink.ptms.adyeshach.core.bukkit.BukkitAnimation
import ink.ptms.adyeshach.core.MinecraftMeta
import org.bukkit.Location as BukkitLocation
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot as BukkitEquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

/**
 * PacketEvents 实现的 MinecraftEntityOperator
 */
class PEEntityOperator : MinecraftEntityOperator {

    private val packetHandler
        get() = Adyeshach.api().getMinecraftAPI().getPacketHandler()

    private val metaHandler
        get() = Adyeshach.api().getMinecraftAPI().getEntityMetadataHandler()

    override fun destroyEntity(player: List<Player>, entityId: Int) {
        packetHandler.sendPacket(player, WrapperPlayServerDestroyEntities(entityId))
    }

    override fun teleportEntity(player: List<Player>, entityId: Int, location: BukkitLocation, onGround: Boolean) {
        val loc = Location(location.x, location.y, location.z, location.yaw, location.pitch)
        val packet = WrapperPlayServerEntityTeleport(entityId, loc, onGround)
        packetHandler.sendPacket(player, packet)
        updateHeadRotation(player, entityId, location.yaw)
    }

    override fun updateEntityLook(player: List<Player>, entityId: Int, yaw: Float, pitch: Float, onGround: Boolean) {
        val packet = WrapperPlayServerEntityRotation(entityId, yaw, pitch, onGround)
        packetHandler.sendPacket(player, packet)
        updateHeadRotation(player, entityId, yaw)
    }

    override fun updateRelEntityMove(player: List<Player>, entityId: Int, x: Short, y: Short, z: Short, onGround: Boolean) {
        val packet = WrapperPlayServerEntityRelativeMove(entityId, x / 4096.0, y / 4096.0, z / 4096.0, onGround)
        packetHandler.sendPacket(player, packet)
    }

    override fun updateRelEntityMoveLook(player: List<Player>, entityId: Int, x: Short, y: Short, z: Short, yaw: Float, pitch: Float, onGround: Boolean) {
        val packet = WrapperPlayServerEntityRelativeMoveAndRotation(entityId, x / 4096.0, y / 4096.0, z / 4096.0, yaw, pitch, onGround)
        packetHandler.sendPacket(player, packet)
        updateHeadRotation(player, entityId, yaw)
    }

    override fun updateEntityVelocity(player: List<Player>, entityId: Int, vector: Vector) {
        val packet = WrapperPlayServerEntityVelocity(entityId, Vector3d(vector.x, vector.y, vector.z))
        packetHandler.sendPacket(player, packet)
    }

    override fun updateHeadRotation(player: List<Player>, entityId: Int, yaw: Float) {
        val packet = WrapperPlayServerEntityHeadLook(entityId, yaw)
        packetHandler.sendPacket(player, packet)
    }

    override fun updateHurtAnimation(player: List<Player>, entityId: Int, yaw: Float) {
        val packet = WrapperPlayServerHurtAnimation(entityId, yaw)
        packetHandler.sendPacket(player, packet)
    }

    override fun updateEquipment(player: List<Player>, entityId: Int, slot: BukkitEquipmentSlot, itemStack: ItemStack) {
        updateEquipment(player, entityId, mapOf(slot to itemStack))
    }

    override fun updateEquipment(player: List<Player>, entityId: Int, equipment: Map<BukkitEquipmentSlot, ItemStack>) {
        val peEquipment = equipment.map { (slot, item) ->
            Equipment(slotFromBukkit(slot), SpigotConversionUtil.fromBukkitItemStack(item))
        }.toMutableList()
        // 1.21.4+ 新增了 BODY 装备槽位，Set Equipment 包中必须包含该槽位，
        // 否则客户端不会渲染主手物品
        if (PacketEvents.getAPI().serverManager.version.isNewerThanOrEquals(ServerVersion.V_1_21_4)) {
            val hasBody = peEquipment.any { it.slot == EquipmentSlot.BODY }
            if (!hasBody) {
                peEquipment.add(Equipment(EquipmentSlot.BODY, SpigotConversionUtil.fromBukkitItemStack(ItemStack(Material.AIR))))
            }
        }
        val packet = WrapperPlayServerEntityEquipment(entityId, peEquipment)
        packetHandler.sendPacket(player, packet)
    }

    override fun updatePassengers(player: List<Player>, entityId: Int, vararg passengers: Int) {
        val packet = WrapperPlayServerSetPassengers(entityId, passengers.map { it }.toIntArray())
        packetHandler.sendPacket(player, packet)
    }

    override fun updateEntityMetadata(player: List<Player>, entityId: Int, metadata: List<MinecraftMeta>) {
        val packet = metaHandler.createMetadataPacket(entityId, metadata)
        packetHandler.sendPacket(player, packet)
    }

    override fun updateEntityAnimation(player: List<Player>, entityId: Int, animation: BukkitAnimation) {
        val animType = EntityAnimationType.values().getOrElse(animation.ordinal) { EntityAnimationType.SWING_MAIN_ARM }
        val packet = WrapperPlayServerEntityAnimation(entityId, animType)
        packetHandler.sendPacket(player, packet)
    }

    override fun updateEntityAttach(player: List<Player>, attached: Int, holding: Int) {
        val packet = WrapperPlayServerAttachEntity(attached, holding, false)
        packetHandler.sendPacket(player, packet)
    }

    override fun updatePlayerSleeping(player: List<Player>, entityId: Int, location: BukkitLocation) {
        val pos = Vector3i(location.blockX, location.blockY, location.blockZ)
        val packet = WrapperPlayServerUseBed(entityId, pos)
        packetHandler.sendPacket(player, packet)
    }

    private fun slotFromBukkit(slot: BukkitEquipmentSlot): EquipmentSlot {
        return when (slot) {
            BukkitEquipmentSlot.HAND -> EquipmentSlot.MAIN_HAND
            BukkitEquipmentSlot.OFF_HAND -> EquipmentSlot.OFF_HAND
            BukkitEquipmentSlot.FEET -> EquipmentSlot.BOOTS
            BukkitEquipmentSlot.LEGS -> EquipmentSlot.LEGGINGS
            BukkitEquipmentSlot.CHEST -> EquipmentSlot.CHEST_PLATE
            BukkitEquipmentSlot.HEAD -> EquipmentSlot.HELMET
            else -> EquipmentSlot.MAIN_HAND
        }
    }
}
