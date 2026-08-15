# AcademyCraft — Forge 1.20.1 port preview

This is a **source port preview**, not a feature-complete conversion of
AcademyCraft 1.1.3. It is based on the last Forge 1.20.1 commit in the
AcademyCraft-Reborn history and has been trimmed to the Forge/common modules.

Read [PORT_STATUS.md](PORT_STATUS.md) before installing it in a world.

## Target

- Minecraft 1.20.1 only
- Forge 47.3.0 or newer in the Forge 47 line
- Java 17
- Mod id: `academy`
- Port version: `1.1.3-port.4`

## Port.4 additions

- Added the remaining five Teleporter entries: Dimensional Folding, Mark
  Teleport, Flesh Ripping, Location Teleport, and Space Fluctuation.
- Added the remaining six Meltdowner entries: Radiation Intensify, Mine Ray
  Basic, Ray Barrage, Mine Ray Expert, Mine Ray Luck, and Electron Missile.
- All 38 category-specific skills registered by AcademyCraft 1.1.3 are now
  represented in the modern developer trees with their original icons and
  dependency layout.
- Dimensional Folding and Space Fluctuation provide the original three tiers
  of Teleporter critical hits. Radiation Intensify marks targets and amplifies
  subsequent Meltdowner attacks.
- The three Mine Rays share a sustained, server-controlled breaking path with
  range, hardness progress, harvest-tier checks, CP drain, cooldowns, and the
  Fortune III behavior of Mine Ray Luck.
- Location Teleport currently provides one persistent same-dimension waypoint;
  sneak-use replaces it. The original multi-location GUI and cross-dimensional
  transfer remain future fidelity work.

## Port.3 additions

- Restored Current Charging, Magnetic Movement, and Mine Detect, bringing the
  registered category-skill count to 27.
- Added a shared press/hold/release server path for sustained skills, including
  ownership, CP drain, cooldown, and proficiency handling.
- Made Plasma Generation functional and exposed Kinetic Energy Applied in the
  developer tree.
- Registered the shared instant/sustained packets centrally; this corrects a
  port.2 omission that prevented the new instant-skill keybinds from reaching
  the server.
- Hardened every older skill packet handler with server-side ownership checks.
  Teleport/dash inputs are bounded, one-shot skills consume CP and use
  cooldowns, Vector Reflection no longer defaults on for unskilled players,
  and Directed Strike no longer duplicates blocks.

Current Charging targets Academy energy-capable blocks, Magnetic Movement
currently anchors to metallic blocks rather than entities, and Mine Detect
uses a particle reveal rather than the original through-wall overlay.

## Port.2 additions

- Twelve more registered skill implementations across Electromaster,
  Teleport, Meltdowner, and Accelerator, using the original 1.1.3 names and
  icons.
- Shared server-authoritative activation checks for the new instant skills,
  including learned-skill/category checks, CP costs, cooldowns, and
  proficiency gain.
- Ten restored 1.1.3 crafting components with original art and a usable
  survival recipe chain.
- Dedicated-server category registration and the broken dowsing-rod recipe
  were corrected.

The added skills are functional 1.20.1 adaptations, not exact simulations of
the original LambdaLib2 context/rendering implementations.

## Build

```bash
./gradlew --no-daemon :forge:build
```

The distributable JAR is written under `forge/build/output/`. The Gradle
wrapper downloads Gradle and Forge dependencies on the first build, so an
Internet connection is required.

Optional development integrations such as JEI, Jade, Oculus, and Embeddium are
resolved by the development configuration; players should install only the
mods they actually want to use.

## Safety

Treat this as alpha software. Back up a world before testing it, and do not
expect a 1.12.2 world containing AcademyCraft blocks/entities to migrate
automatically. Registry names, block entities, networking, rendering, and
saved player ability data changed substantially between Minecraft 1.12.2 and
1.20.1.

## License and attribution

The source is distributed under GPL-3.0. See [LICENSE](LICENSE),
[UPSTREAM.md](UPSTREAM.md), and
[ORIGINAL_LICENSE_NOTICE.md](ORIGINAL_LICENSE_NOTICE.md).
