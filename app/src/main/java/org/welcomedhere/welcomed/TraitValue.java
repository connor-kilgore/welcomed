package org.welcomedhere.welcomed;

import java.io.Serializable;

public class TraitValue implements Serializable{

    int value;
    int traitID;
    String trait;

    public TraitValue(int value, int traitID, String trait)
    {
        this.value = value;
        this.traitID = traitID;
        this.trait = trait;
    }
}
