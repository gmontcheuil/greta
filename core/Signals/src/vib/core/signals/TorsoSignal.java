/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.signals;

/**
 * This class contains informations about torso Signals.
 *
 * @author Andre-Marie Pez
 * @author Quoc Anh Le
 * @author Brice Donval
 */
public class TorsoSignal extends SpineSignal {
    
    public boolean shoulder = false;

    public TorsoSignal(String id) {
        super(id);
    }

    @Override
    public String getModality() {
        return "torso";
    }
    
    public boolean getShoulder() {
        return shoulder;
    }
}
