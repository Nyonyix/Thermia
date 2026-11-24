package com.nyonyix.thermia.api.sources;

import java.util.Objects;

/**
 * A registered contribution.
 *
 * @param value raw contribution in degrees Celsius
 * @param modifier {@code null} uses the category default, {@code 1.0} bypasses it,
 * {@code 0.0} blocks it entirely
 */

public record ThermiaSource(SourceValue value, Float modifier)
{
    public ThermiaSource
    {
        Objects.requireNonNull(value, "value");

        if (modifier != null && !Float.isFinite(modifier))
        {
            throw new IllegalArgumentException("modifier must be finite when present");
        }
    }
}
