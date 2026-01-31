package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.protocol.particle.type.ParticleType
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes
import com.github.retrooper.packetevents.protocol.world.Direction
import com.github.retrooper.packetevents.protocol.world.PaintingType
import com.github.retrooper.packetevents.util.Vector3d
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.github.retrooper.packetevents.util.SpigotReflectionUtil
import ink.ptms.adyeshach.core.MinecraftHelper
import ink.ptms.adyeshach.core.bukkit.BukkitPaintings
import ink.ptms.adyeshach.core.bukkit.BukkitParticles
import ink.ptms.adyeshach.core.bukkit.BukkitDirection
import ink.ptms.adyeshach.core.entity.EntityTypes
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.TropicalFish
import org.bukkit.material.MaterialData
import org.bukkit.util.Vector

/**
 * PacketEvents 实现的 MinecraftHelper
 */
class PEHelper : MinecraftHelper {

    override fun adapt(type: EntityTypes): Any {
        return EntityTypeMapping.toPacketEvents(type)
    }

    override fun adapt(location: Location): Any {
        return BlockPos(location.blockX, location.blockY, location.blockZ)
    }

    override fun adapt(paintings: BukkitPaintings): Any {
        return PaintingType.getByTitle(paintings.legacy ?: paintings.name) ?: PaintingType.KEBAB
    }

    override fun adapt(particles: BukkitParticles): Any {
        return try {
            SpigotConversionUtil.fromBukkitParticle(Particle.valueOf(particles.name)) ?: ParticleTypes.FLAME
        } catch (e: Exception) {
            ParticleTypes.FLAME
        }
    }

    override fun adaptTropicalFishPattern(data: Int): TropicalFish.Pattern {
        return TropicalFish.Pattern.values().getOrElse(data and 0xFFFF) { TropicalFish.Pattern.KOB }
    }

    override fun adaptTropicalFishPattern(pattern: TropicalFish.Pattern): Int {
        return pattern.ordinal
    }

    override fun getEntity(world: World, id: Int): Entity? {
        return SpigotConversionUtil.getEntityById(world, id)
    }

    override fun getEntityDataWatcher(entity: Entity): Any {
        return SpigotConversionUtil.getEntityMetadata(entity)
    }

    override fun getBlockId(materialData: MaterialData): Int {
        val wrapped = SpigotConversionUtil.fromBukkitMaterialData(materialData)
        return wrapped.globalId
    }

    override fun vec3dToVector(vec3d: Any): Vector {
        return when (vec3d) {
            is Vector3d -> Vector(vec3d.x, vec3d.y, vec3d.z)
            else -> Vector(0, 0, 0)
        }
    }

    override fun craftChatSerializerToJson(compound: Any): String {
        return compound.toString()
    }

    override fun literalChatBaseComponent(message: String): Any {
        return "{\"text\":\"${message.replace("\"", "\\\"")}\"}"
    }

    override fun isChunkVisible(player: Player, chunkX: Int, chunkZ: Int): Boolean {
        val loc = player.location
        val playerChunkX = loc.blockX shr 4
        val playerChunkZ = loc.blockZ shr 4
        val viewDistance = 10
        return kotlin.math.abs(chunkX - playerChunkX) <= viewDistance && kotlin.math.abs(chunkZ - playerChunkZ) <= viewDistance
    }

    override fun toMinecraft(entity: Entity): Any {
        return SpigotReflectionUtil.getNMSEntity(entity) ?: entity
    }

    fun directionFromBukkit(direction: BukkitDirection): Direction {
        return when (direction.name) {
            "SOUTH" -> Direction.SOUTH
            "WEST" -> Direction.WEST
            "NORTH" -> Direction.NORTH
            "EAST" -> Direction.EAST
            else -> Direction.SOUTH
        }
    }

    private data class BlockPos(val x: Int, val y: Int, val z: Int)
}
