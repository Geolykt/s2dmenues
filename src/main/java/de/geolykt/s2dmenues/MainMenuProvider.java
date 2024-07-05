package de.geolykt.s2dmenues;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;

import de.geolykt.s2dmenues.components.CroppingTextureDrawable;
import de.geolykt.s2dmenues.components.GenGalaxyWindow;
import de.geolykt.s2dmenues.components.NOPActor;
import de.geolykt.s2dmenues.components.S2DSavegameBrowser;
import de.geolykt.s2dmenues.components.TextDrawable;
import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.NullUtils;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.gui.openui.PathSavegame;
import de.geolykt.starloader.api.gui.openui.Savegame;
import de.geolykt.starloader.api.resource.DataFolderProvider;
import de.geolykt.starloader.api.utils.TickLoopLock.LockScope;
import de.geolykt.starloader.impl.GalimulatorImplementation;

public class MainMenuProvider {

    public static void display() {
        if (!Drawing.isRenderThread()) {
            try {
                throw new Exception("Stack trace");
            } catch (Exception e) {
                LoggerFactory.getLogger(MainMenuProvider.class).warn("Likely calling MainMenuProvider#display outside of render thread. This may cause serious issues!", e);
            }
        }

        try (LockScope lock = Galimulator.getSimulationLoopLock().acquireHardControlWithResources()) {
            Galimulator.setPaused(true);
            MainMenuProvider.display0();
        } catch (InterruptedException ex) {
            LoggerFactory.getLogger(MainMenuProvider.class).error("Interrupted while attempting to display main menu", ex);
        }
    }

    private static void display0() {
        Path parentDir = DataFolderProvider.getProvider().provideAsPath().resolve("mods/s2dmenues");
        if (Files.notExists(parentDir)) {
            try {
                Files.createDirectory(parentDir);
            } catch (IOException e) {
                LoggerFactory.getLogger(MainMenuProvider.class).error("Unable to create directory {}. Skipping display of main menu!", parentDir, e);
                return;
            }
        }
        Path menubg = parentDir.resolve("main-menu-background.png");
        String errmsg = null;
        Texture backgroundTexture = null;

        if (Files.exists(menubg)) {
            try {
                backgroundTexture = new Texture(new FileHandle(menubg.toFile()));
            } catch (RuntimeException e) {
                LoggerFactory.getLogger(MainMenuProvider.class).warn("Unable to create texture under path {}.", menubg, e);
                StringWriter writer = new StringWriter();
                try (PrintWriter printWriter = new PrintWriter(writer)) {
                    e.printStackTrace(printWriter);
                }
                errmsg = writer.toString();
            }
        } else {
            try {
                throw new Exception("Missing file at " + menubg.toAbsolutePath().toString());
            } catch (Exception e) {
                StringWriter writer = new StringWriter();
                try (PrintWriter printWriter = new PrintWriter(writer)) {
                    e.printStackTrace(printWriter);
                }
                errmsg = writer.toString().replace("\t", "    ");
            }
        }

        Drawable backgroundDrawable;
        if (backgroundTexture == null) {
            if (errmsg == null) {
                errmsg = "Assertion failed: Null background texture. No further information available.";
            }
            backgroundDrawable = new TextDrawable(errmsg, NullUtils.requireNotNull(Color.GREEN));
        } else {
            backgroundDrawable = new CroppingTextureDrawable(backgroundTexture, true);
        };

        Stage stage = new MainMenuStage(backgroundDrawable, true);
        VerticalGroup buttons = new VerticalGroup();

        TextButton exit = new TextButton("Exit game", Styles.getInstance().buttonStyle);
        exit.addListener(new RunnableClickListener(Gdx.app::exit));
        TextButton load = new TextButton("Load galaxy", Styles.getInstance().buttonStyle);
        TextButton newG = new TextButton("New galaxy", Styles.getInstance().buttonStyle);
        TextButton continueG = new TextButton("Continue galaxy", Styles.getInstance().buttonStyle);
        continueG.addListener(new RunnableClickListener(() -> {
            Drawing.setShownStage(null);
        }));
        load.addListener(new RunnableClickListener(() -> {
            MainMenuProvider.displayLoadMenu(stage);
        }));
        newG.addListener(new RunnableClickListener(() -> {
            MainMenuProvider.disableAll(stage.getRoot());
            GenGalaxyWindow window = new GenGalaxyWindow(Styles.getInstance().windowStyleTranslucent);
            window.addCloseAction(() -> {
                MainMenuProvider.enableAll(stage.getRoot());
            });
            window.pack();
            window.show(stage);
        }));

        buttons.setWidth(300F);
        buttons.setOrigin(Align.bottomLeft);
        buttons.align(Align.bottom);
        stage.addActor(buttons);

        buttons.addActor(continueG);
        buttons.addActor(newG);
        buttons.addActor(load);
        buttons.addActor(exit);

        buttons.getChildren().forEach((actor) -> {
            actor.setWidth(buttons.getWidth());
        });

        LoggerFactory.getLogger(MainMenuProvider.class).info("Injecting main menu stage");
        Drawing.setShownStage(stage);
    }

    private static void displayLoadMenu(@NotNull Stage stage) {
        MainMenuProvider.disableAll(stage.getRoot());

        Dialog dialog = new Dialog("Load savegame", Styles.getInstance().windowStyleTranslucent);
        TextButton close = new TextButton("Close", Styles.getInstance().cancelButtonStyle);
        S2DSavegameBrowser browser = new S2DSavegameBrowser(savegame -> {
            if (!(savegame instanceof PathSavegame)) {
                // Not part of the official API. I should really expose that method one of these days given of how useful it is
                GalimulatorImplementation.crash("Something went wrong while saving your savegame: The savegame location cannot be determined!", false);
                Drawing.setShownStage(null); // required in order to actually display the crash report (Although even if we were to forget that it would still be logged to console)
                return;
            }
            dialog.hide();
            Drawing.setShownStage(null);
            Galimulator.loadSavegameFile(((PathSavegame) savegame).getLocationPath());
        });

        try {
            Path savegameDir = Paths.get("").toAbsolutePath();
            List<Path> savegames = Files.walk(savegameDir, 1)
                    .filter(p -> p.getFileName().toString().endsWith(".dat"))
                    .distinct()
                    .collect(Collectors.toList());
            List<@NotNull Savegame> savegameInstances = new ArrayList<>();
            for (Path savegamePath : savegames) {
                if (savegamePath == null) {
                    throw new IllegalStateException();
                }
                savegameInstances.add(new PathSavegame(savegamePath));
            }
            savegameInstances.sort((s1, s2) -> Long.compare(s1.getLastModifiedTimestamp(), s2.getLastModifiedTimestamp()));
            browser.addSavegames(savegameInstances);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // vgroup1 and hgroup1 are there to implement padding in a remarkably basic way
        VerticalGroup vgroup1 = new VerticalGroup();
        HorizontalGroup hgroup1 = new HorizontalGroup();
        hgroup1.addActor(new NOPActor(15, 15));
        hgroup1.addActor(vgroup1);
        hgroup1.addActor(new NOPActor(15, 15));

        vgroup1.addActor(new NOPActor(15, 15));
        vgroup1.addActor(browser);
        vgroup1.addActor(close);
        vgroup1.addActor(new NOPActor(15, 15));

        close.addListener(new RunnableClickListener(() -> {
            dialog.hide();
            MainMenuProvider.enableAll(stage.getRoot());
        }));

        dialog.add(vgroup1);
        dialog.show(stage);
    }

    private static void disableAll(Actor a) {
        if (a instanceof Group && !(a instanceof Window)) {
            for (Actor c :((Group) a).getChildren().items) {
                disableAll(c);
            }
        }
        if (a instanceof Disableable) {
            ((Disableable) a).setDisabled(true);
        }
    }

    private static void enableAll(Actor a) {
        if (a instanceof Group && !(a instanceof Window)) {
            for (Actor c :((Group) a).getChildren().items) {
                enableAll(c);
            }
        }
        if (a instanceof Disableable) {
            ((Disableable) a).setDisabled(false);
        }
    }
}
