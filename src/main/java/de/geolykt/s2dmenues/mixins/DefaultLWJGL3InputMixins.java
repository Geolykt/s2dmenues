package de.geolykt.s2dmenues.mixins;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.badlogic.gdx.backends.lwjgl3.DefaultLwjgl3Input;

import de.geolykt.s2dmenues.keybinds.GLFWInput;
import de.geolykt.s2dmenues.keybinds.LinuxInput;

@Mixin(DefaultLwjgl3Input.class)
public class DefaultLWJGL3InputMixins {

    @Unique
    private static boolean s2dmenues$hasInitializedXKB;

    @Unique
    @NotNull
    private static Arena s2dmenues$arena = Arena.ofAuto();

    @Inject(at = @At("HEAD"), target = @Desc(value = "windowHandleChanged", args = long.class), require = 1, expect = 1)
    private void s2dmenues$setupJXKBWayland(long windowHandle, @Nullable CallbackInfo ci) {
        if (DefaultLWJGL3InputMixins.s2dmenues$hasInitializedXKB) {
            return;
        }

        DefaultLWJGL3InputMixins.s2dmenues$hasInitializedXKB = true;

        if (GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_X11) {
            LoggerFactory.getLogger(DefaultLWJGL3InputMixins.class).warn("s2dmenues: X11 is not officially supported. Please use Wayland, keyboard-related bugs are of your own making. Using libkxbcommon as a partial fallback.");
            LinuxInput.initialize(); // No need to poll for events in this branch
            return;
        } else if (GLFW.glfwGetPlatform() != GLFW.GLFW_PLATFORM_WAYLAND) {
            LoggerFactory.getLogger(DefaultLWJGL3InputMixins.class).error("s2dmenues: Unsupported environment. Please use Wayland, keyboard-related bugs are of your own making.");
            // Nothing to initialize in this branch
            return;
        } else {
            // Wayland branch
            LinuxInput.initialize();
            // Force process wayland events (what could go wrong?) to ensure that JXKBWayland is initialized
            // - otherwise we would have two dead frames as we wait for libGDX to call this method twice eventually.
            GLFW.glfwPollEvents();
            GLFW.glfwPollEvents();
        }
    }

    @Inject(at = @At("TAIL"), target = @Desc(value = "windowHandleChanged", args = long.class), require = 1, expect = 1)
    private void s2dmenues$registerGLFWkeyCallbacks(long windowHandle, @Nullable CallbackInfo ci) {
        // Obtain previously set key callback handle
        long previousCallbackHandle = GLFW.nglfwSetKeyCallback(windowHandle, 0L);

        if (previousCallbackHandle == 0L) {
            throw new NullPointerException("nglfwSetKeyCallback returned NULL");
        }

        // Reset the handle, just in case we FUBAR anything between this line and us setting our own callback
        GLFW.nglfwSetKeyCallback(windowHandle, previousCallbackHandle);

        Linker linker = Linker.nativeLinker();
        MethodHandle callbackMethodHandle;

        try {
            callbackMethodHandle = MethodHandles.lookup().findStatic(GLFWInput.class, "glfwKeyCallback", MethodType.methodType(void.class, MemorySegment.class, int.class, int.class, int.class, int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to allocate upcall stub: Unable to find callback function in Java space", e);
        }

        FunctionDescriptor keyCallbackDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);

        MethodHandle previousCallbackDowncall = linker.downcallHandle(MemorySegment.ofAddress(previousCallbackHandle), keyCallbackDescriptor);

        // Ensure that the previous callback is also called
        MethodHandle cleanupHandle = MethodHandles.dropArguments(previousCallbackDowncall, 0, Throwable.class);
        callbackMethodHandle = MethodHandles.tryFinally(callbackMethodHandle, cleanupHandle);

        MemorySegment upcallStub = linker.upcallStub(callbackMethodHandle, keyCallbackDescriptor, DefaultLWJGL3InputMixins.s2dmenues$arena);

        GLFW.nglfwSetKeyCallback(windowHandle, upcallStub.address());
    }
}
