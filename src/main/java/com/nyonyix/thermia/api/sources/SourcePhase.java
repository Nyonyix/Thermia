package com.nyonyix.thermia.api.sources;

/**
 * Where in the temperature pipeline a category is used
 *
 * <p>{@link #BASE} is added to the air temperature <em>before</em> the effective temperature blend.
 * {@link #FELT} is added after it, Directly to felt temperature. {@link #INTERNAL} is added to body temperature and never
 * reaches felt temperature.</p>
 */

public enum SourcePhase
{
    BASE,
    FELT,
    INTERNAL
}
