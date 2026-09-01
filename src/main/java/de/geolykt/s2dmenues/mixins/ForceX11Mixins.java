package de.geolykt.s2dmenues.mixins;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.example.Main;

@Mixin(value = Main.class)
public class ForceX11Mixins {

    @Inject(target = @Desc(value = "main", args = String[].class), at = @At("HEAD"))
    private static void s2dmenues$forceX11(CallbackInfo ci) {
        if (Boolean.getBoolean("de.geolykt.s2dmenues.preferX11")) {
            // Renderdoc requires X11, so if the user requests it there needs to be an option to prefer X11 where available.
            GLFW.glfwInitHint(GLFW.GLFW_PLATFORM, GLFW.GLFW_PLATFORM_X11);
        } else if (!Boolean.getBoolean("de.geolykt.s2dmenues.noDisableLibdecor")) {
            // lwjgl-harfbuzz is incompatible with libdecor (only an issue on Wayland).
            GLFW.glfwInitHint(GLFW.GLFW_WAYLAND_LIBDECOR, GLFW.GLFW_WAYLAND_DISABLE_LIBDECOR);
        }
    }
}
