package de.geolykt.s2dmenues.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import de.geolykt.s2dmenues.S2DBaseStage;
import de.geolykt.s2dmenues.components.drawables.FlatColorDrawable;
import de.geolykt.starloader.api.gui.Drawing;

public class S2DRenderStageProvider {
    public static void openStage() {
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
