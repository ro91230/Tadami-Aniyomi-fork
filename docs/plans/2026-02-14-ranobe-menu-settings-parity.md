# Ranobe Menu/Settings Parity Implementation Plan

> Progress rule: update checkboxes `[ ] -> [x]` only after implementation + verification for each item.

## 0. Baseline
- [x] Commit existing unrelated working-tree changes as baseline checkpoint.

## 1. Critical parity in existing novel screens
- [ ] Add source settings action from novel entry screen toolbar.
- [ ] Add migrate action from novel entry screen toolbar.
- [ ] Add tracking action parity in novel entry screens (Aurora + standard) with backend support.
- [ ] Bring `NovelLibrarySettingsDialog` to parity with anime/manga for display/filter toggles where backend exists.
- [ ] Add/adjust tests for novel entry toolbar actions and novel library settings parity.

## 2. Browse parity (novel extension details + source preferences + migration)
- [ ] Add `browse/novel/extension/details/*` screens + screen model parity.
- [ ] Wire novel extensions list to open details screen.
- [ ] Add `NovelSourcePreferencesScreen` for `ConfigurableNovelSource`.
- [ ] Wire novel source toolbar/settings entry points to source preferences screen.
- [ ] Add novel migration flow in browse (source tab + screen model + screen/dialogs).
- [ ] Add tests for novel extension details/source prefs/migration navigation.

## 3. More tabs parity (history/downloads/stats/storage)
- [ ] Add novel history tab + screen model + UI and include in `HistoriesTab`.
- [ ] Add novel download queue tab + screen model + UI and include in `DownloadsTab`.
- [ ] Add novel stats tab and include in `StatsTab`.
- [ ] Add novel storage tab and include in `StorageTab`.
- [ ] Add/adjust tests for all added novel tabs.

## 4. Deeplink + tracking backend parity
- [ ] Add novel deeplink flow (`Activity` + `Screen` + `ScreenModel`).
- [ ] Implement full novel tracking backend/domain parity (repo/interactors/models) and UI integration.
- [ ] Add tests for novel deeplink and tracking flows.

## 5. Final verification
- [ ] Run targeted unit/UI tests for touched modules.
- [ ] Run project verification subset for changed features.
- [ ] Update this plan with final statuses.
