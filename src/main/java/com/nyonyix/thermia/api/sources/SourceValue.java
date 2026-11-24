package com.nyonyix.thermia.api.sources;


/**
 * Supplies a raw contribution in degrees Celsius. Obtain the context from the
 * argument; never capture state from registration time.
 */

@FunctionalInterface
public interface SourceValue
{
    float get(SourceContext ctx);
}
