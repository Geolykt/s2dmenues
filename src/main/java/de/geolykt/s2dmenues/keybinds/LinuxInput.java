package de.geolykt.s2dmenues.keybinds;

import java.lang.foreign.Arena;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.stianloader.jxkbwayland.JXKBWayland;
import org.stianloader.jxkbwayland.WaylandKeyCallback;
import org.stianloader.jxkbwayland.JXKBWayland.Keysym;
import org.stianloader.jxkbwayland.UnsupportedEnvironmentException;
import org.stianloader.micromixin.transform.internal.util.Objects;

import de.geolykt.starloader.api.event.EventHandler;
import de.geolykt.starloader.api.event.EventManager;
import de.geolykt.starloader.api.event.EventPriority;
import de.geolykt.starloader.api.event.Listener;
import de.geolykt.starloader.api.event.lifecycle.GraphicalTickEvent;
import de.geolykt.starloader.api.event.lifecycle.GraphicalTickEvent.Phase;

public class LinuxInput {
    @Nullable
    private static JXKBWayland xkb;

    @NotNull
    public static String getFriendlyName(int evdevCode) {
        JXKBWayland xkb = LinuxInput.xkb;

        if (xkb == null) {
            throw new IllegalStateException("jXKBWayland is not initialized.");
        }

        try (Arena arena = Arena.ofConfined()) {
            String name = xkb.getKeyName(evdevCode, arena, Keysym.CHARACTER);

            if (name == null) {
                throw new IllegalStateException("jXKBWayland is not yet ready. Two frames are required for jXKBWayland to initialize.");
            }

            if (name.length() > 0 && !Character.isISOControl(name.codePointAt(0))) {
                return name;
            }

            name = xkb.getKeyName(evdevCode, arena, Keysym.NAME);

            if (name == null) {
                throw new AssertionError("jXKBWayland deinitialized itself?");
            }

            return name;
        }
    }

    @NotNull
    public static String getInternalName(int evdevCode) {
        JXKBWayland xkb = LinuxInput.xkb;

        if (xkb == null) {
            throw new IllegalStateException("jXKBWayland is not initialized.");
        }

        try (Arena arena = Arena.ofConfined()) {
            String name = xkb.getKeyName(evdevCode, arena, Keysym.NAME);

            if (name == null) {
                throw new IllegalStateException("jXKBWayland is not yet ready. Two frames are required for jXKBWayland to initialize.");
            }

            return name;
        }
    }

    public static boolean hasXKB() {
        return LinuxInput.xkb != null;
    }

    public static void initialize() {
        if (LinuxInput.xkb != null) {
            throw new IllegalStateException("Already initialized");
        } else if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND) {
            LinuxInput.xkb = new JXKBWayland();

            try {
                LinuxInput.xkb.setKeyCallback(new WaylandKeyCallback() {
                    @Override
                    public void onFocusGain(int[] pressedEvcodes) {
                        // Ignore, for now (TODO: Don't)
                    }

                    @Override
                    public void onFocusLoss() {
                        // Ignore, for now (TODO: Don't)
                    }

                    @Override
                    public void onPress(int evcode) {
                        EventManager.handleEvent(new WaylandKeyEvent(KeyEventType.PRESS, evcode));
                    }

                    @Override
                    public void onRelease(int evcode) {
                        EventManager.handleEvent(new WaylandKeyEvent(KeyEventType.RELEASE, evcode));
                    }

                    @Override
                    public void onRepeat(int evcode) {
                        EventManager.handleEvent(new WaylandKeyEvent(KeyEventType.REPEAT, evcode));
                    }
                });
            } catch (UnsupportedEnvironmentException e) {
                throw new AssertionError("JXKBWayland#setKeyCallback raised an UnsupportedEnvironmentException on Wayland. That shouldn't happen.", e);
            }
        } else if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11) {
            LinuxInput.xkb = new JXKBWayland();

            EventManager.registerListener(new Listener() {
                @EventHandler(EventPriority.HIGHEST)
                public void onTick(@NotNull GraphicalTickEvent evt) {
                    if (evt.getPhase() == Phase.PRE) {
                        // Refresh keyboard state and layout
                        Objects.requireNonNull(LinuxInput.xkb, "jXKBWayland deinitialized?").synchronize();
                    }
                }
            });
        } else {
            throw new UnsupportedOperationException("Invalid GLFW platform: " + GLFW.glfwGetPlatform() + ". Ensure that you are using wayland (preferred), or X11. Other desktops are not supported.");
        }
    }
}
