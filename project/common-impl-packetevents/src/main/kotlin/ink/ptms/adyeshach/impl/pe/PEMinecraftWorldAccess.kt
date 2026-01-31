package ink.ptms.adyeshach.impl.pe

import com.google.gson.JsonParser
import ink.ptms.adyeshach.core.MinecraftWorldAccess
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.type.Slab

/**
 * PacketEvents 实现的 MinecraftWorldAccess，使用 Bukkit API + block_height.json
 */
class PEMinecraftWorldAccess : MinecraftWorldAccess {

    override fun createBlockAccess(world: World, x: Int, z: Int): MinecraftWorldAccess.BlockAccess {
        return PEBlockAccess(world, x, z)
    }

    class PEBlockAccess(val world: World?, override val x: Int, override val z: Int) : MinecraftWorldAccess.BlockAccess {

        constructor() : this(null, 0, 0)

        override fun getBlockType(x: Int, y: Int, z: Int): Material {
            return world?.getBlockAt(x, y, z)?.type ?: Material.AIR
        }

        override fun getBlockHeight(x: Int, y: Int, z: Int): Double {
            return getBlockTypeAndHeight(x, y, z).second
        }

        override fun getHighestBlock(x: Int, y: Int, z: Int): Double {
            var cy = y
            val minY = try { world?.javaClass?.getMethod("getMinHeight")?.invoke(world) as? Int ?: 0 } catch (_: Exception) { 0 }
            while (cy > minY) {
                val typeAndHeight = getBlockTypeAndHeight(x, cy, z)
                if (typeAndHeight.first.isSolid) {
                    return cy + typeAndHeight.second
                }
                cy--
            }
            return cy.toDouble()
        }

        override fun getBlockTypeAndHeight(x: Int, y: Int, z: Int): Pair<Material, Double> {
            val blockType = getBlockType(x, y, z)
            val name = blockType.name
            val height = if (name.endsWith("_SLAB") || name.endsWith("_SLAB2")) {
                val block = world?.getBlockAt(x, y, z)
                if (block != null) {
                    val slab = block.blockData as? Slab
                    if (slab != null) {
                        when (slab.type) {
                            Slab.Type.TOP, Slab.Type.DOUBLE -> 1.0
                            else -> 0.5
                        }
                    } else 1.0
                } else 1.0
            } else {
                BlockHeightCache.getHeight(blockType.name)
            }
            return blockType to height
        }

        override fun createCopy(world: World, x: Int, z: Int): MinecraftWorldAccess.BlockAccess {
            return PEBlockAccess(world, x, z)
        }
    }

    object BlockHeightCache {
        private val heightMap: Map<String, Double> by lazy {
            try {
                val stream = PEMinecraftWorldAccess::class.java.classLoader.getResourceAsStream("core/block_height.json")
                    ?: return@lazy emptyMap<String, Double>()
                val content = stream.readBytes().toString(Charsets.UTF_8)
                stream.close()
                val json = JsonParser().parse(content).asJsonObject
                json.entrySet().associate { (k, v) -> k to v.asDouble }
            } catch (e: Exception) {
                emptyMap<String, Double>()
            }
        }

        fun getHeight(blockName: String): Double = heightMap[blockName] ?: 0.0
    }
}
