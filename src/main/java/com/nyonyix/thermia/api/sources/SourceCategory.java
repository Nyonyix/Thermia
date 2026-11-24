package com.nyonyix.thermia.api.sources;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A named group of sources sharing a phase, a default modifier, and a total cap.
 *
 * <p>Instances are created only by {@link ThermiaSources}. Register into them
 * during mod construction; they freeze at server start and never reload.</p>
 */

public final class SourceCategory
{
    private final String name;
    private final SourcePhase phase;
    private final SourceValue defaultModifier;
    private final float cap;
    private final Map<ResourceLocation, ThermiaSource> sources = new LinkedHashMap<>();
    private volatile boolean frozen;

    SourceValue defaultModifier()
    {
        return defaultModifier;
    }

    float cap()
    {
        return cap;
    }

    Map<ResourceLocation, ThermiaSource> sources()
    {
        return sources;
    }

    void freeze()
    {
        frozen = true;
    }

    @Override
    public String toString()
    {
        return name;
    }

    SourceCategory(String name, SourcePhase phase, SourceValue defaultModifier, float cap)
    {
        this.name = Objects.requireNonNull(name, "name");
        this.phase = Objects.requireNonNull(phase, "phase");
        this.defaultModifier = Objects.requireNonNull(defaultModifier, "defaultModifier");

        if (!Float.isFinite(cap) || cap < 0f)
        {
            throw new IllegalArgumentException("cap must be finite and non negative");
        }

        this.cap = cap;
    }

    public String name()
    {
        return name;
    }

    public SourcePhase phase()
    {
        return phase;
    }

    public boolean isFrozen()
    {
        return frozen;
    }

    public int size()
    {
        return sources.size();
    }

    public boolean isEmpty()
    {
        return sources.isEmpty();
    }

    /**
     * Unmodifiable snapshot view for debug display. Do not call this in the temperature pass
     */

    public Map<ResourceLocation, ThermiaSource> view()
    {
        return Collections.unmodifiableMap(sources);
    }

    /**
     * Registers a source using this categories default modifier
     */

    public void register(ResourceLocation id, SourceValue value)
    {
        register(id, value, null);
    }

    /**
     * Registers a source with an explicit modifier. Call during mod construction
     *
     * @param modifier {@code null} uses the category default, {@code 1.0} bypasses it
     */

    public void register(ResourceLocation id, SourceValue value, Float modifier)
    {
        Objects.requireNonNull(id, "id");

        if (frozen)
        {
            throw new IllegalStateException("Cannot register temperature source '" + id + "' into '" + name + "'after freeze");
        }

        ThermiaSource source = new ThermiaSource(value, modifier);

        if (sources.putIfAbsent(id, source) != null)
        {
            throw new IllegalStateException("Duplicate temperature source '" + id + "' in '" + name + "'");
        }
    }
}
