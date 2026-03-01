package de.geolykt.s2dmenues.components.gui;

import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.utils.Align;

import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.Styles;
import de.geolykt.s2dmenues.components.event.RunnableClickListener;
import de.geolykt.s2dmenues.components.msdf.RunnableTextraButton;
import de.geolykt.s2dmenues.components.msdf.S2DDialog;

public class LAFAquaDialog extends S2DDialog {

    @NotNull
    private final RunnableTextraButton closeButton;

    public LAFAquaDialog(@NotNull Supplier<@NotNull String> titleProvider, @NotNull String closeButtoni18nKey) {
        super(titleProvider, Styles.getInstance().windowStyleAquaTextra);

        this.setResizable(true);
        this.setResizeBorder(16);
        this.setMovable(true);

        this.closeButton = new RunnableTextraButton(S2DI18N.s2d(closeButtoni18nKey), Styles.getInstance().cancelButtonStyle, (Runnable) this::hide);
        this.getButtonTable().add(this.closeButton);

        this.titleTable.clear();
        this.removeActor(this.titleTable);
        this.titleLabel.setAlignment(Align.center);
        this.getContentTable().add(this.titleLabel).colspan(2).top().growX().height(48).row();
    }

    public LAFAquaDialog(@NotNull String i18nKey) {
        this(S2DI18N.s2d(i18nKey), "laf.dialog.button.close");
    }

    @NotNull
    @Contract(mutates = "this", pure = false, value = "null -> fail; !null -> this")
    public LAFAquaDialog addCloseAction(@NotNull Runnable action) {
        this.closeButton.addListener(new RunnableClickListener(action));
        return this;
    }
}
