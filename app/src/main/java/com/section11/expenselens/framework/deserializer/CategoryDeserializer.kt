package com.section11.expenselens.framework.deserializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.section11.expenselens.domain.models.Category
import java.lang.reflect.Type

class CategoryDeserializer : JsonDeserializer<Category> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Category? {
        val categoryName = json?.asString ?: return null
        return Category.entries.find { it.name.equals(categoryName, ignoreCase = true) }
    }
}
