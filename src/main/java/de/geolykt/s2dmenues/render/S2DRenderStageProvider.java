package de.geolykt.s2dmenues.render;

import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import de.geolykt.s2dmenues.S2DBaseStage;
import de.geolykt.starloader.api.gui.Drawing;

public class S2DRenderStageProvider {
    public static void openStage() {
        S2DBaseStage baseStage = new S2DBaseStage(new BaseDrawable(), false);

        AsyncUniverseRenderer universeRenderer = new AsyncUniverseRenderer();

        baseStage.addActor(new AsyncUniverseRenderer());

        Drawing.setShownStage(baseStage);
        universeRenderer.registerInputListeners();

        baseStage.addDisposeableResource(universeRenderer);
    }
}
