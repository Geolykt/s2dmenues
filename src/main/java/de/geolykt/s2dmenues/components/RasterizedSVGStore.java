package de.geolykt.s2dmenues.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import de.geolykt.s2dmenues.S2DMenues;
import de.geolykt.s2dmenues.render.NSVGRasterizer;
import de.geolykt.s2dmenues.render.NSVGRasterizer.NSVGImage;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackedEvent;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackingEvent;

public class RasterizedSVGStore {

    @NotNull
    public static final NamespacedKey CONFIGURE_ICON = NamespacedKey.fromString(S2DMenues.MOD_ID, "aur/icons/configure");

    private static boolean rasterized;

    /**
     * {@link TextureAtlas}'s {@link TextureAtlas#findRegion(String)} is a linear search, which
     * is very obvious not OK. So we use a bog standard {@link HashMap} here.
     */
    @NotNull
    private static Map<@NotNull NamespacedKey, @NotNull TextureRegion> regions = new HashMap<>();

    /**
     * The <code>ZZZ_</code> prefix is only there due to sorting order to not mess up class initialization order should I have accidentally reformatted
     * this file.
     */
    @NotNull
    private static final NamespacedKey @NotNull [] ZZZ_ALL = {
        RasterizedSVGStore.CONFIGURE_ICON
    };

    @NotNull
    public static TextureRegion getRegion(@NotNull NamespacedKey key) {
        if (!RasterizedSVGStore.rasterized) {
            throw new IllegalStateException("Not yet rasterized");
        }

        return Objects.requireNonNull(RasterizedSVGStore.regions.get(key), () -> "No regions associated with key: " + key);
    }

    public static void onAtlasNotification(@NotNull AtlasPackedEvent evt) {
        if (!RasterizedSVGStore.rasterized) {
            throw new IllegalStateException("Not yet rasterized, yet this method was called");
        } else if (RasterizedSVGStore.regions.size() != 0) {
            throw new IllegalStateException("Rasterized regions not empty, this implies that this method was called twice.");
        }

        for (NamespacedKey key : RasterizedSVGStore.ZZZ_ALL) {
            RasterizedSVGStore.regions.put(key, Objects.requireNonNull(evt.getAtlas().findRegion(key.toString()), () -> "Cannot find region for key: " + key.toString()));
        }
    }

    public static void rasterize(@NotNull AtlasPackingEvent event) {
        if (RasterizedSVGStore.rasterized) {
            throw new IllegalStateException("Already rasterized");
        }

        Logger logger = LoggerFactory.getLogger(RasterizedSVGStore.class);
        logger.debug("Rasterizing vector graphics");

        try (NSVGRasterizer rasterizer = new NSVGRasterizer()) {
            for (NamespacedKey key : RasterizedSVGStore.ZZZ_ALL) {
                if (!key.getNamespace().equals(S2DMenues.MOD_ID)) {
                    throw new AssertionError("Hey no freeloading you oaf");
                }

                logger.info("Rasterizing vector graphic: {}", key);

                String svgContent = S2DMenues.readStringFromResources(key.getKey() + ".svg");

                try (NSVGImage image = rasterizer.createImage(svgContent)) {
                    int w = Math.max(32, Math.min(256, (int) image.getWidth()));
                    int h = Math.max(32, Math.min(256, (int) image.getHeight()));

                    event.packTexture(key.toString(), w, h, image.createBlitter());
                }
            }
        }

        RasterizedSVGStore.rasterized = true;
    }
}
