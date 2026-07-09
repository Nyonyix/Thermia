package com.nyonyix.thermia.item.cloth;

public enum ThermiaClothWearableMaterial
{
    BURLAP(0, false),
    WOOLEN(0xA06540, true),
    SILK(0xA06540, true);

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
