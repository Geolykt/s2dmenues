package de.geolykt.s2dmenues;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane.SplitPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.textra.Styles.ListStyle;
import com.github.tommyettinger.textra.Styles.TextButtonStyle;
import com.github.tommyettinger.textra.Styles.TextFieldStyle;

import de.geolykt.s2dmenues.components.drawables.FullViewportDrawable;
import de.geolykt.s2dmenues.components.drawables.LAFAquaBackgroundDrawable;
import de.geolykt.s2dmenues.components.drawables.LAFAquaBoxDrawable;
import de.geolykt.s2dmenues.components.msdf.S2DSelectBox.BasicSelectionDrawable;
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
    public final TextButtonStyle aquaEphemeralButtonStyle;
    @NotNull
    public final ListStyle aquaListStyle;
    @NotNull
    public final TextButtonStyle buttonStyle;
    @NotNull
    public final TextButtonStyle cancelButtonStyle;
    @NotNull
    public final TextButtonStyle confirmButtonStyle;
    @NotNull
    public final ScrollPaneStyle scrollPaneStyle;
    @NotNull
    public final SplitPaneStyle splitPaneStyle;
    @NotNull
    public final TextFieldStyle textFieldStyle;
    @NotNull
    public final com.github.tommyettinger.textra.Styles.WindowStyle windowStyleAquaTextra;
    @NotNull
    public final WindowStyle windowStyleMainMenu;

    private Styles() {
        this.aquaEphemeralButtonStyle = new TextButtonStyle();
        this.aquaEphemeralButtonStyle.font = FontConfig.getInstance().getPreferredFont();
        this.aquaEphemeralButtonStyle.up = new LAFAquaBoxDrawable(4F);
        this.aquaEphemeralButtonStyle.over = new LAFAquaBoxDrawable(4F);
        this.aquaEphemeralButtonStyle.down = new LAFAquaBoxDrawable(4F);
        this.aquaEphemeralButtonStyle.disabled = new LAFAquaBoxDrawable(4F);

        this.aquaListStyle = new ListStyle();
        this.aquaListStyle.fontColorSelected = Color.GRAY;
        this.aquaListStyle.fontColorUnselected = Color.WHITE;
        this.aquaListStyle.selection = new BasicSelectionDrawable();
        // 157, 68, 47
        this.aquaListStyle.background = new LAFAquaBoxDrawable(16F, Color.toFloatBits(90, 45, 33/*157, 68, 47*/, 255));

        this.buttonStyle = new TextButtonStyle();
        this.buttonStyle.font = FontConfig.getInstance().getPreferredFont();
        this.buttonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xFE5B3EFF), 0.66F);
        this.buttonStyle.up.setMinWidth(300F);
        this.buttonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0xFF3814FF), 0.75F);
        this.buttonStyle.over.setMinWidth(300F);
        this.buttonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
        this.buttonStyle.down.setMinWidth(300F);
        this.buttonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x487C9AFF), 0.5F);
        this.buttonStyle.disabled.setMinWidth(300F);

        this.cancelButtonStyle = new TextButtonStyle();
        this.cancelButtonStyle.font = FontConfig.getInstance().getPreferredFont();
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
        this.confirmButtonStyle.font = FontConfig.getInstance().getPreferredFont();
        this.confirmButtonStyle.fontColor = Color.WHITE;
        this.confirmButtonStyle.up = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0x20AF20FF), 0.5F);
        this.confirmButtonStyle.up.setMinWidth(300F);
        this.confirmButtonStyle.over = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0x7FAF7FFF), 0.5F);
        this.confirmButtonStyle.over.setMinWidth(300F);
        this.confirmButtonStyle.down = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x00AF0000FF), 0.5F);
        this.confirmButtonStyle.down.setMinWidth(300F);
        this.confirmButtonStyle.disabled = TextureCache.getInstance().getGradientWindowTenpatch(true, new Color(0x7C489AFF), 0.5F);
        this.confirmButtonStyle.disabled.setMinWidth(300F);

        this.windowStyleAquaTextra = new com.github.tommyettinger.textra.Styles.WindowStyle();
        this.windowStyleAquaTextra.titleFont = FontConfig.getInstance().getPreferredFont();
        this.windowStyleAquaTextra.background = new LAFAquaBackgroundDrawable();

        {
            TextureRegionDrawable tmp = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare());
            // Warning: TextureRegionDrawable.tint() does not return itself - that is the method call is 'pure'.
            this.windowStyleAquaTextra.stageBackground = new FullViewportDrawable(Objects.requireNonNull(tmp.tint(new Color(0x80808080))));
        }

        this.splitPaneStyle = new SplitPaneStyle();
        this.splitPaneStyle.handle = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.BLACK);
        this.splitPaneStyle.handle.setMinHeight(15F);

        this.scrollPaneStyle = new ScrollPaneStyle();
        this.scrollPaneStyle.vScrollKnob = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.LIGHT_GRAY);
        this.scrollPaneStyle.vScrollKnob.setMinHeight(8);
        this.scrollPaneStyle.vScrollKnob.setMinWidth(8);

        this.textFieldStyle = new TextFieldStyle();
        this.textFieldStyle.font = FontConfig.getInstance().getPreferredFont();
        this.textFieldStyle.fontColor = Color.WHITE;
        this.textFieldStyle.background = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(Color.LIGHT_GRAY), 0.66F);
        this.textFieldStyle.background.setLeftWidth(10F);
        this.textFieldStyle.cursor = new TextureRegionDrawable(Drawing.getTextureProvider().getSinglePixelSquare()).tint(Color.BLACK);
        this.textFieldStyle.cursor.setMinWidth(3F);

        this.windowStyleMainMenu = new WindowStyle();
        this.windowStyleMainMenu.titleFont = Drawing.getSpaceFont();
        this.windowStyleMainMenu.titleFontColor = Color.WHITE;
        this.windowStyleMainMenu.background = TextureCache.getInstance().getGradientWindowTenpatch(false, new Color(0.8F, 0.5F, 0.5F, 0.6F), 0.2F);
    }

    public void dispose() {
        if (this == Styles.instance) {
            Styles.instance = null;
        }
    }
}
