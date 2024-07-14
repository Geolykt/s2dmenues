package de.geolykt.s2dmenues;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.stianloader.micromixin.transform.internal.util.Objects;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

import de.geolykt.s2dmenues.components.RunnableTextButton;

public class UIUtil {

    @NotNull
    public static TextButton createTextInputButton(@NotNull String description, @NotNull Supplier<@NotNull String> currentValueSupplier, @NotNull Consumer<@NotNull String> currentValueSetter) {
        return new RunnableTextButton(description + " [GRAY](" + currentValueSupplier.get() + ")[]", Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialog(Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), value -> {
                currentValueSetter.accept(value);
                textButton.setText(description + " [GRAY](" + currentValueSupplier.get() + ")[]");
            }, currentValueSupplier.get());
        });
    }

    @NotNull
    public static TextButton createUnsignedIntInputButton(@NotNull String description, @NotNull IntSupplier currentValueSupplier, @NotNull IntConsumer currentValueSetter) {
        return new RunnableTextButton(description + " [GRAY](" + currentValueSupplier.getAsInt() + ")[]", Styles.getInstance().buttonStyle, (textButton) -> {
            UIUtil.showInputDialogUnsignedInt(Objects.requireNonNull(textButton.getStage(), "button not part of any stage"), value -> {
                currentValueSetter.accept(value);
                textButton.setText(description + " [GRAY](" + currentValueSupplier.getAsInt() + ")[]");
            });
        });
    }

    public static void showInputDialog(@NotNull Stage stage, @NotNull Consumer<@NotNull String> onAccept, @NotNull String defaultInputValue) {
        Dialog setCountdialog = new Dialog("Set planet count", Styles.getInstance().windowStylePlastic);
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

    public static void showInputDialog(@NotNull Stage stage, @NotNull Consumer<@NotNull String> onAccept) {
        UIUtil.showInputDialog(stage, onAccept, "");
    }

    public static void showInputDialogUnsignedInt(@NotNull Stage stage, @NotNull IntConsumer onAccept) {
        UIUtil.showInputDialog(stage, (text) -> {
            try {
                if (!text.isEmpty()) { // Make no text be like no operation
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

    private UIUtil() {
        throw new UnsupportedOperationException();
    }
}
