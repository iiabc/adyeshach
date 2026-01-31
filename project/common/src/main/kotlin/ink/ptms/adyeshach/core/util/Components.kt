package ink.ptms.adyeshach.core.util

import org.bukkit.inventory.ItemStack
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.RawMessage

/**
 * Adyeshach
 * ink.ptms.adyeshach.core.util.Components
 *
 * 基于 TabooLib ComponentText 的文本工具。
 *
 * @author 坏黑
 * @since 2023/2/3 23:52
 */
object Components {

    fun toLegacyText(value: String): String {
        return if (value.startsWith('{') && value.endsWith('}')) {
            runCatching { Components.parseRaw(value).toLegacyText() }.getOrElse { value }
        } else {
            value
        }
    }

    /**
     * 将任意值转换为可显示的 Legacy 文本
     */
    fun toLegacyText(value: Any?): String {
        if (value == null) return ""
        return when (value) {
            is String -> toLegacyText(value)
            is ComponentText -> value.toLegacyText()
            is RawMessage -> value.toLegacyText()
            is ItemStack -> toItemDisplayString(value)
            else -> value.toString()
        }
    }

    private fun toItemDisplayString(item: ItemStack): String {
        if (item.type.isAir) return "Air"
        val name = item.itemMeta?.getDisplayName()?.takeIf { it.isNotBlank() }?.let { toLegacyText(it) }
        return name ?: "${item.type.name} x${item.amount}"
    }

    fun toRawMessage(value: Any): String {
        return when (value) {
            is ComponentText -> value.toRawMessage()
            is RawMessage -> value.toRawMessage()
            else -> {
                val str = value.toString()
                if (str.startsWith('{') && str.endsWith('}')) str
                else Components.text(str).toRawMessage()
            }
        }
    }
}
