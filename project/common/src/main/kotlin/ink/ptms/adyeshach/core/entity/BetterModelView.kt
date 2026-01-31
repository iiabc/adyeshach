package ink.ptms.adyeshach.core.entity

import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * BetterModel 模型视图接口
 * 仅 AdyInteraction 实体类型支持
 */
interface BetterModelView {

    var betterModelName: String

    fun showModel(viewer: Player): Boolean

    fun hideModel(viewer: Player): Boolean

    fun destroyModel()

    fun teleportModel(location: Location)

    fun refreshModel(): Boolean

    fun updateModelNameTag()

    fun playIdle()

    fun playWalk()
}
