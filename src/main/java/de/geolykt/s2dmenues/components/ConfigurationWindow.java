package de.geolykt.s2dmenues.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.TextraButton;
import com.github.tommyettinger.textra.TextraSelectBox;

import de.geolykt.s2dmenues.FontConfig;
import de.geolykt.s2dmenues.FontConfig.FontPrimitive;
import de.geolykt.s2dmenues.RunnableClickListener;
import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.Styles;

public class ConfigurationWindow extends S2DDialog {

    @NotNull
    private final TextraButton closeButton;

    public ConfigurationWindow() {
        super("Configuration", Styles.getInstance().windowStyleAquaTextra);

//        this.debugAll();
        this.setResizable(true);
        this.setResizeBorder(16);
        this.setMovable(true);

        this.closeButton = new RunnableTextraButton("Close", Styles.getInstance().cancelButtonStyle, (Runnable) this::hide);
        this.getButtonTable().add(this.closeButton);

        this.titleTable.clear();
        this.removeActor(this.titleTable);
        this.titleLabel.setAlignment(Align.center);
        this.getContentTable().add(this.titleLabel).colspan(2).top().fillX().height(48).row();

        this.addOption("Font", FontConfig.getInstance().getPreferredFontPrimitive(), FontConfig.getInstance().getAvailableFonts(), FontConfig.getInstance()::setPreferredFont, FontPrimitive::getName);
        this.addOption("Language", S2DI18N.getActiveLocale(), S2DI18N.getAvailableLocales(), S2DI18N::setActiveLocale, Locale::getDisplayName);

        this.getContentTable().add().bottom().growY().row();
    }

    @NotNull
    public ConfigurationWindow addCloseAction(@NotNull Runnable action) {
        this.closeButton.addListener(new RunnableClickListener(action));
        return this;
    }

    public <@NotNull T> void addOption(@NotNull String keyName, T currentValue, @NotNull Iterable<T> options, @NotNull Consumer<T> applyOption, @NotNull Function<T, @NotNull String> stringify) {
        TextraSelectBox selectBox = new S2DSelectBox();
        int i = 0;
        boolean foundCurrent = false;
        List<T> optionList = options instanceof List ? (List<T>) options : new ArrayList<>();

        for (T option : options) {
            DynamicTextraLabel label = new DynamicTextraLabel(stringify.apply(option)) {
                @Override
                public void draw(Batch batch, float parentAlpha) {
                    float prefWidth = selectBox.getList().getWidth();
                    Drawable background = selectBox.getStyle().listStyle.background;
                    if (background != null) {
                        prefWidth -= background.getLeftWidth() + background.getRightWidth();
                    }
                    if (this.getWidth() < prefWidth) {
                        this.setWidth(prefWidth);
                    }
                    super.draw(batch, parentAlpha);
                }
            };

            selectBox.getItems().add(label);

            if (option == currentValue) {
                foundCurrent = true;
                selectBox.setSelectedIndex(i);
            }

            if (optionList != options) {
                optionList.add(option);
            }

            i++;
        }

        selectBox.addListener((event) -> {
            if (event instanceof ChangeEvent) {
                applyOption.accept(optionList.get(selectBox.getSelectedIndex()));
                return true;
            } else {
                return false;
            }
        });

        if (!foundCurrent) {
            LoggerFactory.getLogger(ConfigurationWindow.class).warn("Improperly configured key '{}': Current option '{}' is not in the list of available options!", keyName, stringify.apply(currentValue));
        }

        selectBox.setItems(selectBox.getItems());
        this.getContentTable().add(new DynamicTextraLabel(keyName)).top().left().growX();
        this.getContentTable().add(selectBox).top().right().growX().row();
    }
}
