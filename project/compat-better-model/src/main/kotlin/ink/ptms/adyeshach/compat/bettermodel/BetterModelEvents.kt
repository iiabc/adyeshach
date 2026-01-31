package ink.ptms.adyeshach.compat.bettermodel

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.core.entity.BetterModelView
import ink.ptms.adyeshach.core.entity.manager.event.MetaUpdateEvent
import ink.ptms.adyeshach.core.event.AdyeshachEntityRemoveEvent
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.registerBukkitListener
import java.util.function.Consumer

internal object BetterModelEvents {

    @Awake(LifeCycle.ENABLE)
    fun init() {
        if (!DefaultBetterModel.isBetterModelHooked()) return

        Adyeshach.api().getEventBus().prepareSpawn { e ->
            val view = e.entity as? BetterModelView ?: return@prepareSpawn true
            if (view.betterModelName.isNotBlank()) {
                view.refreshModel()
            }
            true
        }

        Adyeshach.api().getEventBus().prepareDestroy { e ->
            val view = e.entity as? BetterModelView ?: return@prepareDestroy true
            if (view.betterModelName.isNotBlank()) {
                view.hideModel(e.viewer)
            }
            true
        }

        Adyeshach.api().getEventBus().postSpawn { e ->
            (e.entity as? BetterModelView)?.showModel(e.viewer)
        }

        Adyeshach.api().getEventBus().postDestroy { e ->
            (e.entity as? BetterModelView)?.hideModel(e.viewer)
        }

        Adyeshach.api().getEventBus().postTeleport { e ->
            val view = e.entity as? BetterModelView ?: return@postTeleport
            if (view is DefaultBetterModel) {
                view.teleportModel(e.location)
            } else {
                view.refreshModel()
            }
        }

        Adyeshach.api().getEventBus().prepareMove(Consumer { e ->
            val view = e.entity as? BetterModelView ?: return@Consumer
            if (e.isMoving) view.playWalk() else view.playIdle()
        })

        Adyeshach.api().getEventBus().postMetaUpdate(Consumer { e: MetaUpdateEvent ->
            val view = e.entity as? BetterModelView ?: return@Consumer
            if (e.key == "customName" || e.key == "isCustomNameVisible") {
                view.updateModelNameTag()
            }
            if (e.key == "isInvisible") {
                e.entity.getVisiblePlayers().forEach { viewer ->
                    if ((e.value as? Boolean) == true) view.hideModel(viewer) else view.showModel(viewer)
                }
            }
        })

        registerBukkitListener(AdyeshachEntityRemoveEvent::class.java) { e ->
            (e.entity as? BetterModelView)?.destroyModel()
        }
    }
}
