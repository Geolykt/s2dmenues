package de.geolykt.s2dmenues;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.ray3k.tenpatch.TenPatchDrawable;

import de.geolykt.starloader.api.resource.DataFolderProvider;

public class TextureCache {

    private static final int @NotNull[] WINDOW_STRETCH_REGIONS = {
            12,
            19
    };

    private static final int @NotNull[] GRADIENT_WINDOW_STRETCH_REGIONS = {
            24,
            39
    };

    @Nullable
    private static TextureCache instance;

    @NotNull
    public final Texture windowTexture;
    @NotNull
    public final TextureRegion window0;
    @NotNull
    public final TextureRegion window1;
    @NotNull
    public final TextureRegion window2;
    @NotNull
    public final Texture windowGradientTexture;
    @NotNull
    public final TextureRegion gradientThin;
    @NotNull
    public final TextureRegion gradientThick;

    public TextureCache() {
        this.windowTexture = new Texture(this.getFileHandleFor("window.png"));
        this.window0 = new TextureRegion(this.windowTexture, 0, 0, 32, 32);
        this.window1 = new TextureRegion(this.windowTexture, 32, 0, 32, 32);
        this.window2 = new TextureRegion(this.windowTexture, 64, 0, 32, 32);
        this.windowGradientTexture = new Texture(this.getFileHandleFor("window_gradient.png"));
        this.gradientThick = new TextureRegion(this.windowGradientTexture, 0, 0, 0.5F, 1F);
        this.gradientThin = new TextureRegion(this.windowGradientTexture, 0.5F, 0, 1F, 1F);
    }

    @NotNull
    public static TextureCache getInstance() {
        TextureCache instance = TextureCache.instance;
        if (instance == null) {
            TextureCache.instance = instance = new TextureCache();
        }
        return instance;
    }

    @NotNull
    private FileHandle getFileHandleFor(@NotNull String path) {
        Path file = DataFolderProvider.getProvider().provideAsPath().resolve("mods/s2dmenues").resolve(path);
        if (Files.notExists(file)) {
            return new ClassloaderBoundFilehandle(path, S2DMenues.class.getClassLoader());
        } else {
            return new FileHandle(file.toFile());
        }
    }

    public void dispose() {
        this.windowTexture.dispose();
        this.windowGradientTexture.dispose();
        TextureCache.instance = null;
    }

    @NotNull
    public static TenPatchDrawable createWindowTenpatch(@NotNull TextureRegion region) {
        return new TenPatchDrawable(WINDOW_STRETCH_REGIONS, WINDOW_STRETCH_REGIONS, true, region);
    }

    public TenPatchDrawable getGradientWindowTenpatch(boolean thick) {
        return new TenPatchDrawable(GRADIENT_WINDOW_STRETCH_REGIONS, GRADIENT_WINDOW_STRETCH_REGIONS, true, thick ? this.gradientThick : this.gradientThin);
    }

    public TenPatchDrawable getGradientWindowTenpatch() {
        return this.getGradientWindowTenpatch(false);
    }

    public TenPatchDrawable getGradientWindowTenpatch(boolean thick, Color color, float scale) {
        TenPatchDrawable tenpatch = this.getGradientWindowTenpatch(thick);
        tenpatch.setColor(color);
        tenpatch.setScale(scale);
        return tenpatch;
    }
}
