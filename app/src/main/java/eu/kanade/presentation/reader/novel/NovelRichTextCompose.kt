package eu.kanade.presentation.reader.novel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import eu.kanade.tachiyomi.ui.reader.novel.NovelRichTextSegment
import eu.kanade.tachiyomi.ui.reader.novel.NovelRichTextStyle
import android.graphics.Color as AndroidColor

internal fun buildNovelRichAnnotatedString(
    segments: List<NovelRichTextSegment>,
): AnnotatedString {
    if (segments.isEmpty()) return AnnotatedString("")

    return buildAnnotatedString {
        var cursor = 0
        segments.forEach { segment ->
            if (segment.text.isEmpty()) return@forEach
            val start = cursor
            append(segment.text)
            cursor += segment.text.length
            val end = cursor

            buildNovelRichSpanStyle(segment.style)?.let { addStyle(it, start, end) }
            segment.linkUrl?.takeIf { it.isNotBlank() }?.let { url ->
                addStringAnnotation(tag = "URL", annotation = url, start = start, end = end)
            }
        }
    }
}

private fun buildNovelRichSpanStyle(style: NovelRichTextStyle): SpanStyle? {
    val decorations = buildList {
        if (style.underline) add(TextDecoration.Underline)
        if (style.strikeThrough) add(TextDecoration.LineThrough)
    }
    val textDecoration = when (decorations.size) {
        0 -> null
        1 -> decorations.first()
        else -> TextDecoration.combine(decorations)
    }
    val color = parseNovelRichCssColor(style.colorCss)
    val background = parseNovelRichCssColor(style.backgroundColorCss)

    val spanStyle = SpanStyle(
        fontWeight = if (style.bold) FontWeight.Bold else null,
        fontStyle = if (style.italic) FontStyle.Italic else null,
        textDecoration = textDecoration,
        color = color ?: Color.Unspecified,
        background = background ?: Color.Unspecified,
    )

    return spanStyle.takeUnless { it == SpanStyle() }
}

private fun parseNovelRichCssColor(value: String?): Color? {
    val normalized = value?.trim().orEmpty()
    if (normalized.isBlank()) return null
    val hex = normalized.removePrefix("#")
    return when (hex.length) {
        6 -> runCatching {
            val rgb = hex.toLong(16).toInt()
            val argb = (0xFF shl 24) or rgb
            Color(argb)
        }.getOrNull()
        8 -> runCatching {
            val rgba = hex.toLong(16).toInt()
            val rr = (rgba shr 24) and 0xFF
            val gg = (rgba shr 16) and 0xFF
            val bb = (rgba shr 8) and 0xFF
            val aa = rgba and 0xFF
            val argb = (aa shl 24) or (rr shl 16) or (gg shl 8) or bb
            Color(argb)
        }.getOrNull() ?: runCatching {
            Color(AndroidColor.parseColor(normalized))
        }.getOrNull()
        else -> runCatching { Color(AndroidColor.parseColor(normalized)) }.getOrNull()
    }
}
