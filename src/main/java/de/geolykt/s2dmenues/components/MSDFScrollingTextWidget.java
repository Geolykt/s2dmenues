package de.geolykt.s2dmenues.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.maltaisn.msdfgdx.FontStyle;
import com.maltaisn.msdfgdx.MsdfFont;
import com.maltaisn.msdfgdx.MsdfShader;
import com.maltaisn.msdfgdx.widget.MsdfLabel;

import de.geolykt.s2dmenues.bridge.FontStyleMarkerInterface;

public class MSDFScrollingTextWidget extends Widget {

    private float horizontalScroll = 0F;
    @NotNull
    private MsdfFont msdfFont;
    @NotNull
    private FontStyle msdfFontStyle;
    @NotNull
    private final MsdfShader msdfShader;
    private float preferredWidth;
    @NotNull
    private List<MsdfLabel> runs = Collections.emptyList();
    @NotNull
    private String text = "";

    public MSDFScrollingTextWidget(@NotNull MsdfFont font, @NotNull FontStyle msdfFontStyle, @NotNull MsdfShader shader, @NotNull CharSequence text) {
        this.msdfFont = font;
        this.msdfFontStyle = msdfFontStyle;
        this.msdfShader = shader;
        this.setText(text);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch = Objects.requireNonNull(batch, "argument 'batch' may not be null.");
        super.draw(batch, parentAlpha);

        float relY = this.horizontalScroll;

//        batch.setColor(Color.DARK_GRAY.r, Color.DARK_GRAY.g, Color.DARK_GRAY.b, 1F * parentAlpha);
//        batch.draw(Drawing.getTextureProvider().getSinglePixelSquare(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
//        batch.setColor(Color.WHITE);

        for (MsdfLabel run : this.runs) {
            if (Align.isLeft(run.getLabelAlign())) {
                run.setX(this.getX());
            } else if (Align.isCenterHorizontal(run.getLabelAlign())) {
                run.setX(this.getX() + (this.getWidth() - run.getMinWidth()) / 2);
            } else if (Align.isRight(run.getLabelAlign())) {
                run.setX(this.getX() + this.getWidth() - run.getMinWidth());
            }
            run.setY(this.getY() + relY);

            run.getFontStyle().getColor().a = MathUtils.clamp(relY / this.getHeight(), 0F, 0.75F) * 1.25F;
            if (((FontStyleMarkerInterface) (Object) run.getFontStyle()).s2dmenues$useShadow()) {
                run.getFontStyle().getShadowColor().a = MathUtils.clamp(relY / this.getHeight(), 0F, 0.5F) * 0.8F;
            }
            if (((FontStyleMarkerInterface) (Object) run.getFontStyle()).s2dmenues$useInnerShadow()) {
                run.getFontStyle().getInnerShadowColor().a = MathUtils.clamp(relY / this.getHeight(), 0F, 0.5F) * 0.8F;
            }

            run.draw(batch, parentAlpha);
            relY -= run.getPrefHeight();
        }

        if (relY > this.getHeight() + 10) {
            this.horizontalScroll = -10;
        }

        this.horizontalScroll += 0.2F;
    }

    @Override
    public float getPrefWidth() {
        return this.preferredWidth;
    }

    @NotNull
    @Contract(pure = true)
    private MsdfLabel interpretLine(@NotNull String line) {
        Skin skin = new Skin();
        skin.add(this.msdfFontStyle.getFontName(), this.msdfFont);
        skin.add("default", this.msdfShader);

        int align = Align.left;
        FontStyle style = new FontStyle(this.msdfFontStyle);

        while (!line.isEmpty() && line.codePointAt(0) == '\\') {
            if (line.startsWith("\\rightjustify ")) {
                line = line.substring("\\rightjustify ".length());
                align = Align.right;
            } else if (line.startsWith("\\centerjustify ")) {
                line = line.substring("\\centerjustify ".length());
                align = Align.center;
            } else if (line.startsWith("\\shadowcolor=")) {
                int spaceidx = line.indexOf(' ');
                String shadowColorName = line.substring("\\shadowcolor=".length(), spaceidx);
                line = line.substring(spaceidx + 1);
                Color shadowColor = Colors.get(shadowColorName);
                if (shadowColor == null) {
                    line = "Error: Unknown color: '" + shadowColorName + "'; " + line;
                } else {
                    style.setShadowClipped(true);
                    style.setShadowOffset(new Vector2(1F, 1F));
                    style.setShadowColor(new Color(shadowColor));
                    ((FontStyleMarkerInterface) (Object) style).s2dmenues$useShadow(true);
                    style.setInnerShadowColor(new Color(Color.RED));
                    ((FontStyleMarkerInterface) (Object) style).s2dmenues$useInnerShadow(true);
                }
            } else if (line.startsWith("\\fontsize=")) {
                int spaceidx = line.indexOf(' ');
                String numberString = line.substring("\\fontsize=".length(), spaceidx);
                line = line.substring(spaceidx + 1);
                try {
                    style.setSize(this.msdfFontStyle.getSize() * Float.parseFloat(numberString));
                } catch (NumberFormatException nfe) {
                    line = "Error: Unknown number: '" + numberString + "'; " + line;
                }
            } else {
                break;
            }
        }

        if (line.isEmpty()) {
            line = " ";
        }

        MsdfLabel label = new MsdfLabel(line, skin, style);
        label.setAlignment(align, Align.left);

        return label;
    }

    @NotNull
    @Contract(mutates = "this", value = "null -> fail; !null -> this")
    public MSDFScrollingTextWidget setText(@NotNull CharSequence text) {
        this.text = Objects.requireNonNull(Objects.requireNonNull(text, "argument 'text' may not be null.").toString(), "text.toString() yielded null");
        float minWidth = 0F;
        int lineStart = 0;
        int lineEnd;

        List<MsdfLabel> runs = new ArrayList<>();
        while ((lineEnd = this.text.indexOf('\n', lineStart)) >= 0) {
            MsdfLabel run = this.interpretLine(this.text.substring(lineStart, lineEnd));
            runs.add(run);
            minWidth = Math.max(minWidth, run.getMinWidth());
            lineStart = lineEnd + 1;
        }

        if (this.text.length() > lineStart) {
            runs.add(this.interpretLine(this.text.substring(lineStart)));
        }

        this.runs = Collections.unmodifiableList(runs);
        this.preferredWidth = minWidth;
        this.invalidateHierarchy();

        return this;
    }
}
