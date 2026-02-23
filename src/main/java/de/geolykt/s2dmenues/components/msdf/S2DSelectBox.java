package de.geolykt.s2dmenues.components.msdf;

import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles.ListStyle;
import com.github.tommyettinger.textra.Styles.SelectBoxStyle;
import com.github.tommyettinger.textra.TextraSelectBox;

import de.geolykt.s2dmenues.FontConfig;
import de.geolykt.s2dmenues.Styles;
import de.geolykt.s2dmenues.components.drawables.LAFAquaBoxDrawable;
import de.geolykt.starloader.api.gui.Drawing;

public class S2DSelectBox extends TextraSelectBox {
    public static class BasicSelectionDrawable extends BaseDrawable {
        @Override
        public void draw(Batch batch, float x, float y, float width, float height) {
            TextureRegion fillPixel = Drawing.getTextureProvider().getSinglePixelSquare();
            float redFloatBits = Color.RED.toFloatBits();

            final float u1 = fillPixel.getU();
            final float u2 = fillPixel.getU2();
            final float v1 = fillPixel.getV();
            final float v2 = fillPixel.getV2();

            batch.draw(fillPixel.getTexture(), new float[] {
                x, y, redFloatBits, u1, v1,
                x, y + height, redFloatBits, u1, v2,
                x + width, y + height, redFloatBits, u2, v2,
                x + width, y, redFloatBits, u2, v1,
            }, 0, 12);

//            LoggerFactory.getLogger(S2DDialog.class).warn("{}#{} - {}x{}", x, y, width, height);
        }
    }

    @NotNull
    private static SelectBoxStyle getDefaultStyle() {
        ListStyle listStyle = new ListStyle();
        listStyle.fontColorSelected = Color.GRAY;
        listStyle.fontColorUnselected = Color.WHITE;
        listStyle.selection = new BasicSelectionDrawable();
        // 157, 68, 47
        listStyle.background = new LAFAquaBoxDrawable(16F, Color.toFloatBits(90, 45, 33/*157, 68, 47*/, 255));
        SelectBoxStyle selectorStyle = new SelectBoxStyle();
        selectorStyle.fontColor = Color.WHITE;
        selectorStyle.overFontColor = Color.GRAY;
        selectorStyle.disabledFontColor = Color.RED;
        selectorStyle.listStyle = listStyle;
        selectorStyle.scrollStyle = Styles.getInstance().scrollPaneStyle;
        return selectorStyle;
    }

    @NotNull
    private Font lastFont = this.getFont0();

    public S2DSelectBox() {
        this(S2DSelectBox.getDefaultStyle());
    }

    public S2DSelectBox(@NotNull SelectBoxStyle style) {
        super(style);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        this.getFont(); // Invalidate layout if font changed
        super.draw(batch, parentAlpha);
    }

    @NotNull
    public Font getFont() {
        if (this.lastFont != (this.lastFont = this.getFont0())) {
            this.invalidate();
        }

        return this.lastFont;
    }

    @NotNull
    @OverrideOnly
    protected Font getFont0() {
        return FontConfig.getInstance().getPreferredFont();
    }

    @Override
    public void layout() {
        this.getStyle().font = this.getFont0();
        super.layout();
    }
}
