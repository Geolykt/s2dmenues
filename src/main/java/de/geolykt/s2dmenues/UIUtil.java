package de.geolykt.s2dmenues;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import de.geolykt.s2dmenues.components.MSDFTextButton;
import de.geolykt.s2dmenues.components.RunnableTextButton;
import de.geolykt.starloader.api.utils.FloatConsumer;

public class UIUtil {

    @FunctionalInterface
    public static interface FloatSupplier {
        public float getAsFloat();
    }

    @NotNull
    public static TextButton createTextInputButton(@NotNull String description, @NotNull Supplier<@NotNull String> currentValueSupplier, @NotNull Consumer<@NotNull String> currentValueSetter) {
        return new MSDFTextButton(description + " [GRAY](" + currentValueSupplier.get() + ")[]", Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialog(description, Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), value -> {
                currentValueSetter.accept(value);
                textButton.setText(description + " [GRAY](" + currentValueSupplier.get() + ")[]");
            }, currentValueSupplier.get());
        });
    }

    @NotNull
    public static TextButton createFloatInputButton(@NotNull String description, @NotNull FloatSupplier currentValueSupplier, @NotNull FloatConsumer currentValueSetter) {
        return new MSDFTextButton(description + " [GRAY](" + currentValueSupplier.getAsFloat() + ")[]", Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialogFloat(description, Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), value -> {
                currentValueSetter.accept(value);
                textButton.setText(description + " [GRAY](" + currentValueSupplier.getAsFloat() + ")[]");
            });
        });
    }

    @NotNull
    public static TextButton createUnsignedIntInputButton(@NotNull String description, @NotNull IntSupplier currentValueSupplier, @NotNull IntConsumer currentValueSetter) {
        return new MSDFTextButton(description + " [GRAY](" + currentValueSupplier.getAsInt() + ")[]", Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialogUnsignedInt(description, Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), value -> {
                currentValueSetter.accept(value);
                textButton.setText(description + " [GRAY](" + currentValueSupplier.getAsInt() + ")[]");
            });
        });
    }

    public static void showInputDialog(@NotNull String title, @NotNull Stage stage, @NotNull Consumer<@NotNull String> onAccept, @NotNull String defaultInputValue) {
        Dialog setCountdialog = new Dialog(title, Styles.getInstance().windowStylePlastic);
        TextField inputField = new TextField(defaultInputValue, Styles.getInstance().textFieldStyle);
        Actor dialogCancel = new RunnableTextButton("Cancel", Styles.getInstance().cancelButtonStyle, (Runnable) setCountdialog::hide);
        Actor dialogConfirm = new RunnableTextButton("Confirm", Styles.getInstance().confirmButtonStyle, () -> {
            setCountdialog.hide();
            onAccept.accept(Objects.requireNonNull(inputField.getText()));
        });
        setCountdialog.getContentTable().add(inputField).pad(10).padTop(40).growX();
        setCountdialog.getButtonTable().add(dialogConfirm).pad(5);
        setCountdialog.getButtonTable().add(dialogCancel).pad(5);

        setCountdialog.show(stage);
        stage.setKeyboardFocus(inputField);
    }

    public static void showInputDialog(@NotNull String title, @NotNull Stage stage, @NotNull Consumer<@NotNull String> onAccept) {
        UIUtil.showInputDialog(title, stage, onAccept, "");
    }

    public static void showInputDialogUnsignedInt(@NotNull String title, @NotNull Stage stage, @NotNull IntConsumer onAccept) {
        UIUtil.showInputDialog(title, stage, (text) -> {
            try {
                if (!text.isEmpty()) { // Make no text behave like a no-op
                    onAccept.accept(Integer.parseUnsignedInt(text));
                }
            } catch (NumberFormatException nfe) {
                Dialog noticeDialog = new Dialog("Error", Styles.getInstance().windowStyleTranslucent);
                Button cancelNoticeButton = new RunnableTextButton("Ok", Styles.getInstance().cancelButtonStyle, (Runnable) noticeDialog::hide);
                noticeDialog.getContentTable().add(new Label("Not a valid number: '" + text + "'", Styles.getInstance().labelStyleGeneric)).pad(10);
                noticeDialog.getButtonTable().add(cancelNoticeButton).pad(5);
                noticeDialog.show(stage);
            }
        });
    }

    public static void showInputDialogFloat(@NotNull String title, @NotNull Stage stage, @NotNull FloatConsumer onAccept) {
        UIUtil.showInputDialog(title, stage, (text) -> {
            try {
                if (!text.isEmpty()) { // Make no text be like no operation
                    onAccept.accept(Float.parseFloat(text));
                }
            } catch (NumberFormatException nfe) {
                Dialog noticeDialog = new Dialog("Error", Styles.getInstance().windowStyleTranslucent);
                Button cancelNoticeButton = new RunnableTextButton("Ok", Styles.getInstance().cancelButtonStyle, (Runnable) noticeDialog::hide);
                noticeDialog.getContentTable().add(new Label("Not a valid number: '" + text + "'", Styles.getInstance().labelStyleGeneric)).pad(10);
                noticeDialog.getButtonTable().add(cancelNoticeButton).pad(5);
                noticeDialog.show(stage);
            }
        });
    }

    private UIUtil() {
        throw new UnsupportedOperationException();
    }
}
