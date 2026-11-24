/**
 * Temperature sources for other mods.
 *
 * <p>There are two things you can register here. If you want to say something
 * about a block's temperature, use {@link BlockTemperatures}. If you want to
 * add heat or cold to an entity wherever it happens to be, register a source
 * into one of the categories on {@link ThermiaSources}.</p>
 *
 * <p>The difference is whether the value belongs to a position or to an entity.
 * Block temperatures go through Thermia's block scan, so they get distance
 * falloff and exposure checks. Entity sources don't, they're just added to what
 * the entity feels.</p>
 *
 * <p>Values are Celsius, same scale as the block temperature data map. Give the
 * raw number and let the category do the rest: RADIANT multiplies by
 * (1 - radiationProtection), CONDUCTION by (1 - conductionProtection), the
 * others pass straight through. Multiply by protection yourself and it gets
 * applied twice.</p>
 *
 * <p>NaN means "nothing to say" and gets skipped. That's also how a fallback
 * provider declines so something more specific can answer.</p>
 *
 * <p>Register during common setup, not client setup. Both registries freeze
 * when the server starts and throw if you're late. That's on purpose, a source
 * that silently never runs is harder to track down than a crash.</p>
 *
 * <p>Not everything is wired up yet. Radiant, conduction, immersion and
 * inventory all feed the felt temperature. ENTITY and ENVIRONMENT_BASE are
 * empty placeholders, and INTERNAL isn't consumed by anything, so registering
 * into those currently does nothing.</p>
 */
package com.nyonyix.thermia.api.sources;