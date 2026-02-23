package de.geolykt.s2dmenues.components.drawables;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;

import de.geolykt.starloader.api.gui.Drawing;

public class LAFAquaBackgroundDrawable extends BaseDrawable {

    private static final float FRAME_WIDTH_HALF = 4;
    private final float headerHeight;

    public LAFAquaBackgroundDrawable() {
        this(48.0F, 16.0F);
    }

    public LAFAquaBackgroundDrawable(float headerHeight, float fadeoutMargin) {
        if (headerHeight < 0) {
            throw new IllegalArgumentException("headerHeight value out of bounds: " + headerHeight);
        }

        this.headerHeight = headerHeight;
        this.setMinHeight(headerHeight);
        this.setPadding(fadeoutMargin, fadeoutMargin, fadeoutMargin, fadeoutMargin);
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        TextureRegion fillPixel = Drawing.getTextureProvider().getSinglePixelSquare();

        // Inner coordinates (i1 = Inner corner bottom left. Numbering is counterclockwise)
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

        final float hy = i2y - this.headerHeight;
        final float hyt = hy + LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float hyb = hy - LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;

        final float u1 = fillPixel.getU();
        final float u2 = fillPixel.getU2();
        final float v1 = fillPixel.getV();
        final float v2 = fillPixel.getV2();

        final float transparent = Color.toFloatBits(0F, 0F, 0F, 0F);
        final float aqua = Color.toFloatBits(81.0F / 256.0F, 207.0F / 256.0F, 230.0F / 256.0F, 0.8F);
        final float glass = Color.toFloatBits(81.0F / 256.0F, 207.0F / 256.0F, 230.0F / 256.0F, 0.4F);
        final float bright = Color.toFloatBits(0.2F, 0.2F, 0.2F, 1.0F);
        final float medium = Color.toFloatBits(0.15F, 0.15F, 0.15F, 1.0F);
        final float dark = Color.toFloatBits(0.1F, 0.1F, 0.1F, 1.0F);

        // Frame center ("middle") coordinates (inner coordinates are i1x/i1y to i4x/i4y)
        final float fm1x = i1x - LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fm1y = i1y - LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fm2x = fm1x;
        final float fm2y = i2y + LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fm3x = i3x + LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fm3y = fm2y;
        final float fm4x = fm3x;
        final float fm4y = fm1y;

        // Frame outer coordinates
        final float fo1x = fm1x - LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fo1y = fm1y - LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fo2x = fo1x;
        final float fo2y = fm2y + LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fo3x = fm3x + LAFAquaBackgroundDrawable.FRAME_WIDTH_HALF;
        final float fo3y = fo2y;
        final float fo4x = fo3x;
        final float fo4y = fo1y;

        batch.draw(fillPixel.getTexture(), new float[] {
            // Left edge (lower part)
            o1x, o1y, transparent, u1, v1,
            o2x, hy, transparent, u1, v2,
            i2x, hy, aqua, u2, v2,
            i1x, i1y, aqua, u2, v1,
            // Left edge (upper part)
            o2x, hy, transparent, u1, v1,
            o2x, o2y, transparent, u1, v2,
            i2x, i2y, glass, u2, v2,
            i2x, hy, glass, u2, v1,
            // Upper edge
            i2x, i2y, glass, u1, v1,
            o2x, o2y, transparent, u1, v2,
            o3x, o3y, transparent, u2, v2,
            i3x, i3y, glass, u2, v1,
            // Right edge (upper part)
            i3x, hy, glass, u1, v1,
            i3x, i3y, glass, u1, v2,
            o3x, o3y, transparent, u2, v2,
            o3x, hy, transparent, u2, v1,
            // Right edge (lower part)
            i4x, i4y, aqua, u1, v1,
            i4x, hy, aqua, u1, v2,
            o4x, hy, transparent, u2, v2,
            o4x, o4y, transparent, u2, v1,
            // Bottom edge
            o1x, o1y, transparent, u1, v1,
            i1x, i1y, aqua, u1, v2,
            i4x, i4y, aqua, u2, v2,
            o4x, o4y, transparent, u2, v1,
            // Content frame
            i1x, hy, glass, u1, v1,
            i2x, i2y, glass, u1, v2,
            i3x, i3y, glass, u2, v2,
            i4x, hy, glass, u2, v1,
            // Content body
            i1x, i1y, aqua, u1, v1,
            i2x, hy, aqua, u1, v2,
            i3x, hy, aqua, u2, v2,
            i4x, i4y, aqua, u2, v1,
            // Frame (inner part, left)
            fm1x, fm1y, medium, u1, v1,
            fm2x, fm2y, medium, u1, v2,
            i2x, i2y, medium, u2, v2,
            i1x, i1y, medium, u2, v1,
            // Frame inner top
            i2x, i2y, dark, u1, v1,
            fm2x, fm2y, dark, u1, v2,
            fm3x, fm3y, dark, u2, v2,
            i3x, i3y, dark, u2, v1,
            // Frame inner right
            i4x, i4y, bright, u1, v1,
            i3x, i3y, bright, u1, v2,
            fm3x, fm3y, bright, u2, v2,
            fm4x, fm4y, bright, u2, v1,
            // Frame inner bottom
            fm1x, fm1y, bright, u1, v1,
            i1x, i1y, bright, u1, v2,
            i4x, i4y, bright, u2, v2,
            fm4x, fm4y, bright, u2, v1,
            // Frame (outer part, left)
            fo1x, fo1y, bright, u1, v1,
            fo2x, fo2y, bright, u1, v2,
            fm2x, fm2y, bright, u2, v2,
            fm1x, fm1y, bright, u2, v1,
            // Frame outer top
            fm2x, fm2y, bright, u1, v1,
            fo2x, fo2y, bright, u1, v2,
            fo3x, fo3y, bright, u2, v2,
            fm3x, fm3y, bright, u2, v1,
            // Frame outer right
            fm4x, fm4y, medium, u1, v1,
            fm3x, fm3y, medium, u1, v2,
            fo3x, fo3y, medium, u2, v2,
            fo4x, fo4y, medium, u2, v1,
            // Frame outer bottom
            fo1x, fo1y, dark, u1, v1,
            fm1x, fm1y, dark, u1, v2,
            fm4x, fm4y, dark, u2, v2,
            fo4x, fo4y, dark, u2, v1,
            // Divider top
            i1x, hy, bright, u1, v1,
            i1x, hyt, bright, u1, v2,
            i3x, hyt, bright, u2, v2,
            i3x, hy, bright, u2, v1,
            // Divider bottom
            i1x, hyb, dark, u1, v1,
            i1x, hy, dark, u1, v2,
            i3x, hy, dark, u2, v2,
            i3x, hyb, dark, u2, v1
        }, 0, 360);
    }
}
