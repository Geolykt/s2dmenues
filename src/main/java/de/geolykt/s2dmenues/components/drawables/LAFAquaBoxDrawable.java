package de.geolykt.s2dmenues.components.drawables;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import de.geolykt.starloader.api.gui.Drawing;

public class LAFAquaBoxDrawable extends BaseDrawable {
    private static final float BRIGHT = Color.toFloatBits(0.2F, 0.2F, 0.2F, 1.0F);
    private static final float DARK = Color.toFloatBits(0.1F, 0.1F, 0.1F, 1.0F);
    private static final float FRAME_WIDTH_HALF = 4;
    private static final float MEDIUM = Color.toFloatBits(0.15F, 0.15F, 0.15F, 1.0F);
    private static final float TRANSPARENT = Color.toFloatBits(0F, 0F, 0F, 0F);

    private final float fillColor;

    public LAFAquaBoxDrawable() {
        this(16.0F);
    }

    public LAFAquaBoxDrawable(float fadeoutMargin) {
        this(fadeoutMargin, Color.toFloatBits(81.0F / 256.0F, 207.0F / 256.0F, 230.0F / 256.0F, 0.8F));
    }

    public LAFAquaBoxDrawable(float fadeoutMargin, float fillColor) {
        this.fillColor = fillColor;

        this.setPadding(fadeoutMargin, fadeoutMargin, fadeoutMargin, fadeoutMargin);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        TextureRegion fillPixel = Drawing.getTextureProvider().getSinglePixelSquare();

        // Inner coordinates (i1 = Inner corner bottom left. Numbering is clockwise)
        final float i1x = x + this.getLeftWidth();
        final float i1y = y + this.getBottomHeight();
        final float i2x = i1x;
        final float i2y = y + height - this.getTopHeight();
        final float i3x = x + width - this.getRightWidth();
        final float i3y = i2y;
        final float i4x = i3x;
        final float i4y = i1y;

        // Outer coordinates
        final float o1x = x;
        final float o1y = y;
        final float o2x = o1x;
        final float o2y = y + height;
        final float o3x = x + width;
        final float o3y = o2y;
        final float o4x = o3x;
        final float o4y = o1y;

        final float u1 = fillPixel.getU();
        final float u2 = fillPixel.getU2();
        final float v1 = fillPixel.getV();
        final float v2 = fillPixel.getV2();

        final float aqua = this.fillColor;

        // Frame center ("middle") coordinates (inner coordinates are i1x/i1y to i4x/i4y)
        final float fm1x = i1x - LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fm1y = i1y - LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fm2x = fm1x;
        final float fm2y = i2y + LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fm3x = i3x + LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fm3y = fm2y;
        final float fm4x = fm3x;
        final float fm4y = fm1y;

        // Frame outer coordinates
        final float fo1x = fm1x - LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fo1y = fm1y - LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fo2x = fo1x;
        final float fo2y = fm2y + LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fo3x = fm3x + LAFAquaBoxDrawable.FRAME_WIDTH_HALF;
        final float fo3y = fo2y;
        final float fo4x = fo3x;
        final float fo4y = fo1y;

        batch.draw(fillPixel.getTexture(), new float[] {
            // Left edge
            o1x, o1y, LAFAquaBoxDrawable.TRANSPARENT, u1, v1,
            o2x, i2y, LAFAquaBoxDrawable.TRANSPARENT, u1, v2,
            i2x, i2y, aqua, u2, v2,
            i1x, i1y, aqua, u2, v1,
            // Upper edge
            i2x, i2y, aqua, u1, v1,
            o2x, o2y, LAFAquaBoxDrawable.TRANSPARENT, u1, v2,
            o3x, o3y, LAFAquaBoxDrawable.TRANSPARENT, u2, v2,
            i3x, i3y, aqua, u2, v1,
            // Right edge
            i4x, i4y, aqua, u1, v1,
            i3x, i3y, aqua, u1, v2,
            o3x, o3y, LAFAquaBoxDrawable.TRANSPARENT, u2, v2,
            o4x, o4y, LAFAquaBoxDrawable.TRANSPARENT, u2, v1,
            // Bottom edge
            o1x, o1y, LAFAquaBoxDrawable.TRANSPARENT, u1, v1,
            i1x, i1y, aqua, u1, v2,
            i4x, i4y, aqua, u2, v2,
            o4x, o4y, LAFAquaBoxDrawable.TRANSPARENT, u2, v1,
            // Content body
            i1x, i1y, aqua, u1, v1,
            i2x, i2y, aqua, u1, v2,
            i3x, i3y, aqua, u2, v2,
            i4x, i4y, aqua, u2, v1,
            // Frame (inner part, left)
            fm1x, fm1y, LAFAquaBoxDrawable.MEDIUM, u1, v1,
            fm2x, fm2y, LAFAquaBoxDrawable.MEDIUM, u1, v2,
            i2x, i2y, LAFAquaBoxDrawable.MEDIUM, u2, v2,
            i1x, i1y, LAFAquaBoxDrawable.MEDIUM, u2, v1,
            // Frame inner top
            i2x, i2y, LAFAquaBoxDrawable.DARK, u1, v1,
            fm2x, fm2y, LAFAquaBoxDrawable.DARK, u1, v2,
            fm3x, fm3y, LAFAquaBoxDrawable.DARK, u2, v2,
            i3x, i3y, LAFAquaBoxDrawable.DARK, u2, v1,
            // Frame inner right
            i4x, i4y, LAFAquaBoxDrawable.BRIGHT, u1, v1,
            i3x, i3y, LAFAquaBoxDrawable.BRIGHT, u1, v2,
            fm3x, fm3y, LAFAquaBoxDrawable.BRIGHT, u2, v2,
            fm4x, fm4y, LAFAquaBoxDrawable.BRIGHT, u2, v1,
            // Frame inner bottom
            fm1x, fm1y, LAFAquaBoxDrawable.BRIGHT, u1, v1,
            i1x, i1y, LAFAquaBoxDrawable.BRIGHT, u1, v2,
            i4x, i4y, LAFAquaBoxDrawable.BRIGHT, u2, v2,
            fm4x, fm4y, LAFAquaBoxDrawable.BRIGHT, u2, v1,
            // Frame (outer part, left)
            fo1x, fo1y, LAFAquaBoxDrawable.BRIGHT, u1, v1,
            fo2x, fo2y, LAFAquaBoxDrawable.BRIGHT, u1, v2,
            fm2x, fm2y, LAFAquaBoxDrawable.BRIGHT, u2, v2,
            fm1x, fm1y, LAFAquaBoxDrawable.BRIGHT, u2, v1,
            // Frame outer top
            fm2x, fm2y, LAFAquaBoxDrawable.BRIGHT, u1, v1,
            fo2x, fo2y, LAFAquaBoxDrawable.BRIGHT, u1, v2,
            fo3x, fo3y, LAFAquaBoxDrawable.BRIGHT, u2, v2,
            fm3x, fm3y, LAFAquaBoxDrawable.BRIGHT, u2, v1,
            // Frame outer right
            fm4x, fm4y, LAFAquaBoxDrawable.MEDIUM, u1, v1,
            fm3x, fm3y, LAFAquaBoxDrawable.MEDIUM, u1, v2,
            fo3x, fo3y, LAFAquaBoxDrawable.MEDIUM, u2, v2,
            fo4x, fo4y, LAFAquaBoxDrawable.MEDIUM, u2, v1,
            // Frame outer bottom
            fo1x, fo1y, LAFAquaBoxDrawable.DARK, u1, v1,
            fm1x, fm1y, LAFAquaBoxDrawable.DARK, u1, v2,
            fm4x, fm4y, LAFAquaBoxDrawable.DARK, u2, v2,
            fo4x, fo4y, LAFAquaBoxDrawable.DARK, u2, v1
        }, 0, 260);
    }

    @NotNull
    @Contract(pure = false, mutates = "this", value = "_ -> this")
    public LAFAquaBoxDrawable withMinWidth(float width) {
        this.setMinHeight(width);
        return this;
    }
}
