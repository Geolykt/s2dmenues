package de.geolykt.s2dmenues.keybinds;

/**
 * An abstract enum describing the type from where a key event was triggered from.
 *
 * <p>Different {@link AbstractKeyEvent} instances with the same {@link KeyEventSource} value
 * should be interpreted as different key presses/releases/repetitions. If the {@link KeyEventSource}
 * differs, that guarantee cannot be established.
 */
public enum KeyEventSource {
    /**
     * The event was triggered from the window library, such as GLFW or SDL (not yet supported).
     */
    WINDOW_LIBRARY,

    /**
     * The event was triggered from the compositor, such as X11 (probably won't happen due to how
     * GLFW works) or Wayland (under KWin, plasma-keyboard would be responsible for firing the events,
     * provided jXKBWayland is being used to listen to the events).
     */
    COMPOSITOR,

    /**
     * The event was sourced from the raw input driver, such as evdev on most Linux distributions.
     *
     * <p>Not used by s2dmenues at this point, but reserved for forwards compatibility reasons.
     */
    DRIVER;
}
