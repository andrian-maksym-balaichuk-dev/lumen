package sk.mpage.lumen.core.data.mapper

import sk.mpage.lumen.core.common.text.HtmlEntityDecoder
import sk.mpage.lumen.core.database.entity.CategoryEntity
import sk.mpage.lumen.core.model.Category
import sk.mpage.lumen.core.network.model.RemoteCategory
import java.time.Instant

internal fun RemoteCategory.toEntity(
    decoder: HtmlEntityDecoder,
    updatedAt: Instant,
): CategoryEntity = CategoryEntity(id = id, name = decoder.decode(name), updatedAt = updatedAt)

internal fun CategoryEntity.toCategory(): Category = Category(id = id, name = name)
