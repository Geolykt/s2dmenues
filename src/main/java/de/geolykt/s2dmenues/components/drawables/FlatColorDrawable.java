package de.geolykt.s2dmenues.components.drawables;

import org.jspecify.annotations.NonNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import de.geolykt.starloader.api.gui.Drawing;

public class FlatColorDrawable extends BaseDrawable {

    private final Color color;

    public FlatColorDrawable(@NonNull Color color) {
        this.color = color;
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        batch.setColor(this.color);
        batch.draw(Drawing.getTextureProvider().getSinglePixelSquare(), x, y, width, height);
    }
}
