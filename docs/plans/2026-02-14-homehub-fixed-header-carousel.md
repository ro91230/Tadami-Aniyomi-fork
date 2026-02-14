# Fixed Home Header + Swipe Carousel Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Сделать шапку Home фиксированной (гибридно скрываемой), а контент под ней перелистываемым как карусель Anime/Manga/Novel (свайп + кнопки, одинаковая плавная анимация).

**Architecture:** Сохраняем текущую архитектуру `HomeHubTab + TabbedScreenAurora + HorizontalPager`. Вынесем шапку из `LazyColumn` страниц в верхний слой, добавим родительское управление видимостью шапки по вертикальному скроллу и сброс скролла страницы вверх при смене раздела.

**Tech Stack:** Kotlin, Jetpack Compose, Voyager tabs/pager, existing Aurora components.

---

## Progress
- [x] Task 1: Вынести header из контента страниц в фиксированный слой
- [x] Task 2: Единый путь анимации переключения (свайп и кнопки)
- [x] Task 3: Гибридное скрытие/показ header по вертикальному скроллу
- [x] Task 4: Сброс скролла страницы вверх при смене раздела
- [x] Task 5: Единый профиль header (ник/аватар/приветствие)
- [x] Task 6: Unit-тесты логики header
- [ ] Task 7: Проверка (manual QA на устройстве/эмуляторе не выполнен в этой сессии)

## Task 1: Extract fixed header
**Files:**
- Modify: `app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeHubTab.kt`

**Steps:**
1. Удалить `inline_header` из `HomeHubScreen` (`LazyColumn`).
2. Добавить `HomeHubPinnedHeader(...)` с текущим визуалом (без редизайна кнопок).
3. Рендерить `HomeHubPinnedHeader` в `HomeHubTab.Content()` над pager.
4. Сохранить текущие действия header (аватар, ник, переключение секций).

## Task 2: One animation path for swipe and buttons
**Files:**
- Modify: `app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeHubTab.kt`

**Steps:**
1. Оставить `rememberPagerState` и `HorizontalPager` через `TabbedScreenAurora`.
2. Клики по кнопкам секций вести в тот же механизм `animateScrollToPage`.
3. Убедиться, что переход визуально одинаков для свайпа и клика.

## Task 3: Hybrid header visibility (hide on down, show on up)
**Files:**
- Modify: `app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeHubTab.kt`

**Steps:**
1. В `HomeHubScreen` отслеживать вертикальный скролл `LazyListState` (направление + top).
2. Передавать события в родителя и хранить состояние видимости header.
3. Правила:
   - скролл вниз -> скрывать,
   - скролл вверх -> показывать,
   - вверху списка -> всегда показывать.
4. Добавить плавную анимацию появления/скрытия header.

## Task 4: Reset page scroll to top on section switch
**Files:**
- Modify: `app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeHubTab.kt`

**Steps:**
1. Добавить reset-триггер при смене секции.
2. Пробросить триггер в `HomeHubScreen`.
3. На изменение триггера выполнять `listState.scrollToItem(0)`.
4. Проверить оба пути смены секции: свайп и кнопки.

## Task 5: Unified header profile behavior
**Files:**
- Modify: `app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeHubTab.kt`
- Optional: `app/src/main/java/eu/kanade/tachiyomi/ui/home/HomeHubScreenModel.kt`
- Optional: `app/src/main/java/eu/kanade/tachiyomi/ui/home/MangaHomeHubScreenModel.kt`
- Optional: `app/src/main/java/eu/kanade/tachiyomi/ui/home/NovelHomeHubScreenModel.kt`

**Steps:**
1. Использовать единые данные профиля (общие prefs уже есть).
2. Сохранить мгновенное обновление ника/аватарки из фиксированной шапки.
3. Убедиться, что поведение одинаково во всех секциях.

## Task 6: Unit tests
**Files:**
- Create: `app/src/test/java/eu/kanade/tachiyomi/ui/home/HomeHubHeaderBehaviorTest.kt`

**Steps:**
1. Тесты для видимости header (down/up/atTop).
2. Тесты для reset-политики при смене секций.
3. JVM unit tests без UI instrumentation.

## Task 7: Verification
**Commands:**
1. `./gradlew :app:testDebugUnitTest --tests "*HomeHubHeaderBehaviorTest*"`
2. `./gradlew :app:assembleDebug`

**Status:**
- [x] `./gradlew :app:testDebugUnitTest --tests "*HomeHubHeaderBehaviorTest*"`
- [x] `./gradlew :app:assembleDebug`
- [ ] Manual QA сценарии на устройстве/эмуляторе

**Manual QA:**
1. Header скрывается при скролле вниз.
2. Header возвращается при скролле вверх.
3. Свайп между Anime/Manga/Novel плавный.
4. Кнопки секций дают ту же анимацию и тот же результат.
5. При смене секции список стартует сверху.
6. Редактирование ника/аватара работает из fixed header.

## Assumptions
1. Визуал кнопок секций не меняется.
2. Нет авто-карусели по таймеру.
3. Позиции списков между секциями не сохраняются (всегда сверху).
4. Реализация без лишних архитектурных перестроек.

## Hotfix: Smooth Header Collapse (2026-02-14)
- [x] Step 1: Replace visibility toggle (`AnimatedVisibility`) with pixel offset collapse.
- [x] Step 2: Pass raw `deltaY` from `nestedScroll` to parent and compute header offset via `resolveHomeHubHeaderOffset`.
- [x] Step 3: Update `HomeHubPinnedHeader` to `Layout + clipToBounds` so text and container move as one block.
- [x] Step 4: Add unit tests for offset rules (down/up/clamp/atTop reset).
- [x] Step 5: Verify with `./gradlew :app:testDebugUnitTest --tests "*HomeHubHeaderBehaviorTest*"`.
- [x] Step 6: Verify compilation with `./gradlew :app:compileDebugKotlin`.
- [ ] Step 7: Manual QA in app (expected: header scrolls out as one unit without staged fade/disappear).
