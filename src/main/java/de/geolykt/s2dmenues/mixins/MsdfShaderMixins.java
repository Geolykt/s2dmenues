package de.geolykt.s2dmenues.mixins;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Desc;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.maltaisn.msdfgdx.MsdfShader;

import de.geolykt.s2dmenues.ClassloaderBoundFilehandle;

@Mixin(MsdfShader.class)
public class MsdfShaderMixins {
    @ModifyArg(allow = 1, expect = 1, require = 1, index = 0, target = @Desc("<init>"), at = @At(value = "INVOKE", desc = @Desc(owner = ShaderProgram.class, value = "<init>", args = {FileHandle.class, FileHandle.class})))
    private static FileHandle s2dmenues$replaceVertexShader(@NotNull FileHandle vertexShader) {
        return new ClassloaderBoundFilehandle("font.vert", MsdfShaderMixins.class.getClassLoader());
    }

    @ModifyArg(allow = 1, expect = 1, require = 1, index = 1, target = @Desc("<init>"), at = @At(value = "INVOKE", desc = @Desc(owner = ShaderProgram.class, value = "<init>", args = {FileHandle.class, FileHandle.class})))
    private static FileHandle s2dmenues$replaceFragmentShader(@NotNull FileHandle vertexShader) {
        return new ClassloaderBoundFilehandle("font.frag", MsdfShaderMixins.class.getClassLoader());
    }
}
