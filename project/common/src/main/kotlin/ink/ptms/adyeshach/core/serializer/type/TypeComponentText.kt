package ink.ptms.adyeshach.core.serializer.type

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import ink.ptms.adyeshach.core.serializer.SerializerType
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import java.lang.reflect.Type

/**
 * TabooLib ComponentText 的 Gson 序列化器。
 * 序列化存储 toRawMessage() JSON，反序列化兼容纯文本与 JSON。
 */
@SerializerType(baseClass = ComponentText::class)
class TypeComponentText : JsonSerializer<ComponentText>, JsonDeserializer<ComponentText> {

    override fun serialize(src: ComponentText, type: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toRawMessage())
    }

    override fun deserialize(json: JsonElement, type: Type?, context: JsonDeserializationContext): ComponentText {
        val str = json.asString
        return if (str.startsWith('{') && str.endsWith('}')) {
            Components.parseRaw(str)
        } else {
            Components.text(str)
        }
    }
}
