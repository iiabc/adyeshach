package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.protocol.player.GameMode
import com.github.retrooper.packetevents.protocol.player.TextureProperty
import com.github.retrooper.packetevents.protocol.player.UserProfile
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.MinecraftEntityPlayerHandler
import ink.ptms.adyeshach.core.bukkit.data.GameProfile
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.EnumSet
import java.util.UUID

/**
 * PacketEvents 实现的 MinecraftEntityPlayerHandler
 */
class PEEntityPlayerHandler : MinecraftEntityPlayerHandler {

    private val packetHandler
        get() = Adyeshach.api().getMinecraftAPI().getPacketHandler()

    override fun addPlayerInfo(player: Player, uuid: UUID, gameProfile: GameProfile) {
        val peProfile = UserProfile(uuid, gameProfile.name)
        if (gameProfile.texture.size >= 2) {
            peProfile.textureProperties.add(TextureProperty("textures", gameProfile.texture[0], gameProfile.texture.getOrElse(1) { "" }))
        }
        val playerInfo = WrapperPlayServerPlayerInfoUpdate.PlayerInfo(peProfile).apply {
            setListed(gameProfile.listed)
            setLatency(gameProfile.ping)
            setGameMode(if (gameProfile.spectator) GameMode.SPECTATOR else GameMode.CREATIVE)
            setDisplayName(Component.text(gameProfile.name))
        }
        val packet = WrapperPlayServerPlayerInfoUpdate(
            EnumSet.of(WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME, WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED),
            playerInfo
        )
        packetHandler.sendPacket(listOf(player), packet)
    }

    override fun removePlayerInfo(player: Player, uuid: UUID) {
        packetHandler.sendPacket(listOf(player), WrapperPlayServerPlayerInfoRemove(uuid))
    }
}
