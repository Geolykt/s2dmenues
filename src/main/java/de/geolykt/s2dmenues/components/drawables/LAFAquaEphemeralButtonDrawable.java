package de.geolykt.s2dmenues.components.drawables;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import de.geolykt.starloader.api.gui.Drawing;

public class LAFAquaEphemeralButtonDrawable extends BaseDrawable {
    public static final byte DRAW_EDGE_EAST = 2;
    public static final byte DRAW_EDGE_NORTH = 1;
    public static final byte DRAW_EDGE_SOUTH = 4;
    public static final byte DRAW_EDGE_WEST = 8;
    private static final float MEDIUM = Color.toFloatBits(0.15F, 0.15F, 0.15F, 1.0F);
    private final byte enabledVertices;
    private final float fillColor;

    public LAFAquaEphemeralButtonDrawable(byte enabledVertices) {
        this(4.0F, enabledVertices);
    }

    public LAFAquaEphemeralButtonDrawable(float borderWidth, byte enabledVertices) {
        this(borderWidth, Color.toFloatBits(81.0F / 256.0F, 207.0F / 256.0F, 230.0F / 256.0F, 0.8F), enabledVertices);
    }

    public LAFAquaEphemeralButtonDrawable(float borderWidth, float fillColor, byte enabledVertices) {
        this.fillColor = fillColor;
        this.enabledVertices = enabledVertices;

        float padNorth = (enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_NORTH) == 0 ? 0 : borderWidth;
        float padEast = (enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_EAST) == 0 ? 0 : borderWidth;
        float padSouth = (enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_SOUTH) == 0 ? 0 : borderWidth;
        float padWest = (enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_WEST) == 0 ? 0 : borderWidth;

        this.setPadding(padNorth, padWest, padSouth, padEast);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        TextureRegion fillPixel = Drawing.getTextureProvider().getSinglePixelSquare();

        // Inner coordinates (w = west, n = north, s = south, e = east)
        final float iwx = x + this.getLeftWidth();
        final float isy = y + this.getBottomHeight();
        final float iny = y + height - this.getTopHeight();
        final float iex = x + width - this.getRightWidth();

        // Outer coordinates
        final float owx = x;
        final float osy = y;
        final float ony = y + height;
        final float oex = x + width;

        final float u1 = fillPixel.getU();
        final float u2 = fillPixel.getU2();
        final float v1 = fillPixel.getV();
        final float v2 = fillPixel.getV2();

        final float aqua = this.fillColor;
        final float medium = LAFAquaEphemeralButtonDrawable.MEDIUM;

        if ((this.enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_NORTH) != 0) {
            batch.draw(fillPixel.getTexture(), new float[] {
                // Upper edge
                owx, iny, medium, u1, v1,
                owx, ony, medium, u1, v2,
                oex, ony, medium, u2, v2,
                oex, iny, medium, u2, v1
            }, 0, 20);
        }

        if ((this.enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_EAST) != 0) {
            batch.draw(fillPixel.getTexture(), new float[] {
                // Right edge
                iex, osy, medium, u1, v1,
                iex, ony, medium, u1, v2,
                oex, ony, medium, u2, v2,
                oex, osy, medium, u2, v1
           }, 0, 20);
        }

        if ((this.enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_SOUTH) != 0) {
            batch.draw(fillPixel.getTexture(), new float[] {
                // Bottom edge
                owx, osy, medium, u1, v1,
                owx, isy, medium, u1, v2,
                oex, isy, medium, u2, v2,
                oex, osy, medium, u2, v1
           }, 0, 20);
        }

        if ((this.enabledVertices & LAFAquaEphemeralButtonDrawable.DRAW_EDGE_WEST) != 0) {
            batch.draw(fillPixel.getTexture(), new float[] {
                // Left edge
                owx, osy, medium, u1, v1,
                owx, ony, medium, u1, v2,
                iwx, ony, medium, u2, v2,
                iwx, osy, medium, u2, v1
           }, 0, 20);
        }

        batch.draw(fillPixel.getTexture(), new float[] {
            // Content body
            iwx, isy, aqua, u1, v1,
            iwx, iny, aqua, u1, v2,
            iex, iny, aqua, u2, v2,
            iex, isy, aqua, u2, v1
        }, 0, 20);
    }

    @NotNull
    @Contract(pure = false, mutates = "this", value = "_ -> this")
    public LAFAquaEphemeralButtonDrawable withMinWidth(float width) {
        this.setMinHeight(width);
        return this;
    }
}
