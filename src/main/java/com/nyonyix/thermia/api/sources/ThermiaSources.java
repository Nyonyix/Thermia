package com.nyonyix.thermia.api.sources;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Registry of external temperature contributions.
 *
 * <p>A source supplies a raw value in degrees Celsius. The category it is
 * registered into decides its phase, default modifier, and total cap.</p>
 *
 * <p>Registration is valid only during mod construction. The registry freezes
 * at server start; the categories never reload.</p>
 */

public final class ThermiaSources
{
    /**
     * Ambient Air, Added before effective temp calcs
     */
    public static final SourceCategory ENVIRONMENT_BASE = new SourceCategory("environment_base", SourcePhase.BASE, ctx -> 1f, 75f);

    /**
     * Radiant heat and cold, Default modifier = {@code 1 - radationProtection}
     */
    public static final SourceCategory RADIANT = new SourceCategory("radiant", SourcePhase.FELT, ctx -> 1f - ctx.radiationProtection(), 75f);

    /**
     * Contact heat or cold, default modifier = {@code 1 - conductionProtection}
     */
    public static final SourceCategory CONDUCTION = new SourceCategory("contact", SourcePhase.FELT, ctx -> 1 - ctx.conductionProtection(), 75f);

    /**
     * Entity heat or cold, Eg a burning zombie. Default modifier = {@code 1 - radiationProtection}
     */
    public static final SourceCategory ENTITY = new SourceCategory("entity", SourcePhase.FELT, ctx -> 1 - ctx.radiationProtection(), 75f);

    /**
     * Temperatures felt when submerged
     */
    public static final SourceCategory IMMERSION = new SourceCategory("immersion", SourcePhase.FELT, ctx -> 1, 75f);

    /**
     * Heat Carried by items the entity holds or wears, default modifier = {@code 1 - conductionProtection}
     */
    public static final SourceCategory INVENTORY = new SourceCategory("inventory", SourcePhase.FELT, ctx -> 1 - ctx.conductionProtection(), 75f);

    /**
     * Body temperature sources, Movement, fever, etc. (Currently Unused)
     */
    public static final SourceCategory INTERNAL = new SourceCategory("internal", SourcePhase.INTERNAL, ctx -> 1f, 75f);

    // End of sources

    public static final List<SourceCategory> ALL = List.of(ENVIRONMENT_BASE, RADIANT, CONDUCTION, ENTITY,IMMERSION, INVENTORY, INTERNAL);


    private static final Map<SourcePhase, List<SourceCategory>> BY_PHASE;

    static
    {
        Map<SourcePhase, List<SourceCategory>> byPhase = new EnumMap<>(SourcePhase.class);

        for (SourcePhase phase : SourcePhase.values())
        {
            List<SourceCategory> categories = new ArrayList<>();

            for (SourceCategory category : ALL)
            {
                if (category.phase() == phase) categories.add(category);
            }

            byPhase.put(phase, List.copyOf(categories));
        }

        BY_PHASE = Collections.unmodifiableMap(byPhase);
    }

    private ThermiaSources() {}

    /**
     * @param raw Sum pre modifiers per category capped
     * @param applied Sum after modifiers per category capped
     */
    public record Totals(float raw, float applied, List<Contribution> breakdown) {}
    public record Contribution(ResourceLocation id, String category, float raw, float applied) {}

    /**
     * Sums every source belonging to {@code phase} once. Iteration order ius registration order must never affect results
     *
     * @param collectBreakdown populate {@link Totals#breakdown()} for debug
     */

    public static Totals resolve(SourceContext ctx, SourcePhase phase, boolean collectBreakdown)
    {
        List<Contribution> breakdown = collectBreakdown ? new ArrayList<>() : null;

        float rawTotal = 0f;
        float appliedTotal = 0f;

        for (SourceCategory category : BY_PHASE.get(phase))
        {
            if (category.isEmpty()) continue;

            float cap = category.cap();
            float raw = 0f;
            float applied = 0f;

            for (Map.Entry<ResourceLocation, ThermiaSource> entry : category.sources().entrySet())
            {
                float value = entry.getValue().value().get(ctx);
                if (!Float.isFinite(value))
                {
                    continue;
                }

                float modifier = entry.getValue().modifier() != null ? entry.getValue().modifier() : category.defaultModifier().get(ctx);
                float contribution = value * modifier;

                raw += value;
                applied += contribution;

                if (collectBreakdown)
                {
                    breakdown.add(new Contribution(entry.getKey(), category.name(), value, contribution));
                }
            }

            if (Math.abs(applied) > cap) applied = Math.copySign(cap, applied);
            if (Math.abs(raw) > cap) raw = Math.copySign(cap, raw);

            rawTotal += raw;
            appliedTotal += applied;
        }

        return new Totals(rawTotal, appliedTotal, breakdown == null ? List.of() : List.copyOf(breakdown));
    }

    /**
     * Prevents registration after server star
     */

    public static void freeze()
    {
        ALL.forEach(SourceCategory::freeze);
    }
}