package com.nyonyix.thermia.item.cloth;

public enum ThermiaClothWearableMaterial
{
    BURLAP(0, false),
    WOOL(0xFFFFFFFF, true),
    SILK(0xFFF8EA65, true);

    private final int defaultColour;
    private final boolean dyeable;

    ThermiaClothWearableMaterial(int defaultColour, boolean dyeable)
    {
        this.defaultColour = defaultColour;
        this.dyeable = dyeable;
    }

    public int getDefaultColour()
    {
        return this.defaultColour;
    }

    public boolean isDyeable()
    {
        return this.dyeable;
    }
}
