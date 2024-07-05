package de.geolykt.s2dmenues.components;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.registry.MetadatableRegistry.MetadataEntry;

import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.StarGenerator;

public class GalaxyPreviewWidget extends Widget {

    @NotNull
    private final GenGalaxyWindow parent;
    @NotNull
    private final List<Vector2> positions = new ArrayList<>();
    @Nullable
    private MapData lastMap = null;

    public GalaxyPreviewWidget(@NotNull GenGalaxyWindow parent) {
        this.parent = parent;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        TextureRegion region = Drawing.getTextureProvider().getSinglePixelSquare();

        MapData renderData = this.parent.getMapdata();
        StarGenerator generator = renderData.getGenerator();
        if (!generator.allowPreview()) {
            Color c = Color.RED.cpy();
            c.a = parentAlpha;
            batch.setColor(c);
            batch.draw(region, this.getX(), this.getY(), this.getWidth(), this.getHeight());
            return;
        } else {
            Color c = Color.BLUE.cpy();
            c.a = parentAlpha;
            batch.setColor(c);
            batch.draw(region, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }

        if (this.lastMap != renderData) {
            this.lastMap = renderData;
            // FIXME sloppy code - could cause data inconsistency issues
            int starCount = this.parent.getGalaxySize();
            float starCountOld = Space.starCount;
            Space.starCount = starCount;
            generator.prepareGenerator();
            this.positions.clear();
            while (starCount-- != 0) {
                snoddasmannen.galimulator.Star s = generator.generateStar();
                this.positions.add(s.getCoordinates().scl(0.5F / generator.getMaxX(), 0.5F / generator.getMaxY()).add(0.5F, 0.5F));
            }
            Space.starCount = starCountOld;
        }

        Color c = Color.ORANGE.cpy();
        c.a = parentAlpha;
        batch.setColor(c);
        for (Vector2 pos : this.positions) {
            batch.draw(region, this.getX() + (pos.x * this.getWidth() - 4), this.getY() + (pos.y * this.getHeight() - 4), 8, 8);
        }
    }

    @Override
    public float getPrefHeight() {
        return 300F;
    }

    @Override
    public float getPrefWidth() {
        return 300F;
    }

    @Override
    public float getMinHeight() {
        return 30F;
    }

    @Override
    public float getMinWidth() {
        return 30F;
    }
}
