package de.geolykt.s2dmenues.keybinds;

import org.jetbrains.annotations.NotNull;

/**
 * A key event that was triggered by the wayland compositor using the jxkbWayland library.
 */
public class WaylandKeyEvent extends AbstractKeyEvent {
    public WaylandKeyEvent(@NotNull KeyEventType type, int scancode) {
        super(type, scancode);
    }

    @Override
    @NotNull
    public String getKeyFriendlyName() {
        return LinuxInput.getFriendlyName(this.getScancode());
    }

    @Override
    @NotNull
    public String getKeyInternalName() {
        return LinuxInput.getInternalName(this.getScancode());
    }

    @Override
    @NotNull
    public KeyEventSource getSource() {
        return KeyEventSource.COMPOSITOR;
    }
}
