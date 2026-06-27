package com.nyonyix.thermia.item;

public enum ThermiaCapeAnimal
{
    POLAR_BEAR(true),
    GRIZZLY_BEAR(true),
    BLACK_BEAR(true),
    COUGAR(false),
    LION(false),
    SABERTOOTH(true),
    TIGER(false),
    CROCODILE(true),
    WOLF(true),
    DIREWOLF(true),
    COW(false),
    YAK(true),
    ALPACA(false),
    SHEEP(true),
    MUSK_OX(true),
    PANDA(true),
    GOAT(false),
    BISON(true);

    private final boolean hasCloak;

    ThermiaCapeAnimal(boolean hasCloak)
    {
        this.hasCloak = hasCloak;
    }

    public boolean hasCloak()
    {
        return hasCloak;
    }
}
