package de.geolykt.s2dmenues.components;

import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;

import de.geolykt.s2dmenues.RunnableClickListener;
import de.geolykt.s2dmenues.Styles;

import snoddasmannen.galimulator.FractalStarGenerator;
import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.ProceduralStarGenerator;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.StarGenerator;

public class GenGalaxyWindow extends Dialog {

    @NotNull
    private static String getCategoryName(@NotNull MapData map) {
        if (map.isQuickmap()) {
            return "Quickmaps";
        } else if (map.isFractalMap()) {
            return "Fractal";
        } else if (map.hasMovingStars()) {
            return "Procedural moving";
        } else if (map.getGenerator() instanceof ProceduralStarGenerator) {
            return "Procedural";
        } else if (map.isFullMapFromFile()) {
            return "Map on disk";
        }
        return "Uncategorised";
    }

    @NotNull
    private final TextButton closeButton;
    @NotNull
    private final Table contentTableUpper;
    @NotNull
    private final GalaxyPreviewWidget galaxyPreview;
    private int galaxySize;
    @NotNull
    private final TextButton galaxySizeButton;
    @NotNull
    private final TextButton galaxyTypeButton;
    @NotNull
    private MapData mapdata = new MapData(ProceduralStarGenerator.STRETCHED_SPIRAL);
    @NotNull
    private final SplitPane masterSplitPane;
    @NotNull
    private final TextButton openGeneratorOptionsButton;
    @NotNull
    private final TextButton scenarioButton;
    @NotNull
    private final TextButton starAdjustmentsButton;
    @NotNull
    private final TextButton starlaneGeneratorButton;

    public GenGalaxyWindow(@NotNull WindowStyle style) {
        super("Generate Galaxy", style);
        this.setMovable(true);
        this.setModal(true);
        this.setResizable(true);

        this.contentTableUpper = new Table();
        this.masterSplitPane = new SplitPane(this.contentTableUpper, null, true, Styles.getInstance().splitPaneStyle);

        this.galaxySizeButton = new RunnableTextButton("", Styles.getInstance().buttonStyle, (button) -> {
            // Display modal
        });
        this.setGalaxySize(5_000);
        this.galaxyTypeButton = new RunnableTextButton("Galaxy type", Styles.getInstance().buttonStyle, () -> {
            // Display modal
            Collection<MapData> maps = Space.getSelectableMaps();
            Table optionsTable = new Table();
            ScrollPane optionsScrolling = new ScrollPane(optionsTable, Styles.getInstance().scrollPaneStyle);
            NavigableMap<String, Set<RunnableTextButton>> mapButtons = new TreeMap<>();

            AtomicReference<RunnableTextButton> currentSelectedMapMode = new AtomicReference<>();
            for (MapData map : maps) {
                String mapName = map.getGenerator().name();
                String categoryName = GenGalaxyWindow.getCategoryName(map);
                RunnableTextButton textButton = new RunnableTextButton(Objects.requireNonNull(mapName), Styles.getInstance().buttonStyle, (mapButton) -> {
                    this.setMapData(map);
                    currentSelectedMapMode.get().setDisabled(false);
                    mapButton.setDisabled(true);
                    currentSelectedMapMode.lazySet(mapButton);
                });
                if (map.getGenerator() == ProceduralStarGenerator.STRETCHED_SPIRAL) {
                    currentSelectedMapMode.lazySet(textButton);
                    textButton.setDisabled(true);
                    this.setMapData(map);
                }
                textButton.addListener((evt) -> {
                    if (evt instanceof InputEvent && ((InputEvent) evt).getType() == InputEvent.Type.enter) {
                        GenGalaxyWindow.this.getStage().setScrollFocus(textButton);
                    }
                    return false;
                });
                mapButtons.compute(categoryName, (ignore, values) -> {
                    if (values == null) {
                        values = new TreeSet<>((a1, a2) -> {
                            return a1.getText().toString().compareToIgnoreCase(a2.getText().toString());
                        });
                    }
                    values.add(textButton);
                    return values;
                });
            }

            for (Map.Entry<String, Set<RunnableTextButton>> buttons : mapButtons.entrySet()) {
                String categoryName = Objects.requireNonNull(buttons.getKey());
                HorizontalGroup options = new HorizontalGroup().wrap().fill();
                buttons.getValue().forEach(options::addActor);

                TextButton categoryButton = new RunnableTextButton(categoryName, Styles.getInstance().buttonStyle, () -> {
                    // NOP
                });
                optionsTable.add(categoryButton).left().growX().row();
                optionsTable.add(options).left().growX().row();
            }

            this.masterSplitPane.setSecondWidget(optionsScrolling);
            if (this.masterSplitPane.getSplitAmount() > 0.5F) {
                this.masterSplitPane.setSplitAmount(0.5F);
            }
        });

        this.starAdjustmentsButton = new RunnableTextButton("Adjustments", Styles.getInstance().buttonStyle, (button) -> {
            // Display modal
        });
        this.starlaneGeneratorButton = new RunnableTextButton("Starlane generation", Styles.getInstance().buttonStyle, (button) -> {
            // Display modal
        });
        this.scenarioButton = new RunnableTextButton("Scenario", Styles.getInstance().buttonStyle, (button) -> {
            // Display modal
        });
        this.closeButton = new RunnableTextButton("Close", Styles.getInstance().cancelButtonStyle, (button) -> {
            GenGalaxyWindow.this.hide();
        });
        this.galaxyPreview = new GalaxyPreviewWidget(this);
        this.openGeneratorOptionsButton = new RunnableTextButton("Generator options", Styles.getInstance().buttonStyle, this::openGeneratorOptions);

        VerticalGroup options = new VerticalGroup();

        options.addActor(new NOPActor(15, 15));
        options.addActor(this.openGeneratorOptionsButton);
        options.addActor(this.galaxyTypeButton);
        options.addActor(this.galaxySizeButton);
        options.addActor(this.starAdjustmentsButton);
        options.addActor(this.starlaneGeneratorButton);
        options.addActor(this.scenarioButton);
        options.addActor(this.closeButton);
        options.addActor(new NOPActor(15, 15));

        this.getContentTable().add(this.masterSplitPane).top().left().grow();

        this.contentTableUpper.add(options).right().bottom().pad(8F);
        this.contentTableUpper.add(this.galaxyPreview).left().top().grow().pad(8F);

        // Ensure everything is updated (e.g. settings dialogs, etc.)
        this.setMapData(this.mapdata);
    }

    @NotNull
    public GenGalaxyWindow addCloseAction(@NotNull Runnable action) {
        this.closeButton.addListener(new RunnableClickListener(action));
        return this;
    }

    public int getGalaxySize() {
        return this.galaxySize;
    }

    @NotNull
    public MapData getMapdata() {
        return this.mapdata;
    }

    private void openGeneratorOptions() {
        MapData map = this.mapdata;
        StarGenerator generator = map.getGenerator();
        if (generator instanceof FractalStarGenerator) {
            FractalStarGenerator fsg = (FractalStarGenerator) generator;

            Table optionTable = new Table();

            TextButton fractalAlgoHeader = new RunnableTextButton("Fractal algorithm", Styles.getInstance().buttonStyle, () -> {
                // NOP
            });

            HorizontalGroup fractalAlgoOptions = new HorizontalGroup().wrap(true);
            TextButton[] algorithmButtons = new TextButton[FractalStarGenerator.LandGenerator.values().length];
            for (FractalStarGenerator.LandGenerator algo : FractalStarGenerator.LandGenerator.values()) {
                algorithmButtons[algo.ordinal()] = new RunnableTextButton(Objects.toString(algo), Styles.getInstance().buttonStyle, (clickedButton) -> {
                    // TODO the currently selected algorithm is lost - perhaps we could introduce some way of recovering it?
                    for (TextButton button : algorithmButtons) {
                        button.setDisabled(false);
                    }
                    clickedButton.setDisabled(true);
                    algo.a(fsg);
                    this.galaxyPreview.reset();
                });
            }
            for (TextButton button : algorithmButtons) {
                fractalAlgoOptions.addActor(button);
            }
            optionTable.add(fractalAlgoHeader).left().growX().row();
            optionTable.add(fractalAlgoOptions).growX().row();

            TextButton aspectRatioHeader = new RunnableTextButton("Aspect ratio [GRAY](Note: The preview cannot properly represent the current aspect ratio.)[]", Styles.getInstance().buttonStyle, () -> {
                // NOP
            });

            HorizontalGroup aspectRatioGroup = new HorizontalGroup().wrap(true);
            TextButton[] aspectRatioButtons = new TextButton[FractalStarGenerator.AspectRatio.values().length];
            for (FractalStarGenerator.AspectRatio aspectRatio : FractalStarGenerator.AspectRatio.values()) {
                aspectRatioButtons[aspectRatio.ordinal()] = new RunnableTextButton(Objects.toString(aspectRatio), Styles.getInstance().buttonStyle, (clickedButton) -> {
                    aspectRatioButtons[fsg.aspectRatio.ordinal()].setDisabled(false);
                    clickedButton.setDisabled(true);
                    fsg.a(aspectRatio);
                    this.galaxyPreview.reset();
                });
                if (fsg.aspectRatio == aspectRatio) {
                    aspectRatioButtons[aspectRatio.ordinal()].setDisabled(true);
                }
            }
            for (TextButton button : aspectRatioButtons) {
                aspectRatioGroup.addActor(button);
            }
            optionTable.add(aspectRatioHeader).left().growX().row();
            optionTable.add(aspectRatioGroup).growX().row();

            TextButton setSeedButton = new RunnableTextButton("Set seed [GRAY](" + fsg.seedString + ")[]", Styles.getInstance().buttonStyle, (setSeedClickedButton) -> {
                Dialog setSeedDialog = new Dialog("Set fractal seed", Styles.getInstance().windowStylePlastic);
                TextField seedInputField = new TextField(fsg.seedString, Styles.getInstance().textFieldStyle);
                TextButton dialogCancel = new RunnableTextButton("Cancel", Styles.getInstance().cancelButtonStyle, () -> {
                    setSeedDialog.hide();
                });
                TextButton dialogConfirm = new RunnableTextButton("Confirm", Styles.getInstance().confirmButtonStyle, () -> {
                    setSeedDialog.hide();
                    fsg.seedString = seedInputField.getText();
                    this.galaxyPreview.reset();
                    fsg.generateMap();
                    setSeedClickedButton.setText("Set seed [GRAY](" + fsg.seedString + ")[]");
                });
                setSeedDialog.getContentTable().add(seedInputField).pad(10).growX();
                setSeedDialog.getButtonTable().add(dialogCancel).pad(5);
                setSeedDialog.getButtonTable().add(dialogConfirm).pad(5);

                Stage stage = this.getStage();
                setSeedDialog.show(stage);
            });

            TextButton drawLandButton = new RunnableTextButton("Draw land [GRAY](" + fsg.drawLand + ")[]", Styles.getInstance().buttonStyle, (clickedButton) -> {
                fsg.drawLand = !fsg.drawLand;
                clickedButton.setText("Draw land [GRAY](" + fsg.drawLand + ")[]");
            });

            optionTable.add(new NOPActor(1, 50)).row();
            HorizontalGroup otherButtons = new HorizontalGroup();
            otherButtons.addActor(setSeedButton);
            otherButtons.addActor(drawLandButton);
            optionTable.add(otherButtons).row();

            ScrollPane scrollPane = new ScrollPane(optionTable, Styles.getInstance().scrollPaneStyle);
            this.masterSplitPane.setSecondWidget(scrollPane);
            this.getStage().setScrollFocus(optionTable);
        }
    }

    @NotNull
    public GenGalaxyWindow setGalaxySize(int size) {
        this.galaxySize = size;
        this.galaxySizeButton.setText("Galaxy size: [GRAY]" + this.galaxySize + "[]");
        return this;
    }

    public void setMapData(@NotNull MapData map) {
        this.mapdata = map;

        if (map.getGenerator() instanceof FractalStarGenerator) {
            this.openGeneratorOptionsButton.setVisible(true);
        } else {
            this.openGeneratorOptionsButton.setVisible(false);
        }
    }

    @NotNull
    public GenGalaxyWindow show(Stage stage) {
        super.show(stage);
        this.setBounds(16F, 16F, stage.getWidth() - 32F, stage.getHeight() - 32F);
        this.getTitleLabel().setAlignment(Align.left);
        return this;
    }
}
