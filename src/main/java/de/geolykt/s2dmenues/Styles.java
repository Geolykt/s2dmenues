package de.geolykt.s2dmenues;

import java.util.Objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.textra.Font;
import com.github.tommyettinger.textra.Styles.TextButtonStyle;

import de.geolykt.s2dmenues.components.FullViewportDrawable;
import de.geolykt.starloader.api.gui.Drawing;

public class Styles implements Disposable {

    @Nullable
    private static Styles instance;

    @NotNull
    public static Styles getInstance() {
        Styles instance = Styles.instance;
        if (instance == null) {
            Styles.instance = instance = new Styles();
        }
        return instance;
    }

    @NotNull
    public final TextButtonStyle buttonStyle;
    @NotNull
    public final TextButtonStyle cancelButtonStyle;
    @NotNull
    public final TextButtonStyle confirmButtonStyle;
    @NotNull
    public final LabelStyle labelStyleGeneric;
    @NotNull
    public final Font msdfFont;
    @NotNull
    public final ScrollPaneStyle scrollPaneStyle;
    @NotNull
    public final SplitPaneStyle splitPaneStyle;
    @NotNull
    public final TextFieldStyle textFieldStyle;
    @NotNull
    public final WindowStyle windowStyleMainMenu;
    @NotNull
    public final WindowStyle windowStylePlastic;
    @NotNull
    public final WindowStyle windowStyleTranslucent;

    private Styles() {

        // MONOTYPE_BIG is another alternative
        //BitmapFont msdfBMPFont = Objects.requireNonNull(Drawing.getFontBitmap("SPACE"), "The requested font could not be not found");
        BitmapFont msdfBMPFont = Drawing.getSpaceFont();
        this.msdfFont = new Font(msdfBMPFont); // FIXME HIGHLY INAPPROPRIATE

        {
            this.buttonStyle = new TextButtonStyle();
            this.buttonStyle.font = this.msdfFont;
            this.buttonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xFE5B3EFF), 0.66F);
            this.buttonStyle.up.setMinWidth(300F);
            this.buttonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xFF3814FF), 0.75F);
            this.buttonStyle.over.setMinWidth(300F);
            this.buttonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
            this.buttonStyle.down.setMinWidth(300F);
            this.buttonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
            this.buttonStyle.disabled.setMinWidth(300F);
        }

        this.cancelButtonStyle = new TextButtonStyle();
        this.cancelButtonStyle.font = this.msdfFont;
        this.cancelButtonStyle.fontColor = Color.WHITE;
        this.cancelButtonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xAF2020FF), 0.5F);
        this.cancelButtonStyle.up.setMinWidth(300F);
        this.cancelButtonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xAF7F7FFF), 0.5F);
        this.cancelButtonStyle.over.setMinWidth(300F);
        this.cancelButtonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0xAF0000FF), 0.5F);
        this.cancelButtonStyle.down.setMinWidth(300F);
        this.cancelButtonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
        this.cancelButtonStyle.disabled.setMinWidth(300F);

        this.confirmButtonStyle = new TextButtonStyle();
        this.confirmButtonStyle.font = this.msdfFont;
        this.confirmButtonStyle.fontColor = Color.WHITE;
        this.confirmButtonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0x20AF20FF), 0.5F);
        this.confirmButtonStyle.up.setMinWidth(300F);
        this.confirmButtonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0x7FAF7FFF), 0.5F);
        this.confirmButtonStyle.over.setMinWidth(300F);
        this.confirmButtonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x00AF0000FF), 0.5F);
        this.confirmButtonStyle.down.setMinWidth(300F);
        this.confirmButtonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x7C489AFF), 0.5F);
        this.confirmButtonStyle.disabled.setMinWidth(300F);

        this.windowStyleTranslucent = new WindowStyle();
        this.windowStyleTranslucent.titleFont = Drawing.getSpaceFont();
        this.windowStyleTranslucent.titleFontColor = Color.WHITE;
        this.windowStyleTranslucent.background = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0xFF00007F), 0.5F);

        this.windowStylePlastic = new WindowStyle();
        this.windowStylePlastic.titleFont = Drawing.getSpaceFont();
        this.windowStylePlastic.titleFontColor = Color.WHITE;
        this.windowStylePlastic.background = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x7F7F7FFF), 0.5F);

        {
            TextureRegionDrawable tmp = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare());
            // Warning: TextureRegionDrawable.tint() does not return itself - that is the method call is 'pure'.
            FullViewportDrawable fullViewportDrawable = new FullViewportDrawable(Objects.requireNonNull(tmp.tint(new Color(0x80808080))));
            this.windowStyleTranslucent.stageBackground = fullViewportDrawable;
            this.windowStylePlastic.stageBackground = fullViewportDrawable;
        }

        this.splitPaneStyle = new SplitPaneStyle();
        this.splitPaneStyle.handle = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.BLACK);
        this.splitPaneStyle.handle.setMinHeight(15F);

        this.scrollPaneStyle = new ScrollPaneStyle();
        this.scrollPaneStyle.vScrollKnob = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.LIGHT_GRAY);
        this.scrollPaneStyle.vScrollKnob.setMinHeight(8);
        this.scrollPaneStyle.vScrollKnob.setMinWidth(4);

        this.textFieldStyle = new TextFieldStyle();
        this.textFieldStyle.font = Drawing.getSpaceFont();
        this.textFieldStyle.fontColor = Color.WHITE;
        this.textFieldStyle.background = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(Color.LIGHT_GRAY), 0.66F);
        this.textFieldStyle.background.setLeftWidth(10F);
        this.textFieldStyle.cursor = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.BLACK);
        this.textFieldStyle.cursor.setMinWidth(3F);

        this.labelStyleGeneric = new LabelStyle(Drawing.getSpaceFont(), Color.WHITE);

        this.windowStyleMainMenu = new WindowStyle();
        this.windowStyleMainMenu.titleFont = Drawing.getSpaceFont();
        this.windowStyleMainMenu.titleFontColor = Color.WHITE;
        this.windowStyleMainMenu.background = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0.8F, 0.5F, 0.5F, 0.6F), 0.2F);
    }

    public void dispose() {
        if (this == Styles.instance) {
            Styles.instance = null;
            this.msdfFont.dispose(); // We only need to dispose once so we shouldn't have any memory leaks with copies allocated with #getMSDFFont
        }
    }

    @NotNull
    @Contract(pure = true)
    public final Font getMSDFFont() {
        return new Font(this.msdfFont);
    }
}
