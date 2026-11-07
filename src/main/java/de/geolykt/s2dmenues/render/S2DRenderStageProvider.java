package de.geolykt.s2dmenues.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import de.geolykt.s2dmenues.S2DBaseStage;
import de.geolykt.s2dmenues.components.RasterizedSVGStore;
import de.geolykt.s2dmenues.components.drawables.FlatColorDrawable;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.gui.Drawing;

public class S2DRenderStageProvider {
    public static void openStage() {
        try {
            RasterizedSVGStore.checkAvailable();
        } catch (UnsupportedOperationException e) {
            Galimulator.panic("SVG Rasterization not available. This is usually user error and you probably forgot to install plutosvg (version 1.X) and plutovg (version 0.X), in ELF format, packaged as an *.so file.", false, e);
            Drawing.setShownStage(null);
            return;
        }

        S2DBaseStage baseStage = new S2DBaseStage(new BaseDrawable(), false);

        AsyncUniverseRenderer universeRenderer = new AsyncUniverseRenderer();

        baseStage.addActor(new AsyncUniverseRenderer());

        Table table = new Table();
        table.setBackground(new FlatColorDrawable(Color.YELLOW));

        Drawing.setShownStage(baseStage);
        universeRenderer.registerInputListeners();

        baseStage.addDisposeableResource(universeRenderer);
    }
}
