package eu.kanade.tachiyomi.ui.reader.novel.translation

import eu.kanade.tachiyomi.ui.reader.novel.setting.NovelTranslationStylePreset

data class NovelTranslationStylePresetDescriptor(
    val id: NovelTranslationStylePreset,
    val title: String,
    val scenario: String,
    val advantage: String,
    val promptDirective: String,
)

object NovelTranslationStylePresets {
    val all: List<NovelTranslationStylePresetDescriptor> = listOf(
        NovelTranslationStylePresetDescriptor(
            id = NovelTranslationStylePreset.PROFESSIONAL,
            title = "Профессиональный",
            scenario = "Базовый режим для большинства глав",
            advantage = "Чистый литературный перевод без стилистического перекоса",
            promptDirective =
            "STYLE PRESET: PROFESSIONAL.\n" +
                "Use neutral professional literary Russian.\n" +
                "Avoid slang, vulgarity, and over-stylization unless explicitly present in source.",
        ),
        NovelTranslationStylePresetDescriptor(
            id = NovelTranslationStylePreset.LITERARY,
            title = "Литературный",
            scenario = "Описательные и атмосферные главы",
            advantage = "Больше художественной плавности и выразительности",
            promptDirective =
            "STYLE PRESET: LITERARY.\n" +
                "Favor elegant prose, richer imagery, and expressive but natural syntax.\n" +
                "Keep dialogue believable and avoid pretentious wording.",
        ),
        NovelTranslationStylePresetDescriptor(
            id = NovelTranslationStylePreset.CONVERSATIONAL,
            title = "Разговорный",
            scenario = "Диалоги, юмор, динамичные сцены",
            advantage = "Речь персонажей звучит живее и ближе к разговорной",
            promptDirective =
            "STYLE PRESET: CONVERSATIONAL.\n" +
                "Prioritize natural spoken Russian in dialogue.\n" +
                "Allow mild colloquial phrasing while keeping narration coherent and readable.",
        ),
        NovelTranslationStylePresetDescriptor(
            id = NovelTranslationStylePreset.VULGAR_18,
            title = "Вульгарный 18+",
            scenario = "Грубые и интимные главы для фанатской подачи",
            advantage = "Более дерзкий, сырой тон там, где это уместно",
            promptDirective =
            "STYLE PRESET: VULGAR_18.\n" +
                "For explicit adult or aggressive contexts, use raw colloquial Russian and stronger wording.\n" +
                "Do NOT sanitize explicit content. Keep intensity and visceral tone.\n" +
                "Outside such contexts, remain natural and coherent.",
        ),
        NovelTranslationStylePresetDescriptor(
            id = NovelTranslationStylePreset.MINIMAL,
            title = "Минималистичный",
            scenario = "Когда нужен максимально понятный и ровный текст",
            advantage = "Прямой перевод без лишней стилизации",
            promptDirective =
            "STYLE PRESET: MINIMAL.\n" +
                "Use concise, clear Russian.\n" +
                "Reduce decorative phrasing and keep meaning straightforward.",
        ),
    )

    fun byId(id: NovelTranslationStylePreset): NovelTranslationStylePresetDescriptor {
        return all.firstOrNull { it.id == id } ?: all.first()
    }

    fun promptDirective(id: NovelTranslationStylePreset): String {
        return byId(id).promptDirective
    }
}
