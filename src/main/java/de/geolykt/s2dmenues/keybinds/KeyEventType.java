package de.geolykt.s2dmenues.keybinds;

public enum KeyEventType {
    PRESS,
    RELEASE,

    /**
     * Denotes that the key was repeated, that is the key is being held down for a prolonged time.
     *
     * <p>This type of event is optional and is fired at the behest of the window manager.
     * How often a key repeat event is fired, and if it is fired at all depends on the configuration
     * of the window manager.
     */
    REPEAT;
}
