package org.welcomedhere.welcomed;

import java.io.Serializable;

public class Descriptor implements Serializable {
    String descriptor;
    String descriptorType;

    public Descriptor(String descriptor, String descriptorType)
    {
        this.descriptor = descriptor;
        this.descriptorType = descriptorType;
    }
}