package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.protocol.entity.data.EntityData
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes
import com.github.retrooper.packetevents.protocol.entity.pose.EntityPose
import com.github.retrooper.packetevents.protocol.entity.sniffer.SnifferState
import com.github.retrooper.packetevents.protocol.entity.villager.VillagerData as PEVillagerData
import com.github.retrooper.packetevents.protocol.particle.Particle
import com.github.retrooper.packetevents.protocol.particle.type.ParticleType
import com.github.retrooper.packetevents.protocol.world.painting.PaintingVariant
import com.github.retrooper.packetevents.util.Quaternion4f
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.MinecraftEntityMetadataHandler
import ink.ptms.adyeshach.core.MinecraftMetadataParser
import ink.ptms.adyeshach.core.bukkit.BukkitParticles
import ink.ptms.adyeshach.core.bukkit.BukkitPose
import ink.ptms.adyeshach.core.bukkit.data.VillagerData
import ink.ptms.adyeshach.core.MinecraftMeta
import ink.ptms.adyeshach.core.entity.type.AdyEntity
import ink.ptms.adyeshach.core.entity.type.AdySniffer
import net.kyori.adventure.text.Component
import org.bukkit.Art
import org.bukkit.inventory.ItemStack
import org.bukkit.material.MaterialData
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import taboolib.common5.Quat
import java.util.*
import java.util.function.Consumer

/**
 * PacketEvents 实现的 MinecraftEntityMetadataHandler
 */
class PEEntityMetadataHandler : MinecraftEntityMetadataHandler {

    private val registeredParser = linkedMapOf<String, MinecraftMetadataParser<*>>()

    init {
        addParser("Int", PEParsers.IntParser())
        addParser("Byte", PEParsers.ByteParser())
        addParser("Float", PEParsers.FloatParser())
        addParser("Boolean", PEParsers.BooleanParser())
        addParser("UUID", PEParsers.UUIDParser())
        addParser("String", PEParsers.StringParser())
        addParser("Chat", PEParsers.ChatParser())
        addParser("OptChat", PEParsers.OptChatParser())
        addParser("ItemStack", PEParsers.ItemStackParser())
        addParser("EulerAngle", PEParsers.EulerAngleParser())
        addParser("VillagerData", PEParsers.VillagerDataParser())
        addParser("Particle", PEParsers.BukkitParticleParser())
        addParser("BukkitPose", PEParsers.BukkitPoseParser())
        addParser("Cat.Type", PEParsers.CatVariantParser())
        addParser("Frog.Variant", PEParsers.FrogVariantParser())
        addParser("Art", PEParsers.PaintingVariantParser())
        addParser("Vector3", PEParsers.Vector3Parser())
        addParser("Quaternion", PEParsers.QuaternionParser())
        addParser("SnifferState", PEParsers.SnifferStateParser())
        addParser("BlockID", PEParsers.BlockStateParser())
        addParser("OptBlockID", PEParsers.OptBlockStateParser())
        addParser("OptBlockPos", PEParsers.OptBlockPosParser())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : AdyEntity> buildMetadata(type: Class<T>, process: Consumer<T>): List<MinecraftMeta> {
        val entityType = Adyeshach.api().getEntityTypeRegistry().getEntityTypeFromAdyClass(type)
            ?: error("Unsupported entity type: $type")
        val entityInstance = Adyeshach.api().getEntityTypeRegistry().getEntityInstance(entityType)
        val record = arrayListOf<String>()
        entityInstance.setTag("META_GENERATOR", 1)
        entityInstance.setTag("META_GENERATOR_RECORD", record)
        process.accept(entityInstance as T)
        val generated = arrayListOf<MinecraftMeta>()
        entityInstance.getAvailableEntityMeta().forEach { meta ->
            if (meta.key in record) {
                generated += meta.generateMetadata(entityInstance)
            }
        }
        return generated
    }

    override fun createMetadataPacket(entityId: Int, metaList: List<MinecraftMeta>): Any {
        val entityDataList = metaList.map { it.source() as EntityData<*> }
        return WrapperPlayServerEntityMetadata(entityId, entityDataList)
    }

    override fun addParser(id: String, metadataParser: MinecraftMetadataParser<*>) {
        registeredParser[id] = metadataParser
    }

    override fun getParser(id: String): MinecraftMetadataParser<*>? = registeredParser[id]

    override fun getParsers(): List<MinecraftMetadataParser<*>> = registeredParser.values.toList()

    override fun createByteMeta(index: Int, value: Byte) = PEMinecraftMeta(EntityData(index, EntityDataTypes.BYTE, value))
    override fun createIntMeta(index: Int, value: Int) = PEMinecraftMeta(EntityData(index, EntityDataTypes.INT, value))
    override fun createFloatMeta(index: Int, value: Float) = PEMinecraftMeta(EntityData(index, EntityDataTypes.FLOAT, value))
    override fun createStringMeta(index: Int, value: String) = PEMinecraftMeta(EntityData(index, EntityDataTypes.STRING, value))
    override fun createBooleanMeta(index: Int, value: Boolean) = PEMinecraftMeta(EntityData(index, EntityDataTypes.BOOLEAN, value))
    override fun createUUIDMeta(index: Int, value: UUID) = PEMinecraftMeta(EntityData(index, EntityDataTypes.OPTIONAL_UUID, Optional.of(value)))

    override fun createChatMeta(index: Int, rawMessage: String) = PEMinecraftMeta(
        EntityData(index, EntityDataTypes.ADV_COMPONENT, Component.text(rawMessage))
    )

    override fun createOptChatMeta(index: Int, rawMessage: String?) = PEMinecraftMeta(
        EntityData(index, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.ofNullable(rawMessage?.let { Component.text(it) }))
    )

    override fun createItemStackMeta(index: Int, itemStack: ItemStack) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.ITEMSTACK, SpigotConversionUtil.fromBukkitItemStack(itemStack)))

    override fun createBlockStateMeta(index: Int, blockData: MaterialData) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.BLOCK_STATE, SpigotConversionUtil.fromBukkitMaterialData(blockData).globalId.toInt()))

    override fun createOptBlockStateMeta(index: Int, blockData: MaterialData?) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.BLOCK_STATE, blockData?.let { SpigotConversionUtil.fromBukkitMaterialData(it).globalId.toInt() } ?: 0))

    override fun createParticleMeta(index: Int, particle: BukkitParticles) = run {
        val helper = Adyeshach.api().getMinecraftAPI().getHelper() as PEHelper
        val peParticle = helper.adapt(particle) as ParticleType<*>
        PEMinecraftMeta(EntityData(index, EntityDataTypes.PARTICLE, Particle(peParticle)))
    }

    override fun createOptBlockPosMeta(index: Int, vector: Vector?) = PEMinecraftMeta(
        EntityData(index, EntityDataTypes.OPTIONAL_BLOCK_POSITION, Optional.ofNullable(vector?.let { Vector3i(it.x.toInt(), it.y.toInt(), it.z.toInt()) }))
    )

    override fun createEulerAngleMeta(index: Int, value: EulerAngle) = PEMinecraftMeta(
        EntityData(index, EntityDataTypes.ROTATION, Vector3f(value.x.toFloat(), value.y.toFloat(), value.z.toFloat()))
    )

    override fun createVillagerDataMeta(index: Int, villagerData: VillagerData) = PEMinecraftMeta(
        EntityData(index, EntityDataTypes.VILLAGER_DATA, PEVillagerData(
            villagerData.type.ordinal, villagerData.profession.ordinal, 1))
    )

    override fun createPoseMeta(index: Int, pose: BukkitPose) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.ENTITY_POSE, EntityPose.values()[pose.ordinal]))

    override fun createCatVariantMeta(index: Int, type: Any) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.CAT_VARIANT, 0))

    override fun createFrogVariantMeta(index: Int, type: Any) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.FROG_VARIANT, 0))

    override fun createPaintingVariantMeta(index: Int, type: Any) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.PAINTING_VARIANT_TYPE, 0))

    override fun createSnifferStateMeta(index: Int, state: AdySniffer.State) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.SNIFFER_STATE, SnifferState.values()[state.ordinal]))

    override fun createVector3Meta(index: Int, value: Vector) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.VECTOR3F, Vector3f(value.x.toFloat(), value.y.toFloat(), value.z.toFloat())))

    override fun createQuaternionMeta(index: Int, value: Quat) =
        PEMinecraftMeta(EntityData(index, EntityDataTypes.QUATERNION, Quaternion4f(value.x().toFloat(), value.y().toFloat(), value.z().toFloat(), value.w().toFloat())))
}
