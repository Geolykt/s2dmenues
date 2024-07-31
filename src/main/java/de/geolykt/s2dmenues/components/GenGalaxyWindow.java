package de.geolykt.s2dmenues.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;

import de.geolykt.s2dmenues.RunnableClickListener;
import de.geolykt.s2dmenues.Styles;
import de.geolykt.s2dmenues.UIUtil;
import de.geolykt.s2dmenues.bridge.MovingSpiralStarGenerator;
import de.geolykt.s2dmenues.bridge.ReflectionHacks;
import de.geolykt.s2dmenues.bridge.VelocityMovingStarGenerator;
import de.geolykt.s2dmenues.incubator.StarPlacementGenerator;
import de.geolykt.s2dmenues.incubator.StarPlacementRegistry;
import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.empire.StarlaneGenerator;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.registry.Registry;
import de.geolykt.starloader.api.registry.RegistryKeys;

import snoddasmannen.galimulator.FractalStarGenerator;
import snoddasmannen.galimulator.MapData;
import snoddasmannen.galimulator.ProceduralScenarioSource;
import snoddasmannen.galimulator.ProceduralStarGenerator;
import snoddasmannen.galimulator.Scenario;
import snoddasmannen.galimulator.ScenarioSource;
import snoddasmannen.galimulator.Space;
import snoddasmannen.galimulator.Space.ConnectionMethod;
import snoddasmannen.galimulator.Space.StarAdjustmentMethod;
import snoddasmannen.galimulator.StarGenerator;

public class GenGalaxyWindow extends Dialog implements Disposable {

    private static enum SubDialog {
        ADJUSTMENT_METHODS,
        GALAXY_TYPE,
        GENERATOR_OPTIONS,
        NONE,
        SCENARIO_SOURCES,
        STARLANE_METHODS;
    }

    @NotNull
    private StarAdjustmentMethod adjustmentMethod = StarAdjustmentMethod.NORMAL;
    @NotNull
    private final TextButton closeButton;
    @NotNull
    private final Table contentTableUpper;
    @Nullable
    private StarPlacementGenerator currentGenerator;
    @NotNull
    private ScenarioSource currentScenarioSource = ProceduralScenarioSource.CLASSIC;
    @NotNull
    private SubDialog dialog = SubDialog.NONE;
    @NotNull
    private final TextButton galaxyGenerateButton;
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
    private StarlaneGenerator starlaneGenerator = Registry.STARLANE_GENERATORS.require(RegistryKeys.GALIMULATOR_STARLANES_STANDARD);
    @NotNull
    private final TextButton starlaneGeneratorButton;

    public GenGalaxyWindow(@NotNull WindowStyle style) {
        super("Generate Galaxy", style);
        this.setMovable(true);
        this.setModal(true);
        this.setResizable(true);

        this.contentTableUpper = new Table();
        this.masterSplitPane = new SplitPane(this.contentTableUpper, null, true, Styles.getInstance().splitPaneStyle);

        this.galaxyGenerateButton = new RunnableTextButton("Generate Galaxy!", Styles.getInstance().confirmButtonStyle, (button) -> {
            this.mapdata.setConnectionMethod((ConnectionMethod) this.starlaneGenerator);
            this.mapdata.setStarAdjustmentMethod(this.adjustmentMethod);
            this.mapdata.setScenarioSource(this.currentScenarioSource);
            Space.generateGalaxySync(this.galaxySize, this.mapdata);
            Drawing.setShownStage(null);
        });
        this.galaxySizeButton = UIUtil.createUnsignedIntInputButton("Star count", this::getGalaxySize, this::setGalaxySize);
        this.galaxyTypeButton = new MSDFTextButton("Galaxy type", Styles.getInstance().buttonStyle, (openGalaxyTypeSelectionButton) -> {
            this.enableCurrentDialogButton();
            openGalaxyTypeSelectionButton.setDisabled(true);
            this.dialog = SubDialog.GALAXY_TYPE;
            // Display modal
            Collection<StarPlacementGenerator> generators = StarPlacementRegistry.GENERATOR_REGISTRY.valuesView();
            Table optionsTable = new Table();
            ScrollPane optionsScrolling = new ScrollPane(optionsTable, Styles.getInstance().scrollPaneStyle);
            NavigableMap<String, Set<RunnableTextButton>> mapButtons = new TreeMap<>();

            if (this.currentGenerator == null) {
                this.currentGenerator = StarPlacementRegistry.GENERATOR_REGISTRY.require(NamespacedKey.fromString("galimulator", "PLACEMENT_GENERATOR_STRETCHED_SPIRAL"));
            }

            AtomicReference<RunnableTextButton> currentSelectedMapMode = new AtomicReference<>();
            for (StarPlacementGenerator map : generators) {
                String mapName = map.getDisplayName();
                String categoryName = map.getDisplayCategory();
                RunnableTextButton textButton = new MSDFTextButton(Objects.requireNonNull(mapName), Styles.getInstance().buttonStyle, (mapButton) -> {
                    this.setMapData((MapData) map.toLegacyMap());
                    currentSelectedMapMode.get().setDisabled(false);
                    mapButton.setDisabled(true);
                    currentSelectedMapMode.set(mapButton);
                    this.currentGenerator = map;
                });
                if (map == this.currentGenerator) {
                    currentSelectedMapMode.set(textButton);
                    textButton.setDisabled(true);
                    this.setMapData((MapData) map.toLegacyMap());
                }
                textButton.addListener((evt) -> {
                    if (evt instanceof InputEvent && ((InputEvent) evt).getType() == InputEvent.Type.enter) {
                        GenGalaxyWindow.this.getStage().setScrollFocus(evt.getListenerActor());
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

                TextButton categoryButton = new MSDFTextButton(categoryName, Styles.getInstance().buttonStyle, () -> {
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

        this.starAdjustmentsButton = new MSDFTextButton("Adjustments [GRAY](" + this.adjustmentMethod.toString() + ")[]", Styles.getInstance().buttonStyle, (starAdjustmentsButton) -> {
            this.enableCurrentDialogButton();
            starAdjustmentsButton.setDisabled(true);
            this.dialog = SubDialog.ADJUSTMENT_METHODS;
            TreeMap<String, Actor> buttons = new TreeMap<>();
            AtomicReference<Button> currentlyActiveButton = new AtomicReference<>();
            for (StarAdjustmentMethod adjustmentMethod : StarAdjustmentMethod.values()) {
                Button adjustmentButton = new MSDFTextButton(Objects.toString(adjustmentMethod), Styles.getInstance().buttonStyle, (clickedOption) -> {
                    this.adjustmentMethod = adjustmentMethod;
                    starAdjustmentsButton.setText("Adjustments [GRAY](" + this.adjustmentMethod.toString() + ")[]");
                    currentlyActiveButton.get().setDisabled(false);
                    currentlyActiveButton.lazySet(clickedOption);
                    clickedOption.setDisabled(true);
                });
                adjustmentButton.addListener((evt) -> {
                    if (evt instanceof InputEvent && ((InputEvent) evt).getType() == InputEvent.Type.enter) {
                        GenGalaxyWindow.this.getStage().setScrollFocus(evt.getListenerActor());
                    }
                    return false;
                });
                buttons.put(adjustmentMethod.toString(), adjustmentButton);
                if (adjustmentMethod == this.adjustmentMethod) {
                    adjustmentButton.setDisabled(true);
                    currentlyActiveButton.lazySet(adjustmentButton);
                }
            }

            HorizontalGroup buttonGroup = new HorizontalGroup().wrap(true).top().left();
            buttons.values().forEach(buttonGroup::addActor);
            this.masterSplitPane.setSecondWidget(new ScrollPane(buttonGroup, Styles.getInstance().scrollPaneStyle));
            if (this.masterSplitPane.getSplitAmount() > 0.8F) {
                this.masterSplitPane.setSplitAmount(0.8F);
            }
        });

        this.starlaneGeneratorButton = new MSDFTextButton("Starlanes [GRAY](" + this.starlaneGenerator.getDisplayName() + ")[]", Styles.getInstance().buttonStyle, (starlaneGenButton) -> {
            this.enableCurrentDialogButton();
            starlaneGenButton.setDisabled(true);
            this.dialog = SubDialog.STARLANE_METHODS;
            TreeMap<String, Actor> buttons = new TreeMap<>();
            AtomicReference<Button> currentlyActiveButton = new AtomicReference<>();
            for (StarlaneGenerator generator : Registry.STARLANE_GENERATORS.getValues()) {
                Button generatorButton = new MSDFTextButton(generator.getDisplayName(), Styles.getInstance().buttonStyle, (clickedOption) -> {
                    this.starlaneGenerator = generator;
                    starlaneGenButton.setText("Starlanes [GRAY](" + this.starlaneGenerator.getDisplayName() + ")[]");
                    currentlyActiveButton.get().setDisabled(false);
                    currentlyActiveButton.lazySet(clickedOption);
                    clickedOption.setDisabled(true);
                });
                generatorButton.addListener((evt) -> {
                    if (evt instanceof InputEvent && ((InputEvent) evt).getType() == InputEvent.Type.enter) {
                        GenGalaxyWindow.this.getStage().setScrollFocus(evt.getListenerActor());
                    }
                    return false;
                });
                buttons.put(generator.getDisplayName(), generatorButton);
                if (generator == this.starlaneGenerator) {
                    generatorButton.setDisabled(true);
                    currentlyActiveButton.lazySet(generatorButton);
                }
            }

            HorizontalGroup buttonGroup = new HorizontalGroup().wrap(true).top().left();
            buttons.values().forEach(buttonGroup::addActor);
            this.masterSplitPane.setSecondWidget(new ScrollPane(buttonGroup, Styles.getInstance().scrollPaneStyle));
            if (this.masterSplitPane.getSplitAmount() > 0.8F) {
                this.masterSplitPane.setSplitAmount(0.8F);
            }
        });

        this.scenarioButton = new MSDFTextButton("Scenario [GRAY](" + this.currentScenarioSource.getName() + ")[]", Styles.getInstance().buttonStyle, (scenarioSourceButton) -> {
            this.enableCurrentDialogButton();
            scenarioSourceButton.setDisabled(true);
            this.dialog = SubDialog.SCENARIO_SOURCES;
            TreeMap<String, Actor> buttons = new TreeMap<>();
            AtomicReference<Button> currentlyActiveButton = new AtomicReference<>();
            List<Object> scenarios = new ArrayList<>();
            scenarios.addAll(Scenario.loadScenarios());
            for (ScenarioSource scenario : ProceduralScenarioSource.values()) {
                scenarios.add(scenario);
            }
            for (Object scenario : scenarios) {
                ScenarioSource scenarioSource = (ScenarioSource) scenario;
                Button scenarioButton = new MSDFTextButton(Objects.requireNonNull(scenarioSource.getName()), Styles.getInstance().buttonStyle, (clickedOption) -> {
                    this.currentScenarioSource = scenarioSource;
                    scenarioSourceButton.setText("Scenario [GRAY](" + this.currentScenarioSource.getName() + ")[]");
                    currentlyActiveButton.get().setDisabled(false);
                    currentlyActiveButton.lazySet(clickedOption);
                    clickedOption.setDisabled(true);
                });
                scenarioButton.addListener((evt) -> {
                    if (evt instanceof InputEvent && ((InputEvent) evt).getType() == InputEvent.Type.enter) {
                        GenGalaxyWindow.this.getStage().setScrollFocus(evt.getListenerActor());
                    }
                    return false;
                });
                buttons.put(scenarioSource.getName(), scenarioButton);
                if (scenarioSource == this.currentScenarioSource) {
                    scenarioButton.setDisabled(true);
                    currentlyActiveButton.lazySet(scenarioButton);
                }
            }

            HorizontalGroup buttonGroup = new HorizontalGroup().wrap(true).top().left();
            buttons.values().forEach(buttonGroup::addActor);
            this.masterSplitPane.setSecondWidget(new ScrollPane(buttonGroup, Styles.getInstance().scrollPaneStyle));
            if (this.masterSplitPane.getSplitAmount() > 0.8F) {
                this.masterSplitPane.setSplitAmount(0.8F);
            }
        });
        this.closeButton = new RunnableTextButton("Close", Styles.getInstance().cancelButtonStyle, (Runnable) GenGalaxyWindow.this::hide);
        this.galaxyPreview = new GalaxyPreviewWidget(this);
        this.openGeneratorOptionsButton = new RunnableTextButton("Generator options", Styles.getInstance().buttonStyle, this::openGeneratorOptions);

        VerticalGroup options = new VerticalGroup();

        options.addActor(new NOPActor(15, 15));
        options.addActor(this.galaxyGenerateButton);
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
        this.setGalaxySize(5_000);
    }

    @NotNull
    public GenGalaxyWindow addCloseAction(@NotNull Runnable action) {
        this.closeButton.addListener(new RunnableClickListener(action));
        return this;
    }

    @Override
    public void dispose() {
        this.galaxyPreview.dispose();
    }

    private void enableCurrentDialogButton() {
        switch (this.dialog) {
        case ADJUSTMENT_METHODS:
            this.starAdjustmentsButton.setDisabled(false);
            break;
        case GALAXY_TYPE:
            this.galaxyTypeButton.setDisabled(false);
            break;
        case GENERATOR_OPTIONS:
            this.openGeneratorOptionsButton.setDisabled(false);
            break;
        case NONE:
            break;
        case SCENARIO_SOURCES:
            this.scenarioButton.setDisabled(false);
            break;
        case STARLANE_METHODS:
            this.starlaneGeneratorButton.setDisabled(false);
            break;
        default:
            LoggerFactory.getLogger(GenGalaxyWindow.class).warn("Unknown dialog button: {}", this.dialog);
            break;
        }
    }

    public int getGalaxySize() {
        return this.galaxySize;
    }

    @NotNull
    public MapData getMapdata() {
        return this.mapdata;
    }

    private void openGeneratorOptions() {
        this.enableCurrentDialogButton();
        this.openGeneratorOptionsButton.setDisabled(true);
        this.dialog = SubDialog.GENERATOR_OPTIONS;
        MapData map = this.mapdata;
        StarGenerator generator = map.getGenerator();
        if (generator instanceof FractalStarGenerator) {
            FractalStarGenerator fsg = (FractalStarGenerator) generator;

            Table optionTable = new Table();

            TextButton fractalAlgoHeader = new MSDFTextButton("Fractal algorithm", Styles.getInstance().buttonStyle, () -> {
                // NOP
            });

            HorizontalGroup fractalAlgoOptions = new HorizontalGroup().wrap(true);
            Button[] algorithmButtons = new TextButton[FractalStarGenerator.LandGenerator.values().length];
            AtomicReference<Button> currentActiveButton = new AtomicReference<>();
            for (FractalStarGenerator.LandGenerator algo : FractalStarGenerator.LandGenerator.values()) {
                Button algorithmButton = new MSDFTextButton(Objects.toString(algo), Styles.getInstance().buttonStyle, (clickedButton) -> {
                    for (Button button : algorithmButtons) {
                        button.setDisabled(false);
                    }
                    clickedButton.setDisabled(true);
                    algo.a(fsg);
                    fsg.a(algo);
                    this.galaxyPreview.reset();
                });
                if (algo == fsg.landGenerator) {
                    currentActiveButton.set(algorithmButton);
                    algorithmButton.setDisabled(true);
                }
                algorithmButtons[algo.ordinal()] = algorithmButton;
            }
            for (Actor button : algorithmButtons) {
                fractalAlgoOptions.addActor(button);
            }
            optionTable.add(fractalAlgoHeader).left().growX().row();
            optionTable.add(fractalAlgoOptions).growX().row();

            TextButton aspectRatioHeader = new MSDFTextButton("Aspect ratio [GRAY](Note: The preview cannot properly represent the current aspect ratio.)[]", Styles.getInstance().buttonStyle, () -> {
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

            TextButton setSeedButton = UIUtil.createTextInputButton("Set seed", () -> Objects.toString(fsg.seedString), seed -> {
                fsg.seedString = seed;
                this.galaxyPreview.reset();
                fsg.generateMap();
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
        } else if (generator instanceof VelocityMovingStarGenerator) {
            VelocityMovingStarGenerator vmg = (VelocityMovingStarGenerator) generator;
            TextButton setSpeedButton = UIUtil.createFloatInputButton("Set rotation speed", vmg::s2dmenues$getVelocity, (velocity) -> {
                vmg.s2dmenues$setVelocity(velocity);
                this.galaxyPreview.reset();
            });
            this.masterSplitPane.setSecondWidget(setSpeedButton);
            this.masterSplitPane.setSplitAmount(0.9F);
        } else if (generator == ProceduralStarGenerator.MOVING_PLANETS) {
            TextButton setPlanetCountButton = UIUtil.createUnsignedIntInputButton("Set planet count", ReflectionHacks::getPlanetaryStarGeneratorPlanetCount, (planetCount) -> {
                if (planetCount <= 0) {
                    planetCount = 1;
                }
                if (!ReflectionHacks.setPlanetaryStarGeneratorPlanetCount(planetCount)) {
                    Dialog noticeDialog = new Dialog("Error", Styles.getInstance().windowStyleTranslucent);
                    Button cancelNoticeButton = new RunnableTextButton("Ok", Styles.getInstance().cancelButtonStyle, (Runnable) noticeDialog::hide);
                    noticeDialog.getContentTable().add(new Label("A reflective error occured. Please take a look at the logs. Sorry!", Styles.getInstance().labelStyleGeneric)).pad(10);
                    noticeDialog.getButtonTable().add(cancelNoticeButton).pad(5);
                    noticeDialog.show(this.getStage());
                } else {
                    this.galaxyPreview.reset();
                }
            });
            this.masterSplitPane.setSecondWidget(setPlanetCountButton);
            this.masterSplitPane.setSplitAmount(0.9F);
        } else if (generator == ProceduralStarGenerator.MOVING_SPIRAL) {
            MovingSpiralStarGenerator movingSpiralGenerator = (MovingSpiralStarGenerator) generator;

            Table table = new Table();

            table.add(UIUtil.createFloatInputButton("Set core size", movingSpiralGenerator::s2dmenues$getCoreSize, (value) -> {
                movingSpiralGenerator.s2dmenues$setCoreSize(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            table.add(UIUtil.createFloatInputButton("Set orbital fudge", movingSpiralGenerator::s2dmenues$getOrbitalFudge, (value) -> {
                movingSpiralGenerator.s2dmenues$setOrbitalFudge(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            table.add(UIUtil.createFloatInputButton("Set rotation speed", movingSpiralGenerator::s2dmenues$getSpeed, (value) -> {
                movingSpiralGenerator.s2dmenues$setSpeed(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            table.add(UIUtil.createFloatInputButton("Set undulation", movingSpiralGenerator::s2dmenues$getUndulation, (value) -> {
                movingSpiralGenerator.s2dmenues$setUndulation(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            ScrollPane scrollPAne = new ScrollPane(table, Styles.getInstance().scrollPaneStyle);
            this.masterSplitPane.setSecondWidget(scrollPAne);
            this.masterSplitPane.setSplitAmount(0.73F);
            this.getStage().setScrollFocus(table);
        }
    }

    @NotNull
    public GenGalaxyWindow setGalaxySize(int size) {
        this.galaxySize = size;
        this.galaxySizeButton.setText("Galaxy size: [GRAY]" + this.galaxySize + "[]");
        this.galaxyPreview.reset();
        return this;
    }

    public void setMapData(@NotNull MapData map) {
        this.mapdata = map;

        StarGenerator generator = map.getGenerator();
        if (generator instanceof FractalStarGenerator
                || generator instanceof VelocityMovingStarGenerator
                || generator == ProceduralStarGenerator.MOVING_SPIRAL
                || generator == ProceduralStarGenerator.MOVING_PLANETS) {
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
