package de.geolykt.s2dmenues.components;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles.LabelStyle;
import com.github.tommyettinger.textra.TextraLabel;

public class MSDFScrollingTextWidget extends Widget {

    private float horizontalScroll = 0F;
    @NotNull
    private LabelStyle baseRunLabelStyle;
    private float preferredWidth;
    @NotNull
    private List<TextraLabel> runs = Collections.emptyList();
    @NotNull
    private String text = "";

    private long deltaTime = 0;

    public MSDFScrollingTextWidget(@NotNull LabelStyle baseRunLabelStyle, @NotNull CharSequence text) {
        this.baseRunLabelStyle = Objects.requireNonNull(baseRunLabelStyle, "'baseRunLabelStyle' may not be null");
        this.setText(text);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch = Objects.requireNonNull(batch, "argument 'batch' may not be null.");
        super.draw(batch, parentAlpha);

        float relY = this.horizontalScroll;

        for (TextraLabel run : this.runs) {
            int align = run.getAlignment();

            if (Align.isLeft(align)) {
                run.setX(this.getX());
            } else if (Align.isCenterHorizontal(align)) {
                run.setX(this.getX() + (this.getWidth() - run.getMinWidth()) / 2);
            } else if (Align.isRight(align)) {
                run.setX(this.getX() + this.getWidth() - run.getMinWidth());
            }

            run.setY(this.getY() + relY);

            run.style.fontColor.a = MathUtils.clamp(relY / this.getHeight(), 0F, 0.5F) * 2F;
            run.style.fontColor.a = ((int) Math.round(run.style.fontColor.a * 8F)) / 8F;

            run.draw(batch, parentAlpha);
            relY -= run.getPrefHeight();
        }

        if (relY > this.getHeight() + 10) {
            this.horizontalScroll = -10;
        }

        if (this.deltaTime == 0) {
            this.deltaTime = System.nanoTime();
        } else {
            long currentTime = System.nanoTime();
            this.horizontalScroll += (currentTime - this.deltaTime) * 20e-9;
            this.deltaTime = currentTime;
        }
    }

    @Override
    public float getPrefWidth() {
        return this.preferredWidth;
    }

    @NotNull
    @Contract(pure = true)
    private TextraLabel interpretLine(@NotNull String line) {
        int align = Align.left;
        LabelStyle runStyle = new LabelStyle(this.baseRunLabelStyle);
        runStyle.font = new Font(runStyle.font);

        while (!line.isEmpty() && line.codePointAt(0) == '\\') {
            if (line.startsWith("\\rightjustify ")) {
                line = line.substring("\\rightjustify ".length());
                align = Align.right;
            } else if (line.startsWith("\\centerjustify ")) {
                line = line.substring("\\centerjustify ".length());
                align = Align.center;
            } else if (line.startsWith("\\fontsize=")) {
                int spaceidx = line.indexOf(' ');
                String numberString = line.substring("\\fontsize=".length(), spaceidx);
                line = line.substring(spaceidx + 1);

                try {
                    runStyle.font.scale(Float.parseFloat(numberString));
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

        TextraLabel label = new TextraLabel(line, runStyle);
        label.setAlignment(align);

        return label;
    }

    @NotNull
    @Contract(mutates = "this", value = "null -> fail; !null -> this")
    public MSDFScrollingTextWidget setText(@NotNull CharSequence text) {
        this.text = Objects.requireNonNull(Objects.requireNonNull(text, "argument 'text' may not be null.").toString(), "text.toString() yielded null");
        float minWidth = 0F;
        int lineStart = 0;
        int lineEnd;

        List<TextraLabel> runs = new ArrayList<>();
        while ((lineEnd = this.text.indexOf('\n', lineStart)) >= 0) {
            TextraLabel run = this.interpretLine(this.text.substring(lineStart, lineEnd));
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
