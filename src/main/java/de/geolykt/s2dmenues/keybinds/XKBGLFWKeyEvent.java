package de.geolykt.s2dmenues.keybinds;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * A {@link GLFWKeyEvent} where the {@link #getKeyFriendlyName()} and {@link #getKeyInternalName()}
 * methods are wired through libXKBcommon. Only supported on X11 and Wayland, as per {@link GLFW#glfwGetPlatform()}.
 */
public class XKBGLFWKeyEvent extends GLFWKeyEvent {

    public XKBGLFWKeyEvent(@NotNull KeyEventType type, int key, int scancode, int mods) {
        super(type, key, scancode, mods);
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
}
