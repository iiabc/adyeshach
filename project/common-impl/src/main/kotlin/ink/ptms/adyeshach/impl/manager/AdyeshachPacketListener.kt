package ink.ptms.adyeshach.impl.manager

import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client
import com.github.retrooper.packetevents.protocol.player.InteractionHand
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.event.AdyeshachEntityDamageEvent
import ink.ptms.adyeshach.core.event.AdyeshachEntityInteractEvent
import ink.ptms.adyeshach.core.event.AdyeshachPlayerJoinEvent
import ink.ptms.adyeshach.core.util.safeDistance
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import taboolib.common.platform.function.submit

/**
 * 使用 SimplePacketListenerAbstract，仅接收 PLAY 阶段包
 */
class AdyeshachPacketListener : SimplePacketListenerAbstract() {

    override fun onPacketPlayReceive(event: PacketPlayReceiveEvent) {
        val player = event.getPlayer<Player>() ?: return
        if (player !is Player) return

        when (event.packetType) {
            Client.PLAYER_POSITION,
            Client.PLAYER_POSITION_AND_ROTATION,
            Client.PLAYER_ROTATION -> {
                if (player.name !in DefaultPlayerEvents.onlinePlayerSet) {
                    DefaultPlayerEvents.onlinePlayerSet += player.name
                    AdyeshachPlayerJoinEvent(player).call()
                }
            }
            Client.INTERACT_ENTITY -> {
                val wrapper = WrapperPlayClientInteractEntity(event)
                val entity = Adyeshach.api().getEntityFinder().getEntityFromEntityId(wrapper.entityId, player) ?: return
                if (entity.isViewer(player) && entity.getLocation().safeDistance(player.location) < 10) {
                    when (wrapper.action) {
                        WrapperPlayClientInteractEntity.InteractAction.ATTACK -> {
                            submit { AdyeshachEntityDamageEvent(entity, player).call() }
                        }
                        WrapperPlayClientInteractEntity.InteractAction.INTERACT_AT -> {
                            val target = wrapper.target
                            val vector = target.map { Vector(it.x.toDouble(), it.y.toDouble(), it.z.toDouble()) }.orElse(Vector(0, 0, 0))
                            val hand = wrapper.hand == InteractionHand.MAIN_HAND
                            submit { AdyeshachEntityInteractEvent(entity, player, hand, vector).call() }
                        }
                        else -> {}
                    }
                }
            }
            else -> {}
        }
    }
}
