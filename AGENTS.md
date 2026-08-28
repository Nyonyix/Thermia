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
  ThermiaCommands.java          Engine-registered commands
  ServerConfig.java             Server-side config
  ClientConfig.java             Client-side config
  ai/behaviours/                Mob brain behaviours (seek comfort, stay at comfort)
  ai/memories/                  Brain memory modules
  ai/sensors/                   Sensory brain sensors (temperature, comfort decision)
  api/                          Public API for other mods
  client/                       Client setup, HUD widgets (ThermiaGui), F3 debug overlay
  client/renderer/              Cloth/thick/cape/wide-brim-hat entity renderers
  compat/jade/                  Jade/Hwyla entity temperature tooltip
  compat/create/                Create contraption integration
  compat/powergrid/             PowerGrid compat
  data/                         Damage types, tags, pelt loot modifier
  data/attachment/              Attachments (temperature, interior, humidity, thermometer)
  data/climate/                 Climate model records (ThermiaClimateModels)
  data/datagen/                 Data generation providers (damage/datamap/lang/loot/model/recipe/tags)
  data/datamap/                 Block/entity/fluid/item data map definitions
  data/manager/                 Core logic — temperature, interiors, inventory, humidity
  data/records/                 Records (Interior, BlockSearchResult, SolarShadeResult, etc.)
  effect/                       Mob effects (hyperthermia, hypothermia)
  item/                         Pelt and cloth/thick/cape/wide-brim-hat items & materials
  mixin/                        Mixins into TFC classes
  models/                       Entity models for cloth/thick/hat wearables
  server/                       Server-side event handler
  util/                         Block search, interior scanner, environment helpers
```

## Agent Behaviour

**NEVER modify code.** The user writes and builds all code themselves. The agent's role is:

- **Research** — browse TFC/NeoForge source, javadoc, and API surfaces to answer questions or confirm what a method actually does
- **Bounce ideas** — discuss design tradeoffs, suggest approaches, weigh options
- **Verify hunches** — check whether a proposed approach fits the existing code / Java quirks before the user commits to writing it
- **Explain** — clarify how existing systems work when asked
- **Sanity check** — when the user hands it code, review for bugs, logic errors, edge cases

Treat this like a collaborative code review, not a pair-programming session.
The user drives; the agent navigates.

Ask before reading more than 2–3 files at once outside of explicit code reviews.
Prefer targeted questions over broad exploration.

Tone: direct, concise, no fluff. No "Great question!" or "Certainly!" — just get into it.
Opinions welcome, but own them ("I think this is wrong because...") rather than
hedging endlessly.
