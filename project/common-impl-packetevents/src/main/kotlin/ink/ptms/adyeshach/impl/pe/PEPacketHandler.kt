package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.wrapper.PacketWrapper
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.MinecraftMeta
import ink.ptms.adyeshach.core.MinecraftPacketHandler
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * PacketEvents 实现的 MinecraftPacketHandler
 */
class PEPacketHandler : MinecraftPacketHandler {

    private val buffer = ConcurrentHashMap<Player, ConcurrentLinkedQueue<Any>>()
    private val metaBuffer = ConcurrentHashMap<Player, ConcurrentLinkedQueue<BufferPacket>>()

    override fun sendPacket(player: List<Player>, packet: Any) {
        player.forEach {
            buffer.getOrPut(it) { ConcurrentLinkedQueue() }.offer(packet)
        }
    }

    override fun bufferMetadataPacket(player: List<Player>, id: Int, packet: MinecraftMeta) {
        player.forEach {
            metaBuffer.getOrPut(it) { ConcurrentLinkedQueue() }.offer(BufferPacket(id, packet))
        }
    }

    override fun flush(player: List<Player>) {
        player.forEach { p ->
            val user = PacketEvents.getAPI().playerManager.getUser(p) ?: return@forEach
            buffer.remove(p)?.let { queue ->
                queue.forEach { packet ->
                    when (packet) {
                        is PacketWrapper<*> -> PacketEvents.getAPI().protocolManager.sendPacket(user.channel, packet)
                        else -> PacketEvents.getAPI().protocolManager.sendPacket(user.channel, packet)
                    }
                }
            }
            metaBuffer.remove(p)?.let { queue ->
                val metadataHandler = Adyeshach.api().getMinecraftAPI().getEntityMetadataHandler()
                val packets = queue.groupBy { it.id }.map { (id, packets) ->
                    metadataHandler.createMetadataPacket(id, packets.map { it.packet })
                }
                packets.forEach { packet ->
                    when (packet) {
                        is PacketWrapper<*> -> PacketEvents.getAPI().protocolManager.sendPacket(user.channel, packet)
                        else -> PacketEvents.getAPI().protocolManager.sendPacket(user.channel, packet)
                    }
                }
            }
        }
    }

    private data class BufferPacket(val id: Int, val packet: MinecraftMeta)
}
