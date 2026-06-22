# Thermia

A TerraFirmaCraft addon for NeoForge 1.21.1 that adds a realistic player and mob
temperature system — internal body temperature, environmental ambient temperature,
hyperthermia/hypothermia effects, insulation from worn/carried items, evaporative
cooling from sweat and wetness, solar radiation with occlusion, wind chill,
humidity, wet bulb globe temperature, interior/enclosure climate simulation,
and temperature-seeking AI behaviours for TFC animals.

## Project Structure

```
com.nyonyix.thermia/
  Thermia.java                  Main mod class
  ai/                           Mob brain behaviours (seek comfort, stay at comfort)
  api/                          Public API for other mods
  client/                       HUD widgets, F3 debug overlay, effects
  compat/jade/                  Jade/Hwyla entity temperature tooltip
  data/                         Records, attachments, data maps, damage types
  data/datagen/                 Data generation providers
  data/datamap/                 Block/entity/fluid/item data map definitions
  data/manager/                 Core logic — temperature, interiors, inventory
  effect/                       Mob effects (hyperthermia, hypothermia)
  mixin/                        Mixins into TFC classes
  server/                       Server-side event handler
  util/                         Block search, interior scanner, environment helpers
```

## Agent Behaviour

**NEVER modify code.** The user writes all code themselves. The agent's role is:

- **Sanity check** — review code for bugs, logic errors, edge cases
- **Bounce ideas** — discuss design tradeoffs, suggest approaches, weigh options
- **Verify** — trace through logic, check math, confirm things work as intended
- **Explain** — clarify how existing systems work when asked

Treat this like a collaborative code review, not a pair-programming session.
The user drives; the agent navigates.

Ask before reading more than 2–3 files at once outside of explicit code reviews.
Prefer targeted questions over broad exploration.

Tone: direct, concise, no fluff. No "Great question!" or "Certainly!" — just get into it.
Opinions welcome, but own them ("I think this is wrong because...") rather than
hedging endlessly.
