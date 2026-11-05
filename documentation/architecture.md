# Architecture

## Layers

Lumen uses MVVM with a repository layer as the single source of truth (SSOT).

| Layer | Modules | Responsibility |
|---|---|---|
| UI | `feature:*`, `core:ui`, `core:designsystem` | Composables and ViewModels. Shows state and sends user actions |
| Domain | `core:domain`, `core:model` | Repository interfaces, use cases, the game state machine and the domain models. Plain Kotlin, no Android |
| Data | `core:data` | Repository implementations, mapping between data types and domain models |
| Data sources | `core:database`, `core:datastore`, `core:network` | Room, DataStore and Retrofit |
