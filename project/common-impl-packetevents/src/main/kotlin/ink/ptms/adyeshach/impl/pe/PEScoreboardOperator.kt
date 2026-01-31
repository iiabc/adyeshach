package ink.ptms.adyeshach.impl.pe

import com.github.retrooper.packetevents.util.ColorUtil
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerTeams
import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.MinecraftScoreboardOperator
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player

/**
 * PacketEvents 实现的 MinecraftScoreboardOperator
 */
class PEScoreboardOperator : MinecraftScoreboardOperator {

    private val packetHandler
        get() = Adyeshach.api().getMinecraftAPI().getPacketHandler()

    override fun updateTeam(player: List<Player>, team: MinecraftScoreboardOperator.Team, method: MinecraftScoreboardOperator.TeamMethod) {
        val teamMode = when (method) {
            MinecraftScoreboardOperator.TeamMethod.ADD -> WrapperPlayServerTeams.TeamMode.CREATE
            MinecraftScoreboardOperator.TeamMethod.REMOVE -> WrapperPlayServerTeams.TeamMode.REMOVE
            MinecraftScoreboardOperator.TeamMethod.CHANGE -> WrapperPlayServerTeams.TeamMode.UPDATE
            MinecraftScoreboardOperator.TeamMethod.JOIN -> WrapperPlayServerTeams.TeamMode.ADD_ENTITIES
            MinecraftScoreboardOperator.TeamMethod.LEAVE -> WrapperPlayServerTeams.TeamMode.REMOVE_ENTITIES
        }
        val optionData = if (team.canSeeInvisible) WrapperPlayServerTeams.OptionData.FRIENDLY_CAN_SEE_INVISIBLE else WrapperPlayServerTeams.OptionData.NONE
        val teamInfo = if (teamMode == WrapperPlayServerTeams.TeamMode.CREATE || teamMode == WrapperPlayServerTeams.TeamMode.UPDATE) {
            WrapperPlayServerTeams.ScoreBoardTeamInfo(
                Component.empty(),
                Component.empty(),
                Component.empty(),
                if (team.nameTagVisible) WrapperPlayServerTeams.NameTagVisibility.ALWAYS else WrapperPlayServerTeams.NameTagVisibility.NEVER,
                if (team.collision) WrapperPlayServerTeams.CollisionRule.ALWAYS else WrapperPlayServerTeams.CollisionRule.NEVER,
                ColorUtil.fromId(team.color.ordinal).let { if (it != null) it else NamedTextColor.WHITE },
                optionData
            )
        } else null
        val packet = WrapperPlayServerTeams(team.name, teamMode, teamInfo, team.members.toList())
        packetHandler.sendPacket(player, packet)
    }
}
