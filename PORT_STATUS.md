# Port status

## What this deliverable is

This project is a buildable-layout Forge 1.20.1 source baseline derived from
commit `677198bb` of AcademyCraft-Reborn, the final commit in that repository's
history before it moved from Minecraft 1.20.1 to 1.21.1. The Forge-only
packaging, expanded ability set, and legacy component chain were updated on
2026-08-15.

The supplied AcademyCraft 1.1.3 JAR is a Minecraft 1.12.2 coremod containing
1,708 class files. Its source tree contains 356 Java files, 61 Scala files,
custom LambdaLib2 rendering/UI/network code, and many legacy Forge APIs. A
complete conversion is therefore a rewrite of several subsystems, not a binary
remap.

## Implemented in this baseline

- Modern Forge entrypoint and registries for blocks, items, fluids, particles,
  entities, menus, sounds, and block entities.
- Modern client/server packet and request/response infrastructure.
- Player ability state, experience synchronization, category acquisition, and
  skill-learning scaffolding.
- Ability categories: Electromaster, Teleport, Accelerator/Vector
  Manipulation, Meltdowner, and Level 0.
- Implemented skill classes (38 registered classes):
  - Electromaster: Arc Generate, Current Charging, Magnetic Movement, Magnet
    Manipulation, Mine Detect, Body Intensify, Thunder Bolt, Railgun, and
    Thunder Clap.
  - Teleport: Self Teleport, Dimensional Folding, Penetrate Teleport, Mark
    Teleport, Flesh Ripping, Location Teleport, Shift Teleport, Space
    Fluctuation, and Flashing.
  - Accelerator: Vector Reflection, Bloodflow Reverse, Storm Wing, Plasma
    Generation, Kinetic Energy Applied, Directed Strike, Vector Acceleration,
    Ground Shock, and Vector Deviation.
  - Meltdowner: Electron Bomb, Radiation Intensify, Scatter Bomb, Light Shield,
    Single High-Speed Electron Beam, Mine Ray Basic, Ray Barrage, Jet Engine,
    Mine Ray Expert, Mine Ray Luck, and Electron Missile.
- The compact skill adaptations use shared server-authoritative paths: the
  server verifies category and skill ownership, CP, and cooldown before
  applying effects and synchronizing proficiency.
- Current Charging and Magnetic Movement use a shared sustained-skill path with
  server-owned press/hold/release state and per-tick CP consumption. Mine
  Detect performs its ore scan on the server.
- Plasma Generation now has a functional plasma attack, and Kinetic Energy
  Applied is registered in the visible developer tree.
- All 38 category-specific skills from the 1.1.3 tree are represented. Port.4
  adds both Teleporter critical-hit passives, a persistent same-dimension
  Location Teleport waypoint, Radiation Intensify marks, sustained Mine Rays,
  Ray Barrage, and sustained Electron Missiles.
- The common instant and sustained packet types are explicitly registered.
  This fixes a port.2 runtime omission that blocked the new instant skills.
- All older skill packet handlers now verify server-side ownership. Additional
  fixes bound client-provided teleport/dash data, add CP/cooldown/proficiency
  handling, disable unlearned default Vector Reflection, and prevent Directed
  Strike from creating duplicate blocks.
- Restored original 1.1.3 icons for the added skills.
- Restored ten legacy component items with original registry names and art:
  Constraint Metal Ingot/Plate, Imag Silicon Wafer/Piece, Data and Calculation
  Chips, Brainwave Analyzer, Information Processor, Energy Unit, and Energy
  Converter. Modern JSON recipes form an obtainable survival chain; recipes
  that originally required missing machines or ores use transitional material
  substitutions.
- Ability categories are now initialized on both physical sides, fixing an
  empty server ability registry on dedicated servers.
- Corrected the existing Imagiphase Dowsing Rod recipe, which previously used
  an invalid comparator tag and produced a vanilla shield.
- Core preview content including the ability developer, wireless node, wind
  generator multiblock, omni crafting table, cat engine, cleaning robot,
  imagiphase materials/vegetation, data terminal, coin, and dowsing rod.
- Modern GUI, HUD, model, entity, particle, and effect-rendering foundations.
- JEI integration source and optional development integrations for Jade,
  Oculus, Embeddium, and Player Animator.

## Not yet equivalent to AcademyCraft 1.1.3

- The 38 category-specific skill slots are present, but many remain functional
  adaptations rather than exact LambdaLib2-era reproductions. Location
  Teleport has one same-dimension waypoint instead of the original named
  multi-location GUI and cross-dimensional group transfer. Electron Missile
  uses server-driven homing rays rather than the original orbiting entities.
- The original also creates Brain Course, Advanced Brain Course, and Mind
  Course nodes in each of its four trees (12 registrations). The modern
  curriculum system is not yet an equivalent replacement for those nodes.
- The new skill effects preserve the original roles and progression but do not
  yet reproduce every charge mechanic, passive effect, entity, animation,
  shader, or sound from the 1.12.2 implementation.
- Many original machines, electronics, crafting recipes, tutorials, terminal
  apps, world-generation features, compatibility layers, and configuration
  options have not been reimplemented.
- Visual/audio parity is incomplete.
- No automatic 1.12.2 world or player-data migration is provided.
- Multiplayer, dedicated-server, shader-mod, and modpack compatibility still
  require in-game regression testing.

## Verification record

- Confirmed the supplied JAR metadata reports AcademyCraft 1.1.3 and matches
  the authoritative 1.1.3 source tag for Minecraft 1.12.2.
- Confirmed this source targets Minecraft 1.20.1, Java 17, and Forge 47.3.0.
- Confirmed the Forge/common source layout contains 348 Java source files and
  the expected Forge `mods.toml`, access transformer, mixin configs, assets,
  and data resources.
- Parsed all JSON resources successfully and passed `git diff --check`.
- The Gradle compile was attempted, but the wrapper could not download Gradle
  8.12 because the preparation environment had no route to
  `services.gradle.org`. This means the source is statically checked but not
  compile-verified. Run the build command from README.md before installing the
  resulting JAR.

## Recommended next milestones

1. Establish clean client and dedicated-server smoke tests.
2. Add GameTests for registrations, multiblock formation, ability state, and
   packet round trips.
3. Replace the compact skill adaptations with high-fidelity mechanics one
   category at a time, and restore the 12 generic course nodes.
4. Replace the transitional component recipes as the original ores and Metal
   Former return; then port tutorial progression, terminal apps, remaining
   machines, and integrations.
5. Design an explicit data fixer only if 1.12.2 world migration is required.
