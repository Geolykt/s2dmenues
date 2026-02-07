package de.geolykt.s2dmenues.components;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.github.tommyettinger.textra.TextraButton;

import de.geolykt.s2dmenues.Styles;
import de.geolykt.starloader.api.gui.openui.Savegame;

public class S2DSavegameBrowser extends Container<ScrollPane> {
    @NotNull
    private final VerticalGroup buttons = new VerticalGroup();
    @NotNull
    private final Consumer<@NotNull Savegame> consumer;

    public S2DSavegameBrowser(@NotNull Consumer<@NotNull Savegame> consumer) {
        Objects.requireNonNull(consumer, "\"consumer\" may not be null");
        this.consumer = consumer;
        this.setActor(new ScrollPane(this.buttons));
    }

    /**
     * Adds multiple savegames to the list of savegames to display in the browser
     *
     * @param savegames The savegames to add
     * @return The current {@link S2DSavegameBrowser}, for chaining
     */
    @NotNull
    @Contract(mutates = "this", pure = false, value = "null -> fail; !null -> this")
    public S2DSavegameBrowser addSavegames(@NotNull List<@NotNull Savegame> savegames) {
        savegames.forEach(this::addSavegame);
        return this;
    }

    /**
     * Adds a savegame to the list of savegames to display in the browser
     *
     * @param savegame The savegame to add
     * @return The current {@link S2DSavegameBrowser}, for chaining
     */
    @NotNull
    @Contract(mutates = "this", pure = false, value = "null -> fail; !null -> this")
    public S2DSavegameBrowser addSavegame(@NotNull Savegame savegame) {
        Objects.requireNonNull(savegame, "savegame may not be null");
        ZonedDateTime timestampTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(savegame.getLastModifiedTimestamp()), ZoneId.systemDefault());
        String timestamp = DateTimeFormatter.RFC_1123_DATE_TIME.format(timestampTime);

        String text = savegame.getDisplayName() + "\n" + timestamp + "\n[GRAY]" +  savegame.getSavagameFormat() + " (" + savegame.getGalimulatorVersion() + ")[]";
        TextraButton button = new RunnableTextraButton(text, Styles.getInstance().buttonStyle, () -> {
            this.consumer.accept(savegame);
        });
        button.setHeight(80F);
        this.buttons.addActor(button);
        return this;
    }
}
