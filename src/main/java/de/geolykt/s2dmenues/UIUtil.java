package de.geolykt.s2dmenues;

import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.github.tommyettinger.textra.Styles.TextButtonStyle;
import com.github.tommyettinger.textra.TextraButton;
import com.github.tommyettinger.textra.TextraField;

import de.geolykt.s2dmenues.S2DI18N.PlaceholderContext;
import de.geolykt.s2dmenues.bridge.I18NCapable;
import de.geolykt.s2dmenues.components.drawables.LAFAquaBoxDrawable;
import de.geolykt.s2dmenues.components.drawables.LAFAquaEphemeralButtonDrawable;
import de.geolykt.s2dmenues.components.event.ActorLifecycle;
import de.geolykt.s2dmenues.components.gui.LAFAquaDialog;
import de.geolykt.s2dmenues.components.msdf.DynamicTextraField;
import de.geolykt.s2dmenues.components.msdf.DynamicTextraLabel;
import de.geolykt.s2dmenues.components.msdf.RunnableTextraButton;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.utils.FloatConsumer;

public class UIUtil {
    private static class InvalidNumberPlaceholderContext implements PlaceholderContext {
        @NotNull
        private final String text;

        @NotNull
        public static final NamespacedKey REGISTRY_KEY = NamespacedKey.fromString("s2dmenues", "legacylaf.uiutil.nan");

        public InvalidNumberPlaceholderContext(@NotNull String text) {
            this.text = text;
        }

        @Override
        @NotNull
        public String applyPlaceholder(@NotNull String key) {
            if (!key.equals("text")) {
                throw new IllegalArgumentException("Unexpected key: " + key);
            }

            return this.text;
        }
    }

    @NotNull
    public static TextraButton createFloatInputButton(@NotNull Supplier<@NotNull String> description, @NotNull FloatConsumer currentValueSetter) {
        return new RunnableTextraButton(description, Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialogFloat(description, Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), currentValueSetter);
        });
    }

    @NotNull
    public static TextraButton createTextInputButton(@NotNull Supplier<@NotNull String> description, @NotNull Supplier<@NotNull String> currentValueSupplier, @NotNull Consumer<@NotNull String> currentValueSetter) {
        return new RunnableTextraButton(description, Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialog(description, Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), currentValueSetter, currentValueSupplier.get());
        });
    }

    @NotNull
    public static TextraButton createUnsignedIntInputButton(@NotNull Supplier<@NotNull String> description, @NotNull IntConsumer currentValueSetter) {
        return new RunnableTextraButton(description, Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialogUnsignedInt(description, Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), currentValueSetter);
        });
    }

    public static void showInputDialog(@NotNull Supplier<@NotNull String> title, @NotNull Stage stage, @NotNull Consumer<@NotNull String> onAccept) {
        UIUtil.showInputDialog(title, stage, onAccept, "");
    }

    public static void showInputDialog(@NotNull Supplier<@NotNull String> title, @NotNull Stage stage, @NotNull Consumer<@NotNull String> onAccept, @NotNull String defaultInputValue) {
        LAFAquaDialog setCountdialog = new LAFAquaDialog(title, "legacylaf.uiutil.cancel");
        TextraField inputField = new DynamicTextraField(defaultInputValue, Styles.getInstance().textFieldStyle);

        Actor dialogConfirm = new RunnableTextraButton(S2DI18N.s2d("legacylaf.uiutil.confirm"), Styles.getInstance().confirmButtonStyle, () -> {
            setCountdialog.hide();
            onAccept.accept(Objects.requireNonNull(inputField.getText()));
        });

        setCountdialog.getContentTable().add(inputField).pad(10).padTop(40).growX();
        setCountdialog.getButtonTable().add(dialogConfirm).pad(5);

        setCountdialog.show(stage);
        stage.setKeyboardFocus(inputField);
    }

    public static void showInputDialogFloat(@NotNull Supplier<@NotNull String> title, @NotNull Stage stage, @NotNull FloatConsumer onAccept) {
        UIUtil.showInputDialog(title, stage, (text) -> {
            try {
                if (!text.isEmpty()) { // Make no text be like no operation
                    onAccept.accept(Float.parseFloat(text));
                }
            } catch (NumberFormatException nfe) {
                LAFAquaDialog noticeDialog = new LAFAquaDialog("legacylaf.uiutil.error");
                noticeDialog.getContentTable().add(new DynamicTextraLabel(S2DI18N.s2d("legacylaf.uiutil.nan").withContext(InvalidNumberPlaceholderContext.REGISTRY_KEY, new InvalidNumberPlaceholderContext(text)))).grow();
                noticeDialog.show(stage);
            }
        });
    }

    public static void showInputDialogUnsignedInt(@NotNull Supplier<@NotNull String> title, @NotNull Stage stage, @NotNull IntConsumer onAccept) {
        UIUtil.showInputDialog(title, stage, (text) -> {
            try {
                if (!text.isEmpty()) { // Make no text behave like a no-op
                    onAccept.accept(Integer.parseUnsignedInt(text));
                }
            } catch (NumberFormatException nfe) {
                LAFAquaDialog noticeDialog = new LAFAquaDialog("legacylaf.uiutil.error");
                noticeDialog.getContentTable().add(new DynamicTextraLabel(S2DI18N.s2d("legacylaf.uiutil.nan").withContext(InvalidNumberPlaceholderContext.REGISTRY_KEY, new InvalidNumberPlaceholderContext(text)))).grow();
                noticeDialog.show(stage);
            }
        });
    }

    public static <@NotNull T extends I18NCapable> void showSelectionWindow(@NotNull InputEvent event, @NotNull Iterable<T> elements, @NotNull BiConsumer<T, @NotNull InputEvent> onClick, int preferredHAlign) {
        UIUtil.showSelectionWindow(event, elements, I18NCapable::s2dmenues$getLocalisation, onClick, preferredHAlign);
    }

    public static <@NotNull T> void showSelectionWindow(@NotNull InputEvent triggeringEvent, @NotNull Iterable<T> elements, @NotNull Function<T, @NotNull Supplier<@NotNull String>> textifier, @NotNull BiConsumer<T, @NotNull InputEvent> onClick, int preferredHAlign) {
        Objects.requireNonNull(onClick, "'onClick' may not be null"); // preemptive null check

        TreeMap<String, T> placementOrder = new TreeMap<>();

        for (T element : elements) {
            placementOrder.put(textifier.apply(element).get(), element);
        }

        Table table = new Table();

        table.setBackground(new LAFAquaBoxDrawable(8F, Color.CLEAR.toFloatBits()));

        for (Iterator<T> it = placementOrder.values().iterator(); it.hasNext();) {
            T orderedElement = it.next();
            byte enabledVertices = it.hasNext() ? LAFAquaEphemeralButtonDrawable.DRAW_EDGE_SOUTH : 0;

            TextButtonStyle aquaEphemeralButtonStyle = new TextButtonStyle();
            aquaEphemeralButtonStyle.font = FontConfig.getInstance().getPreferredFont();
            aquaEphemeralButtonStyle.up = new LAFAquaEphemeralButtonDrawable(4F, enabledVertices);
            aquaEphemeralButtonStyle.over = new LAFAquaEphemeralButtonDrawable(4F, Color.toFloatBits(0.0F / 256.0F, 100.0F / 256.0F, 100.0F / 256.0F, 0.8F), enabledVertices);
            aquaEphemeralButtonStyle.down = new LAFAquaEphemeralButtonDrawable(4F, Color.toFloatBits(1F, 207.0F / 256.0F, 1F, 1F), enabledVertices);
            aquaEphemeralButtonStyle.disabled = new LAFAquaEphemeralButtonDrawable(4F, enabledVertices);

            TextraButton button = new RunnableTextraButton(textifier.apply(orderedElement), aquaEphemeralButtonStyle, (clickedButton, clickedEvent) -> {
                onClick.accept(orderedElement, clickedEvent);
            });

            table.add(button).prefWidth(button.getWidth()).row();
        }

        table.pack();
        float positionY = Math.min(triggeringEvent.getStageY(), triggeringEvent.getStage().getHeight() - table.getHeight());
        float positionX;

        if (Align.isLeft(preferredHAlign)) {
            positionX = Math.max(triggeringEvent.getStageX() - table.getWidth(), 0);
        } else if (Align.isRight(preferredHAlign)) {
            positionX = Math.min(triggeringEvent.getStageX(), triggeringEvent.getStage().getWidth() - table.getWidth());
        } else {
            positionX = Math.max(Math.min(triggeringEvent.getStageX() - table.getWidth() / 2, triggeringEvent.getStage().getWidth() - table.getWidth()), 0);
        }

        ScrollPane dialogPane = new ScrollPane(table, Styles.getInstance().scrollPaneStyle);

        dialogPane.setWidth(table.getWidth());
        dialogPane.setHeight(positionY < 0 ? triggeringEvent.getStage().getHeight() : table.getHeight());
        dialogPane.setPosition(positionX, Math.max(0, positionY));

        new ActorLifecycle(Objects.requireNonNull(triggeringEvent.getStage(), "'InputEvent#getStage' may not return null"), dialogPane).disposeOnUnfocus();
    }

    private UIUtil() {
        throw new UnsupportedOperationException();
    }
}
