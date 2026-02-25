package de.geolykt.s2dmenues.components.gui;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.utils.Align;

import de.geolykt.s2dmenues.RunnableClickListener;
import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.Styles;
import de.geolykt.s2dmenues.components.msdf.RunnableTextraButton;
import de.geolykt.s2dmenues.components.msdf.S2DDialog;

public class LAFAquaDialog extends S2DDialog {

    @NotNull
    private final RunnableTextraButton closeButton;

    public LAFAquaDialog(@NotNull String i18nKey) {
        super(S2DI18N.s2d(i18nKey), Styles.getInstance().windowStyleAquaTextra);
        this.setResizable(true);
        this.setResizeBorder(16);
        this.setMovable(true);

        this.closeButton = new RunnableTextraButton(S2DI18N.s2d("laf.dialog.button.close"), Styles.getInstance().cancelButtonStyle, (Runnable) this::hide);
        this.getButtonTable().add(this.closeButton);

        this.titleTable.clear();
        this.removeActor(this.titleTable);
        this.titleLabel.setAlignment(Align.center);
        this.getContentTable().add(this.titleLabel).colspan(2).top().growX().height(48).row();
    }

    @NotNull
    @Contract(mutates = "this", pure = false, value = "null -> fail; !null -> this")
    public LAFAquaDialog addCloseAction(@NotNull Runnable action) {
        this.closeButton.addListener(new RunnableClickListener(action));
        return this;
    }
}
