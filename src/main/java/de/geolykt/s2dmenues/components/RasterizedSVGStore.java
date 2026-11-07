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
import de.geolykt.s2dmenues.render.plutovg.PlutoSVGDocument;
import de.geolykt.s2dmenues.render.plutovg.PlutoVGFFI;
import de.geolykt.s2dmenues.render.plutovg.PlutoVGSurface;
import de.geolykt.s2dmenues.render.tosvg.TOSVGRootObject;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackedEvent;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackingEvent;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackingEvent.DirectPixmapBlittingFunction;

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
        RasterizedSVGStore.CONFIGURE_ICON,
        NamespacedKey.fromString(S2DMenues.MOD_ID, "aur/icons/configure_bu")
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

        RasterizedSVGStore.checkAvailable();

        Logger logger = LoggerFactory.getLogger(RasterizedSVGStore.class);
        logger.debug("Rasterizing vector graphics");

        for (NamespacedKey key : RasterizedSVGStore.ZZZ_ALL) {
            if (!key.getNamespace().equals(S2DMenues.MOD_ID)) {
                throw new AssertionError("Hey no freeloading you oaf");
            }

            logger.info("Rasterizing vector graphic: {}", key);

            String svgContent = S2DMenues.readStringFromResources(key.getKey() + ".svg");

            try (PlutoSVGDocument document = new PlutoSVGDocument(svgContent, 64F, 64F);
                    PlutoVGSurface surface = document.render(64, 64)) {
                TOSVGRootObject svg = TOSVGRootObject.parse(svgContent);
                event.packTexture(key.toString(), surface.getWidth(), surface.getHeight(), (DirectPixmapBlittingFunction) svg::renderTo);
//                event.packTexture(key.toString(), surface.getWidth(), surface.getHeight(), surface.asBlittingFunction());
            }
        }

        RasterizedSVGStore.rasterized = true;
    }

    public static void checkAvailable() throws UnsupportedOperationException {
        try {
            Class.forName(PlutoVGFFI.class.getName(), true, PlutoVGFFI.class.getClassLoader());
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            throw new UnsupportedOperationException("Missing native libraries: SVG Rasterization is not available, ensure that the plutovg and plutosvg libraries are installed from your system package manager.", e);
        }
    }

    public static boolean isAvailable() {
        try {
            Class.forName(PlutoVGFFI.class.getName(), true, PlutoVGFFI.class.getClassLoader());
            return true;
        } catch (NoClassDefFoundError | ClassNotFoundException expected) {
            return false;
        }
    }
}
