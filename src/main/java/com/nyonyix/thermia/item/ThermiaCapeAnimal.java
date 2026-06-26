package com.nyonyix.thermia.item;

public enum ThermiaCapeAnimal
{
    LEOPARD_SEAL(true),
    POLAR_BEAR(true),
    GRIZZLY_BEAR(true),
    BLACK_BEAR(true),
    COUGAR(false),
    PANTHER(false),
    LION(false),
    SABERTOOTH(true),
    TIGER(false),
    CROCODILE(true),
    WOLF(true),
    HYENA(false),
    DIREWOLF(true),
    PIG(false),
    COW(false),
    YAK(true),
    ALPACA(false),
    SHEEP(true),
    MUSK_OX(true),
    FOX(false),
    PANDA(true),
    OCELOT(false),
    DEER(false),
    CARIBOU(false),
    BONGO(false),
    GAZELLE(false),
    BOAR(false),
    MOOSE(true),
    WILDEBEEST(true),
    BISON(true),
    DONKEY(false),
    MULE(false),
    HORSE(true);

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
