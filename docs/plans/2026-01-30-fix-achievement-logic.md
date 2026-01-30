# Fix Achievement System Logic Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix critical bugs in achievement logic where achievements trigger incorrectly or don't trigger at all due to pattern matching and validation issues.

**Architecture:**
- Achievement events flow through `AchievementHandler` → `calculateProgress` → `isEventMatch`
- EVENT-type achievements use pattern matching on achievement IDs
- Secret achievements use dedicated checker methods
- Current issues: pattern mismatches, wrong validation logic, no actual progress tracking

**Tech Stack:**
- Kotlin + Coroutines
- SqlDelight for database queries
- JUnit 5 + Kotest + MockK for testing

---

## Phase 1: Fix Critical Bug - read_long_manga Triggers on Any Chapter Read

### Task 1: Prevent read_long_manga from triggering in handleChapterRead

**Files:**
- Modify: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:97-116`

**Problem:** `read_long_manga` achievement has type EVENT and ID contains "read", so it triggers on every `ChapterRead` event via pattern match.

**Step 1: Write failing test**

Create: `data/src/test/java/tachiyomi/data/achievement/AchievementHandlerTest.kt`

```kotlin
@Test
fun `read_long_manga does not trigger on regular chapter read`() = runTest {
    // Arrange
    val manga = createTestManga(id = 1, title = "Test Manga")
    val chapter = createTestChapter(id = 1, mangaId = 1, chapterNumber = 1.0f, read = true)

    // Act - read a chapter in a non-completed manga with <200 chapters
    eventBus.tryEmit(AchievementEvent.ChapterRead(chapterId = chapter.id!!, mangaId = manga.id))

    // Assert - achievement should NOT be unlocked
    val progress = repository.getProgress("read_long_manga").first()
    assertThat(progress?.isUnlocked).isFalse()
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: FAIL (achievement incorrectly unlocks)

**Step 3: Modify handleChapterRead to filter out read_long_manga**

Edit: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:104-111`

```kotlin
private suspend fun handleChapterRead(event: AchievementEvent.ChapterRead) {
    logcat(LogPriority.INFO) { "[ACHIEVEMENTS] handleChapterRead: mangaId=${event.mangaId}, chapter=${event.chapterNumber}" }
    streakChecker.logChapterRead()

    val achievements = getAchievementsForCategory(AchievementCategory.MANGA)
    logcat(LogPriority.INFO) { "[ACHIEVEMENTS] Found ${achievements.size} MANGA achievements (incl BOTH)" }

    val relevantAchievements = achievements.filter {
        it.type == AchievementType.QUANTITY ||
            it.type == AchievementType.EVENT ||
            it.type == AchievementType.STREAK ||
            it.type == AchievementType.DIVERSITY ||
            it.type == AchievementType.LIBRARY ||
            it.type == AchievementType.META
    }.filter {
        // Exclude read_long_manga - it should only trigger on MangaCompleted event
        it.id != "read_long_manga"
    }

    relevantAchievements.forEach { achievement ->
        checkAndUpdateProgress(achievement, event)
    }
}
```

**Step 4: Run test to verify it passes**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: PASS

**Step 5: Commit**

```bash
git add data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt
git add data/src/test/java/tachiyomi/data/achievement/AchievementHandlerTest.kt
git commit -m "fix(achievements): prevent read_long_manga from triggering on regular chapter reads"
```

---

### Task 2: Fix checkLongMangaAchievement to validate read chapters, not total chapters

**Files:**
- Modify: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:536-541`
- Create: `data/src/main/sqldelight/data/history.sq` - add new query if needed

**Problem:** Current implementation uses `getChapterCountByMangaId` which returns ALL chapters in manga, not READ chapters.

**Step 1: Check if history query exists**

Read: `data/src/main/sqldelight/data/history.sq`

Look for existing query like `getChaptersReadByMangaId`. If not exists, we need to add it.

**Step 2: Add SQL query if missing**

If query doesn't exist in `data/src/main/sqldelight/data/history.sq`, add:

```sql
getChaptersReadByMangaId:
SELECT COUNT(*)
FROM history
INNER JOIN chapters ON history.chapter_id = chapters._id
WHERE chapters.manga_id = :mangaId;
```

**Step 3: Write failing test**

```kotlin
@Test
fun `read_long_manga requires 200+ chapters read in completed manga`() = runTest {
    // Arrange - manga with 250 chapters, only 50 read
    val manga = createTestManga(id = 1, title = "Long Manga", status = SManga.COMPLETED.toLong())
    insertChapters(mangaId = 1, count = 250)
    markChaptersAsRead(mangaId = 1, count = 50) // Only 50 read

    // Act
    eventBus.tryEmit(AchievementEvent.MangaCompleted(mangaId = manga.id))

    // Assert - should NOT unlock (only 50 read, not 200+)
    val progress = repository.getProgress("read_long_manga").first()
    assertThat(progress?.isUnlocked).isFalse()
}

@Test
fun `read_long_manga unlocks when 200+ chapters read in completed manga`() = runTest {
    // Arrange - manga with 250 chapters, 200+ read
    val manga = createTestManga(id = 2, title = "Long Manga", status = SManga.COMPLETED.toLong())
    insertChapters(mangaId = 2, count = 250)
    markChaptersAsRead(mangaId = 2, count = 200) // 200 read

    // Act
    eventBus.tryEmit(AchievementEvent.MangaCompleted(mangaId = manga.id))

    // Assert - should unlock
    val progress = repository.getProgress("read_long_manga").first()
    assertThat(progress?.isUnlocked).isTrue()
}
```

**Step 4: Run test to verify it fails**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: FAIL (uses wrong query)

**Step 5: Implement fix**

Edit: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:536-541`

```kotlin
/**
 * Специальная проверка для достижения "Долгостройщик"
 * Требует: манга завершена И 200+ глав прочитано
 */
private suspend fun checkLongMangaAchievement(event: AchievementEvent.MangaCompleted): Boolean {
    // Check how many chapters were ACTUALLY READ (not total chapters)
    val chaptersRead = mangaHandler.awaitOneOrNull {
        historyQueries.getChaptersReadByMangaId(event.mangaId)
    } ?: 0L
    return chaptersRead >= 200
}
```

**Step 6: Run test to verify it passes**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: PASS

**Step 7: Commit**

```bash
git add data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt
git add data/src/main/sqldelight/data/history.sq
git add data/src/test/java/tachiyomi/data/achievement/AchievementHandlerTest.kt
git commit -m "fix(achievements): read_long_manga now checks read chapters instead of total chapters"
```

---

## Phase 2: Fix Pattern Matching Issues for complete_1_manga and complete_1_anime

### Task 3: Fix isEventMatch pattern for complete_1_manga

**Files:**
- Modify: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:500-503`

**Problem:** Achievement ID `"complete_1_manga"` doesn't match any of the patterns:
- `"manga_complete"` ❌
- `"completed_manga"` ❌
- `"manga_completed"` ❌

**Step 1: Write failing test**

```kotlin
@Test
fun `complete_1_manga triggers on manga completion`() = runTest {
    // Arrange
    val manga = createTestManga(id = 1, title = "Test", status = SManga.COMPLETED.toLong())

    // Act
    eventBus.tryEmit(AchievementEvent.MangaCompleted(mangaId = manga.id))

    // Assert
    val progress = repository.getProgress("complete_1_manga").first()
    assertThat(progress?.isUnlocked).isTrue()
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: FAIL (pattern doesn't match)

**Step 3: Fix pattern matching**

Edit: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:500-503`

Current code:
```kotlin
is AchievementEvent.MangaCompleted ->
    id.contains("manga_complete") || id.contains("completed_manga") || id.contains("manga_completed") || id == "read_long_manga"
```

Fix to:
```kotlin
is AchievementEvent.MangaCompleted ->
    id.contains("manga_complete") ||
    id.contains("completed_manga") ||
    id.contains("manga_completed") ||
    (id.contains("complete") && id.contains("_manga")) ||  // Matches "complete_1_manga"
    id == "read_long_manga"
```

**Step 4: Run test to verify it passes**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: PASS

**Step 5: Commit**

```bash
git add data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt
git add data/src/test/java/tachiyomi/data/achievement/AchievementHandlerTest.kt
git commit -m "fix(achievements): fix pattern matching for complete_1_manga achievement"
```

---

### Task 4: Fix isEventMatch pattern for complete_1_anime

**Files:**
- Modify: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:502-504`

**Problem:** Same issue with `"complete_1_anime"` not matching patterns.

**Step 1: Write failing test**

```kotlin
@Test
fun `complete_1_anime triggers on anime completion`() = runTest {
    // Arrange
    val anime = createTestAnime(id = 1, title = "Test", status = SManga.COMPLETED.toLong())

    // Act
    eventBus.tryEmit(AchievementEvent.AnimeCompleted(animeId = anime.id))

    // Assert
    val progress = repository.getProgress("complete_1_anime").first()
    assertThat(progress?.isUnlocked).isTrue()
}
```

**Step 2: Run test to verify it fails**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: FAIL

**Step 3: Fix pattern matching**

Edit: `data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt:502-504`

Current code:
```kotlin
is AchievementEvent.AnimeCompleted ->
    id.contains("anime_complete") || id.contains("completed_anime") || id.contains("anime_completed")
```

Fix to:
```kotlin
is AchievementEvent.AnimeCompleted ->
    id.contains("anime_complete") ||
    id.contains("completed_anime") ||
    id.contains("anime_completed") ||
    (id.contains("complete") && id.contains("_anime"))  // Matches "complete_1_anime"
```

**Step 4: Run test to verify it passes**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.AchievementHandlerTest`
Expected: PASS

**Step 5: Commit**

```bash
git add data/src/main/java/tachiyomi/data/achievement/handler/AchievementHandler.kt
git add data/src/test/java/tachiyomi/data/achievement/AchievementHandlerTest.kt
git commit -m "fix(achievements): fix pattern matching for complete_1_anime achievement"
```

---

## Phase 3: Comprehensive Testing

### Task 5: Add integration test for all EVENT achievements

**Files:**
- Create: `data/src/test/java/tachiyomi/data/achievement/EventAchievementsIntegrationTest.kt`

**Step 1: Write comprehensive test**

```kotlin
@Test
fun `all EVENT achievements trigger correctly`() = runTest {
    // Test first_chapter
    val manga1 = createTestManga(id = 1)
    val chapter1 = createTestChapter(id = 1, mangaId = 1, read = true)
    eventBus.tryEmit(AchievementEvent.ChapterRead(chapterId = 1, mangaId = 1))
    assertThat(repository.getProgress("first_chapter").first()?.isUnlocked).isTrue()

    // Test complete_1_manga
    val manga2 = createTestManga(id = 2, status = SManga.COMPLETED.toLong())
    eventBus.tryEmit(AchievementEvent.MangaCompleted(mangaId = 2))
    assertThat(repository.getProgress("complete_1_manga").first()?.isUnlocked).isTrue()

    // Test read_long_manga - should NOT trigger with only 50 chapters read
    val manga3 = createTestManga(id = 3, status = SManga.COMPLETED.toLong())
    insertChapters(mangaId = 3, count = 50)
    markChaptersAsRead(mangaId = 3, count = 50)
    eventBus.tryEmit(AchievementEvent.MangaCompleted(mangaId = 3))
    assertThat(repository.getProgress("read_long_manga").first()?.isUnlocked).isFalse()

    // Test first_episode
    val anime1 = createTestAnime(id = 1)
    val episode1 = createTestEpisode(id = 1, animeId = 1, watch = true)
    eventBus.tryEmit(AchievementEvent.EpisodeWatched(episodeId = 1, animeId = 1))
    assertThat(repository.getProgress("first_episode").first()?.isUnlocked).isTrue()

    // Test complete_1_anime
    val anime2 = createTestAnime(id = 2, status = SManga.COMPLETED.toLong())
    eventBus.tryEmit(AchievementEvent.AnimeCompleted(animeId = 2))
    assertThat(repository.getProgress("complete_1_anime").first()?.isUnlocked).isTrue()
}
```

**Step 2: Run test**

Run: `./gradlew --no-daemon :data:test --tests tachiyomi.data.achievement.EventAchievementsIntegrationTest`
Expected: PASS

**Step 3: Commit**

```bash
git add data/src/test/java/tachiyomi/data/achievement/EventAchievementsIntegrationTest.kt
git commit -m "test(achievements): add comprehensive integration test for EVENT achievements"
```

---

## Phase 4: Documentation

### Task 6: Update achievement system documentation

**Files:**
- Modify: `docs/ACHIEVEMENTS_GUIDE.md` (if exists) or create

**Step 1: Document the fixes**

Add section about EVENT achievement pattern matching rules:

```
# EVENT Achievement Pattern Matching

EVENT-type achievements use pattern matching on achievement IDs to determine
which events they respond to. The pattern matching happens in `isEventMatch()`.

## Rules

1. **ChapterRead events**: Match IDs containing "chapter" or "read"
   - Example: "first_chapter" ✅, "read_10_chapters" ❌ (use QUANTITY)

2. **MangaCompleted events**: Match IDs containing:
   - "manga_complete" OR
   - "completed_manga" OR
   - "manga_completed" OR
   - "complete" + "_manga"
   - Special case: "read_long_manga"

3. **AnimeCompleted events**: Match IDs containing:
   - "anime_complete" OR
   - "completed_anime" OR
   - "anime_completed" OR
   - "complete" + "_anime"

4. **Special achievements that should NOT match ChapterRead**:
   - "read_long_manga" - must be explicitly filtered in handleChapterRead
   - These should only trigger on MangaCompleted event

## Best Practices

- Use descriptive IDs that clearly indicate the event type
- Avoid generic words like "read" in IDs unless it's meant to trigger on ChapterRead
- For completion-based achievements, use "complete_X_manga" or "complete_X_anime" pattern
- Document special validation logic (like read chapter count) in checker methods
```

**Step 2: Commit**

```bash
git add docs/ACHIEVEMENTS_GUIDE.md
git commit -m "docs(achievements): document EVENT achievement pattern matching rules"
```

---

## Phase 5: Verification

### Task 7: Run full test suite and verify all achievements

**Step 1: Run all achievement tests**

```bash
./gradlew --no-daemon :data:test --tests "*achievement*"
```

Expected: ALL PASS

**Step 2: Run integration tests**

```bash
./gradlew --no-daemon :app:testDebugUnitTest --tests "*achievement*"
```

Expected: ALL PASS

**Step 3: Manual testing checklist**

- [ ] Read a chapter → "first_chapter" unlocks, "read_long_manga" does NOT
- [ ] Complete a manga with <200 chapters → "complete_1_manga" unlocks, "read_long_manga" does NOT
- [ ] Complete a manga with 200+ chapters read → "read_long_manga" unlocks
- [ ] Complete an anime → "complete_1_anime" unlocks
- [ ] Check that QUANTITY achievements still count correctly
- [ ] Check that SECRET achievements still work

**Step 4: Final commit**

```bash
git commit --allow-empty -m "chore(achievements): complete achievement logic fixes - all tests passing"
```

---

## Summary of Changes

| File | Change |
|------|--------|
| `AchievementHandler.kt` | Filter out `read_long_manga` in `handleChapterRead` |
| `AchievementHandler.kt` | Fix `checkLongMangaAchievement` to use `getChaptersReadByMangaId` |
| `AchievementHandler.kt` | Fix `isEventMatch` pattern for `complete_1_manga` |
| `AchievementHandler.kt` | Fix `isEventMatch` pattern for `complete_1_anime` |
| `history.sq` | Add `getChaptersReadByMangaId` query (if missing) |
| Test files | Add comprehensive tests for all fixes |
| Documentation | Document EVENT achievement pattern rules |

---

## Testing Strategy

**Unit Tests:**
- Test each achievement trigger in isolation
- Test edge cases (0 chapters, 199 chapters, 200 chapters, 201 chapters)
- Test that achievements don't trigger when they shouldn't

**Integration Tests:**
- Test full event flow from EventBus → Handler → Database
- Test multiple achievements in sequence
- Test that progress persists correctly

**Manual Testing:**
- Real device testing with actual manga/anime
- Verify achievement unlock animations
- Verify points are awarded correctly
- Verify meta achievements update correctly
