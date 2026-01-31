package ink.ptms.adyeshach.impl

import com.github.retrooper.packetevents.PacketEvents
import ink.ptms.adyeshach.impl.manager.AdyeshachPacketListener
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.platform.util.bukkitPlugin

object PacketLoader {

    @Awake(LifeCycle.ENABLE)
    fun enable() {
        PacketEvents.getAPI().eventManager.registerListener(AdyeshachPacketListener())
    }

}
