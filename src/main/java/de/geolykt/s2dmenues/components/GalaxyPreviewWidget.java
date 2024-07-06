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

import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.StarGenerator;
import snoddasmannen.galimulator.StarPath;

public class GalaxyPreviewWidget extends Widget {

    private static class StarPositionMeta {
        private static final Vector2 TMP = new Vector2();

        private final float maxX;
        private final float maxY;
        @Nullable
        private final StarPath path;
        private float pathAngle;
        private final float pathAngleVelocity;
        private final Vector2 rawPosition;

        public StarPositionMeta(snoddasmannen.galimulator.Star star, float maxX, float maxY) {
            this.rawPosition = star.getCoordinates();
            this.path = star.getPath();
            this.pathAngle = star.pathAngle;
            this.pathAngleVelocity = star.getPathAngleVelocity();
            this.maxX = maxX;
            this.maxY = maxY;
        }

        public Vector2 getCurrentPosition() {
            return StarPositionMeta.TMP.set(this.rawPosition).scl(0.5F / this.maxX, 0.5F / this.maxY).add(0.5F, 0.5F);
        }

        public void updatePosition() {
            StarPath path = this.path;
            if (path == null) {
                return;
            }
            path.a();
            this.pathAngle += this.pathAngleVelocity;
            this.rawPosition.set(path.b(this.pathAngle));
        }
    }

    private boolean dirty = false;
    @Nullable
    private MapData lastMap = null;
    @NotNull
    private final GenGalaxyWindow parent;
    @NotNull
    private final List<StarPositionMeta> positions = new ArrayList<>();

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

        if (this.lastMap != renderData || this.dirty) {
            this.lastMap = renderData;
            this.dirty = false;
            // FIXME sloppy code - could cause data inconsistency issues
            int starCount = this.parent.getGalaxySize();
            float starCountOld = Space.starCount;
            Space.starCount = starCount;
            generator.prepareGenerator();
            this.positions.clear();
            while (starCount-- != 0) {
                this.positions.add(new StarPositionMeta(generator.generateStar(), generator.getMaxX(), generator.getMaxY()));
            }
            Space.starCount = starCountOld;
        }

        Color c = Color.ORANGE.cpy();
        c.a = parentAlpha;
        batch.setColor(c);
        for (StarPositionMeta posMeta : this.positions) {
            for (int i = 0; i < 8; i++) {
                posMeta.updatePosition();
            }
            Vector2 pos = posMeta.getCurrentPosition();
            batch.draw(region, this.getX() + (pos.x * this.getWidth() - 4), this.getY() + (pos.y * this.getHeight() - 4), 8, 8);
        }
    }

    @Override
    public float getMinHeight() {
        return 30F;
    }

    @Override
    public float getMinWidth() {
        return 30F;
    }

    @Override
    public float getPrefHeight() {
        return 300F;
    }

    @Override
    public float getPrefWidth() {
        return 300F;
    }

    public void reset() {
        this.dirty = true;
    }
}
