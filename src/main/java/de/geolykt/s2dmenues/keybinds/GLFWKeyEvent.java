package de.geolykt.s2dmenues.keybinds;

import java.util.Objects;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * A key event that was triggered due to a GLFW callback (i.e. {@link GLFW#nglfwSetKeyCallback(long, long)})
 * registered by s2dmenues. Note that the GLFW callbacks supplement other key event listening methods, such as
 * libGDX input handling or {@link WaylandKeyEvent} (if {@link GLFW#glfwGetPlatform()} is {@link GLFW#GLFW_PLATFORM_WAYLAND}).
 *
 * <p>The {@link #getScancode()} method of this class (or rather, any {@link AbstractKeyEvent} class - though here it is
 * the most noticeable) is system-specific. As such GLFW provides a system-agnostic way of representing keys using
 */
public class GLFWKeyEvent extends AbstractKeyEvent {
    private final int activeMods;
    private final int key;

    public GLFWKeyEvent(@NotNull KeyEventType type, int key, int scancode, int activeMods) {
        super(type, scancode);

        this.key = key;
        this.activeMods = activeMods;
    }

    /**
     * Obtain the currently depressed modifier keys associated with this key event.
     *
     * @return The modifier bitmask associated with this key press event.
     */
    public int getActiveModifiers() {
        return this.activeMods;
    }

    @Contract(pure = true)
    public int getGLFWKey() {
        return this.key;
    }

    @Override
    @NotNull
    @CheckReturnValue
    public String getKeyFriendlyName() {
        return Objects.requireNonNull(GLFW.glfwGetKeyName(-1, this.getScancode()));
    }

    @Override
    @NotNull
    @CheckReturnValue
    public String getKeyInternalName() {
        return Objects.requireNonNull(GLFW.glfwGetKeyName(-1, this.getScancode()));
    }

    @Override
    @NotNull
    @Contract(pure = true)
    public KeyEventSource getSource() {
        return KeyEventSource.WINDOW_LIBRARY;
    }
}
