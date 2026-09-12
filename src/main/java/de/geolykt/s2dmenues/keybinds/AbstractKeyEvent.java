package de.geolykt.s2dmenues.keybinds;

import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.InputProcessor;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import de.geolykt.starloader.api.event.Event;

/**
 * An {@link AbstractKeyEvent} is an {@link Event} fired by s2dmenues whenever a keyboard's key is {@link KeyEventType#PRESS pressed},
 * {@link KeyEventType#RELEASE released}, or {@link KeyEventType#REPEAT repeated}.
 *
 * <p>Instances of this class come directly from the window manager or GLFW, skipping libGDX. As such, the
 * {@link #getScancode()} method is heavily system dependent. On Linux and FreeBSD systems, where evdev is the input driver,
 * evdev keycodes are to be expected. That being said, other systems <b>will</b> use other keycodes.
 * Note that although {@link #getScancode()} contains 'scancode' in its name, the values returned by that method
 * do not describe the scancodes received by the operating system from the input device.
 *
 * <p>This event and it's subclasses do not replace or alter libGDX input processing and are safe to use alongside libGDX's
 * {@link InputProcessor} class, however they may trigger repeated key events.
 */
@NonExtendable
public abstract class AbstractKeyEvent extends Event {

    private final int scancode;
    @NotNull
    private final KeyEventType type;

    @Contract(pure = true)
    public AbstractKeyEvent(@NotNull KeyEventType type, int scancode) {
        this.type = Objects.requireNonNull(type);
        this.scancode = scancode;
    }

    /**
     * Returns the name of the key behind this event in the current keyboard configuration.
     *
     * <p>When the key represents a printable symbol (such as Latin uppercase L),
     * the symbol is returned. The returned symbol is valid for the current keyboard state (that is, it takes into
     * account the state of the modifier keys of the keyboard, thus producing '&#64;' instead of 'q' on a
     * QWERTZ keyboard when alt_gr is pressed).
     *
     * <p>This event should not be used for text input handling. This is especially true for this method,
     * as it may not take into account other methods of handling input (e.g. Kanji input).
     *
     * <p>In some environments (e.g. GLFW), this method ignores active keyboard modifiers.
     * This should only be the case if there is no way to go forward in that regard. Regardless,
     * this is one other reason to not use this method for text input handling.
     *
     * @return A user-friendly name for this key, falling back to the internal name if unavailable.
     */
    @NotNull
    public abstract String getKeyFriendlyName();

    /**
     * Returns the internal name of the key behind this event in the current keyboard configuration.
     *
     * <p>This event should not be used for text input handling. This is especially true for this method,
     * as it may not take into account other methods of handling input (e.g. Kanji input).
     *
     * <p>This method is the same as {@link #getKeyFriendlyName()} except that it does not do the key -&gt;
     * character conversion, unless the environment does not provide a way to do so (GLFW
     * doesn't provide such a method, and thus the two methods return the exact same thing, AND are also
     * blind with). This is especially
     * required to differ between MINUS and NP_MINUS. However, this might be not exactly user-friendly for
     * , so care is to be taken even .
     *
     * @return The name for the key of this event, as exposed by the current platform.
     */
    @NotNull
    public abstract String getKeyInternalName();

    /**
     * Returns the scancode of the key of this event.
     *
     * <p>Please be aware that the scancodes used come from GLFW or the Wayland compositor (depending on the subclass
     * of this event).
     *
     * @return The scancode/keycode associated with this event.
     */
    @Contract(pure = true)
    public int getScancode() {
        return this.scancode;
    }

    /**
     * Returns the {@link KeyEventSource} which describes what caused this {@link AbstractKeyEvent}
     * to fire.
     *
     * <p>Different {@link AbstractKeyEvent} instances with the same {@link KeyEventSource} value
     * should be interpreted as different key presses/releases/repetitions. If the {@link KeyEventSource}
     * differs, that guarantee cannot be established.
     *
     * @return The source where this event originates from.
     */
    @NotNull
    public abstract KeyEventSource getSource();

    /**
     * Get the type of key action of this event.
     *
     * <p>Simply, returns whether this is a key press, key release, or key repeat event.
     *
     * @return The {@link KeyEventType} associated with this event
     */
    @NotNull
    @Contract(pure = true)
    public KeyEventType getType() {
        return this.type;
    }
}
