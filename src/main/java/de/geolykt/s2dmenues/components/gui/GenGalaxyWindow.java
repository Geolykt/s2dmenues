package de.geolykt.s2dmenues.components.gui;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SplitPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.github.tommyettinger.textra.TextraButton;

import de.geolykt.s2dmenues.S2DI18N;
import de.geolykt.s2dmenues.S2DI18N.ConfiguredTranslateable;
import de.geolykt.s2dmenues.S2DI18N.PlaceholderContext;
import de.geolykt.s2dmenues.S2DMenues;
import de.geolykt.s2dmenues.Styles;
import de.geolykt.s2dmenues.UIUtil;
import de.geolykt.s2dmenues.bridge.I18NCapable;
import de.geolykt.s2dmenues.bridge.MovingSpiralStarGenerator;
import de.geolykt.s2dmenues.bridge.ReflectionHacks;
import de.geolykt.s2dmenues.bridge.VelocityMovingStarGenerator;
import de.geolykt.s2dmenues.components.NOPActor;
import de.geolykt.s2dmenues.components.msdf.DynamicTextraLabel;
import de.geolykt.s2dmenues.components.msdf.RunnableTextraButton;
import de.geolykt.s2dmenues.incubator.StarPlacementGenerator;
import de.geolykt.s2dmenues.incubator.StarPlacementGeneratorCategory;
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

public class GenGalaxyWindow extends LAFAquaDialog implements Disposable, PlaceholderContext {

    private static enum SubDialog {
        ADJUSTMENT_METHODS,
        GALAXY_TYPE,
        GENERATOR_OPTIONS,
        NONE,
        SCENARIO_SOURCES,
        STARLANE_METHODS;
    }

    @NotNull
    public static final NamespacedKey PLACEHOLDER_KEY = NamespacedKey.fromString("s2dmenues", "dialogs.generate_galaxy");

    @NotNull
    private StarAdjustmentMethod adjustmentMethod = StarAdjustmentMethod.NORMAL;
    @NotNull
    private final Table contentTableUpper;
    @Nullable
    private StarPlacementGenerator currentGenerator;
    @NotNull
    private ScenarioSource currentScenarioSource = ProceduralScenarioSource.CLASSIC;
    @NotNull
    private SubDialog dialog = SubDialog.NONE;
    @NotNull
    private final TextraButton galaxyGenerateButton;
    @NotNull
    private final GalaxyPreviewWidget galaxyPreview;
    private int galaxySize;
    @NotNull
    private final TextraButton galaxySizeButton;
    @NotNull
    private final TextraButton galaxyTypeButton;
    @NotNull
    private MapData mapdata = new MapData(ProceduralStarGenerator.STRETCHED_SPIRAL);
    @NotNull
    private final SplitPane masterSplitPane;
    @NotNull
    private final TextraButton openGeneratorOptionsButton;
    @NotNull
    private final TextraButton scenarioButton;
    @NotNull
    private final TextraButton starAdjustmentsButton;
    @NotNull
    private StarlaneGenerator starlaneGenerator = Registry.STARLANE_GENERATORS.require(RegistryKeys.GALIMULATOR_STARLANES_STANDARD);
    @NotNull
    private final TextraButton starlaneGeneratorButton;

    public GenGalaxyWindow() {
        super("dialog.galgen.title");

        this.contentTableUpper = new Table();
        this.masterSplitPane = new SplitPane(this.contentTableUpper, null, true, Styles.getInstance().splitPaneStyle);

        this.galaxyGenerateButton = new RunnableTextraButton(this.translate("dialog.galgen.button.confirm"), Styles.getInstance().confirmButtonStyle, (button) -> {
            this.mapdata.setConnectionMethod((ConnectionMethod) this.starlaneGenerator);
            this.mapdata.setStarAdjustmentMethod(this.adjustmentMethod);
            this.mapdata.setScenarioSource(this.currentScenarioSource);
            Space.generateGalaxySync(this.galaxySize, this.mapdata);
            Drawing.setShownStage(null);
        });

        this.galaxySizeButton = UIUtil.createUnsignedIntInputButton(this.translate("dialog.galgen.button.size"), this::setGalaxySize);

        this.galaxyTypeButton = new RunnableTextraButton(this.translate("dialog.galgen.button.type"), Styles.getInstance().buttonStyle, (openGalaxyTypeSelectionButton, event) -> {
            if (S2DMenues.MOD_OPTION_REVISED_GALAXY_TYPE_SELECTION.get()) {
                this.onRevisedGeneratorSelection(event);
            } else {
                this.enableCurrentDialogButton();
                openGalaxyTypeSelectionButton.setDisabled(true);
                this.dialog = SubDialog.GALAXY_TYPE;

                this.onLegacyGeneratorSelection();
            }
        });

        this.starAdjustmentsButton = new RunnableTextraButton(this.translate("dialog.galgen.button.adjustments"), Styles.getInstance().buttonStyle, (starAdjustmentsButton) -> {
            this.enableCurrentDialogButton();
            starAdjustmentsButton.setDisabled(true);
            this.dialog = SubDialog.ADJUSTMENT_METHODS;
            TreeMap<String, Actor> buttons = new TreeMap<>();
            AtomicReference<Button> currentlyActiveButton = new AtomicReference<>();

            for (StarAdjustmentMethod adjustmentMethod : StarAdjustmentMethod.values()) {

                Supplier<@NotNull String> adjustmentMethodName;

                if (adjustmentMethod instanceof I18NCapable) {
                    adjustmentMethodName = ((I18NCapable) adjustmentMethod).s2dmenues$getLocalisation();
                } else {
                    adjustmentMethodName = S2DI18N.s2d("registries.adjustments.galimulator." + adjustmentMethod.name().toLowerCase(Locale.ROOT));
                }

                Button adjustmentButton = new RunnableTextraButton(adjustmentMethodName, Styles.getInstance().buttonStyle, (clickedOption) -> {
                    this.adjustmentMethod = adjustmentMethod;
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

                buttons.put(adjustmentMethodName.get(), adjustmentButton); // Key only required for stable order. Hence this is good enough most of the time

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

        this.starlaneGeneratorButton = new RunnableTextraButton(this.translate("dialog.galgen.button.starlanes"), Styles.getInstance().buttonStyle, (starlaneGenButton) -> {
            this.enableCurrentDialogButton();
            starlaneGenButton.setDisabled(true);
            this.dialog = SubDialog.STARLANE_METHODS;
            TreeMap<String, Actor> buttons = new TreeMap<>();
            AtomicReference<Button> currentlyActiveButton = new AtomicReference<>();

            for (StarlaneGenerator generator : Registry.STARLANE_GENERATORS.getValues()) {
                Supplier<@NotNull String> generatorName = S2DI18N.translate(generator);

                Button generatorButton = new RunnableTextraButton(generatorName, Styles.getInstance().buttonStyle, (clickedOption) -> {
                    this.starlaneGenerator = generator;
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

                buttons.put(generatorName.get(), generatorButton);

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

        this.scenarioButton = new RunnableTextraButton(this.translate("dialog.galgen.button.scenario"), Styles.getInstance().buttonStyle, (scenarioSourceButton) -> {
            this.enableCurrentDialogButton();
            scenarioSourceButton.setDisabled(true);
            this.dialog = SubDialog.SCENARIO_SOURCES;
            TreeMap<String, Actor> buttons = new TreeMap<>();
            AtomicReference<Button> currentlyActiveButton = new AtomicReference<>();
            List<Object> scenarios = new ArrayList<>();
            scenarios.addAll(Scenario.loadScenarios());

            // TODO the custom_empires scenario can be configured more throughly

            for (ScenarioSource scenario : ProceduralScenarioSource.values()) {
                scenarios.add(scenario);
            }

            for (Object scenario : scenarios) {
                ScenarioSource scenarioSource = (ScenarioSource) Objects.requireNonNull(scenario);

                Supplier<@NotNull String> scenarioName;

                if (scenarioSource instanceof I18NCapable) {
                    scenarioName = ((I18NCapable) scenarioSource).s2dmenues$getLocalisation();
                } else {
                    scenarioName = () -> Objects.toString(scenarioSource.getName());
                }

                Button scenarioButton = new RunnableTextraButton(scenarioName, Styles.getInstance().buttonStyle, (clickedOption) -> {
                    this.currentScenarioSource = scenarioSource;
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

                buttons.put(scenarioName.get(), scenarioButton); // Key is just to have a consistent order.

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

        this.galaxyPreview = new GalaxyPreviewWidget(this);
        this.openGeneratorOptionsButton = new RunnableTextraButton(this.translate("dialog.galgen.button.generator_options"), Styles.getInstance().buttonStyle, this::openGeneratorOptions);

        VerticalGroup options = new VerticalGroup();

        options.addActor(new NOPActor(15, 15));
        options.addActor(this.galaxyGenerateButton);
        options.addActor(this.openGeneratorOptionsButton);
        options.addActor(this.galaxyTypeButton);
        options.addActor(this.galaxySizeButton);
        options.addActor(this.starAdjustmentsButton);
        options.addActor(this.starlaneGeneratorButton);
        options.addActor(this.scenarioButton);
        options.addActor(new NOPActor(15, 15));

        this.getContentTable().add(this.masterSplitPane).center().left().grow();

        this.contentTableUpper.add(options).right().bottom().pad(8F);
        this.contentTableUpper.add(this.galaxyPreview).left().top().grow().pad(8F);

        // Ensure everything is updated (e.g. settings dialogs, etc.)
        this.setMapData(this.mapdata);
        this.setGalaxySize(5_000);
    }

    @Override
    @Contract(pure = true)
    @NotNull
    public String applyPlaceholder(@NotNull String key) {
        switch (key) {
        case "size":
            return Integer.toString(this.galaxySize);
        case "starlanes.generator":
            return S2DI18N.translate(this.starlaneGenerator).get();
        case "scenario.name":
            if (this.currentScenarioSource instanceof I18NCapable) {
                return ((I18NCapable) this.currentScenarioSource).s2dmenues$getLocalisation().get();
            } else {
                return Objects.toString(this.currentScenarioSource.getName());
            }
        case "adjustment":
            if (this.adjustmentMethod instanceof I18NCapable) {
                return ((I18NCapable) this.adjustmentMethod).s2dmenues$getLocalisation().get();
            } else {
                return S2DI18N.s2d("registries.adjustments.galimulator." + this.adjustmentMethod.name().toLowerCase(Locale.ROOT)).get();
            }
        case "generator.moving_planets.planets":
            return Integer.toString(ReflectionHacks.getPlanetaryStarGeneratorPlanetCount());
        case "generator.moving_generic.velocity":
            return Float.toString(((VelocityMovingStarGenerator) this.mapdata.getGenerator()).s2dmenues$getVelocity());
        case "generator.moving_spiral.core":
            return Float.toString(((MovingSpiralStarGenerator) this.mapdata.getGenerator()).s2dmenues$getCoreSize());
        case "generator.moving_spiral.fudge":
            return Float.toString(((MovingSpiralStarGenerator) this.mapdata.getGenerator()).s2dmenues$getOrbitalFudge());
        case "generator.moving_spiral.rotation":
            return Float.toString(((MovingSpiralStarGenerator) this.mapdata.getGenerator()).s2dmenues$getSpeed());
        case "generator.moving_spiral.undulation":
            return Float.toString(((MovingSpiralStarGenerator) this.mapdata.getGenerator()).s2dmenues$getUndulation());
        case "generator.fractal.seed":
            return Objects.toString(((FractalStarGenerator) this.mapdata.getGenerator()).seedString);
        case "generator.fractal.land":
            if (((FractalStarGenerator) this.mapdata.getGenerator()).drawLand) {
                return S2DI18N.s2d("boolean.generic_true").get();
            } else {
                return S2DI18N.s2d("boolean.generic_false").get();
            }
        case "generator.fractal.aspect_ratio": {
            String registryName = Objects.toString(((FractalStarGenerator) this.mapdata.getGenerator()).aspectRatio).toLowerCase(Locale.ROOT);
            return S2DI18N.s2d("registries.aspect_ratio.galimulator." + registryName).get();
        }
        case "generator.fractal.algorithm": {
            String registryName = Objects.toString(((FractalStarGenerator) this.mapdata.getGenerator()).landGenerator).toLowerCase(Locale.ROOT);
            return S2DI18N.s2d("registries.fractal_algo.galimulator." + registryName).get();
        }
        default:
            return "%UnknownKey:'" + key + "'%";
        }
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

    private void onLegacyGeneratorSelection() {
        // Display modal
        Collection<StarPlacementGenerator> generators = StarPlacementRegistry.GENERATOR_REGISTRY.valuesView();
        Table optionsTable = new Table();
        ScrollPane optionsScrolling = new ScrollPane(optionsTable, Styles.getInstance().scrollPaneStyle);
        NavigableMap<Map.Entry<String, @NotNull ConfiguredTranslateable>, Set<TextraButton>> mapButtons = new TreeMap<>((e1, e2) -> {
            return e1.getKey().compareTo(e2.getKey());
        });

        if (this.currentGenerator == null) {
            this.currentGenerator = StarPlacementRegistry.GENERATOR_REGISTRY.require(NamespacedKey.fromString("galimulator", "PLACEMENT_GENERATOR_STRETCHED_SPIRAL"));
        }

        AtomicReference<TextraButton> currentSelectedMapMode = new AtomicReference<>();
        for (StarPlacementGenerator map : generators) {
            TextraButton textButton = new RunnableTextraButton(map.s2dmenues$getLocalisation(), Styles.getInstance().buttonStyle, (mapButton) -> {
                this.currentGenerator = map;
                this.setMapData((MapData) map.toLegacyMap());
                currentSelectedMapMode.get().setDisabled(false);
                mapButton.setDisabled(true);
                currentSelectedMapMode.set(mapButton);
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

            ConfiguredTranslateable categoryNameTranslation = map.getCategory().s2dmenues$getLocalisation();
            Map.Entry<String, ConfiguredTranslateable> categoryKey = new SimpleImmutableEntry<>(categoryNameTranslation.get(), categoryNameTranslation);

            mapButtons.compute(categoryKey, (ignore, values) -> {
                if (values == null) {
                    values = new TreeSet<>((a1, a2) -> {
                        return a1.getText().toString().compareToIgnoreCase(a2.getText().toString());
                    });
                }
                values.add(textButton);
                return values;
            });
        }

        for (Map.Entry<Map.Entry<String, @NotNull ConfiguredTranslateable>, Set<TextraButton>> buttons : mapButtons.entrySet()) {
            HorizontalGroup options = new HorizontalGroup().wrap().fill();
            buttons.getValue().forEach(options::addActor);

            TextraButton categoryButton = new RunnableTextraButton(buttons.getKey().getValue(), Styles.getInstance().buttonStyle);
            optionsTable.add(categoryButton).left().growX().row();
            optionsTable.add(options).left().growX().row();
        }

        this.masterSplitPane.setSecondWidget(optionsScrolling);
        if (this.masterSplitPane.getSplitAmount() > 0.5F) {
            this.masterSplitPane.setSplitAmount(0.5F);
        }
    }

    private void onRevisedGeneratorSelection(@NotNull InputEvent event) {
        Map<@NotNull StarPlacementGeneratorCategory, Set<@NotNull StarPlacementGenerator>> categories = new HashMap<>();

        for (StarPlacementGenerator generator : StarPlacementRegistry.GENERATOR_REGISTRY.valuesView()) {
            categories.compute(generator.getCategory(), (var10001, collection) -> {
                if (collection == null) {
                    collection = new HashSet<>();
                }

                collection.add(generator);
                return collection;
            });
        }

        UIUtil.showSelectionWindow(event, categories.keySet(), (category, e2) -> {
            UIUtil.showSelectionWindow(e2, Objects.requireNonNull(categories.get(category), "no generator under category"), (generator, e3) -> {
                this.currentGenerator = generator;
                this.setMapData((MapData) generator.toLegacyMap());

                if (this.dialog == SubDialog.GENERATOR_OPTIONS) {
                    this.openGeneratorOptions(); // Force refresh of generator options
                }
            }, Align.left);
        }, Align.left);
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

            TextraButton fractalAlgoHeader = new RunnableTextraButton(this.translate("dialog.galgen.button.fractal.algorithm"), Styles.getInstance().buttonStyle, () -> {
                // NOP
            });

            HorizontalGroup fractalAlgoOptions = new HorizontalGroup().wrap(true);
            RunnableTextraButton[] algorithmButtons = new RunnableTextraButton[FractalStarGenerator.LandGenerator.values().length];
            AtomicReference<Button> currentActiveButton = new AtomicReference<>();

            for (FractalStarGenerator.LandGenerator algo : FractalStarGenerator.LandGenerator.values()) {
                String registryName = Objects.toString(algo).toLowerCase(Locale.ROOT);
                RunnableTextraButton algorithmButton = new RunnableTextraButton(S2DI18N.s2d("registries.fractal_algo.galimulator." + registryName), Styles.getInstance().buttonStyle, (clickedButton) -> {
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

            TextraButton aspectRatioHeader = new RunnableTextraButton(this.translate("dialog.galgen.button.fractal.aspect_ratio"), Styles.getInstance().buttonStyle, () -> {
                // NOP
            });

            HorizontalGroup aspectRatioGroup = new HorizontalGroup().wrap(true);
            TextraButton[] aspectRatioButtons = new TextraButton[FractalStarGenerator.AspectRatio.values().length];

            for (FractalStarGenerator.AspectRatio aspectRatio : FractalStarGenerator.AspectRatio.values()) {
                String registryName = Objects.toString(aspectRatio).toLowerCase(Locale.ROOT);
                aspectRatioButtons[aspectRatio.ordinal()] = new RunnableTextraButton(S2DI18N.s2d("registries.aspect_ratio.galimulator." + registryName), Styles.getInstance().buttonStyle, (clickedButton) -> {
                    aspectRatioButtons[fsg.aspectRatio.ordinal()].setDisabled(false);
                    clickedButton.setDisabled(true);
                    fsg.a(aspectRatio);
                    this.galaxyPreview.reset();
                });

                if (fsg.aspectRatio == aspectRatio) {
                    aspectRatioButtons[aspectRatio.ordinal()].setDisabled(true);
                }
            }

            for (TextraButton button : aspectRatioButtons) {
                aspectRatioGroup.addActor(button);
            }

            optionTable.add(aspectRatioHeader).left().growX().row();
            optionTable.add(aspectRatioGroup).growX().row();

            TextraButton setSeedButton = UIUtil.createTextInputButton(this.translate("dialog.galgen.button.fractal.seed"), () -> Objects.toString(fsg.seedString), seed -> {
                fsg.seedString = seed;
                this.galaxyPreview.reset();
                fsg.generateMap();
            });

            TextraButton drawLandButton = new RunnableTextraButton(this.translate("dialog.galgen.button.fractal.land"), Styles.getInstance().buttonStyle, () -> {
                fsg.drawLand = !fsg.drawLand;
            });

            HorizontalGroup otherButtons = new HorizontalGroup();
            otherButtons.addActor(setSeedButton);
            otherButtons.addActor(drawLandButton);
            optionTable.add(otherButtons).row();

            ScrollPane scrollPane = new ScrollPane(optionTable, Styles.getInstance().scrollPaneStyle);
            this.masterSplitPane.setSecondWidget(scrollPane);
            this.getStage().setScrollFocus(optionTable);
        } else if (generator instanceof VelocityMovingStarGenerator) {
            VelocityMovingStarGenerator vmg = (VelocityMovingStarGenerator) generator;
            TextraButton setSpeedButton = UIUtil.createFloatInputButton(this.translate("dialog.galgen.button.moving_generic.velocity"), (velocity) -> {
                vmg.s2dmenues$setVelocity(velocity);
                this.galaxyPreview.reset();
            });
            this.masterSplitPane.setSecondWidget(setSpeedButton);
            this.masterSplitPane.setSplitAmount(0.9F);
        } else if (generator == ProceduralStarGenerator.MOVING_PLANETS) {
            TextraButton setPlanetCountButton = UIUtil.createUnsignedIntInputButton(this.translate("dialog.galgen.button.moving_planets.planets"), (planetCount) -> {
                if (planetCount <= 0) {
                    planetCount = 1;
                }
                if (!ReflectionHacks.setPlanetaryStarGeneratorPlanetCount(planetCount)) {
                    LAFAquaDialog noticeDialog = new LAFAquaDialog("legacylaf.uiutil.error");
                    noticeDialog.getContentTable().add(new DynamicTextraLabel(S2DI18N.s2d("dialog.galgen.error.reflection"))).pad(10);
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

            table.add(UIUtil.createFloatInputButton(this.translate("dialog.galgen.button.moving_spiral.core"), (value) -> {
                movingSpiralGenerator.s2dmenues$setCoreSize(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            table.add(UIUtil.createFloatInputButton(this.translate("dialog.galgen.button.moving_spiral.fudge"), (value) -> {
                movingSpiralGenerator.s2dmenues$setOrbitalFudge(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            table.add(UIUtil.createFloatInputButton(this.translate("dialog.galgen.button.moving_spiral.rotation"), (value) -> {
                movingSpiralGenerator.s2dmenues$setSpeed(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            table.add(UIUtil.createFloatInputButton(this.translate("dialog.galgen.button.moving_spiral.undulation"), (value) -> {
                movingSpiralGenerator.s2dmenues$setUndulation(value);
                this.galaxyPreview.reset();
            })).left().growX();

            table.row();

            ScrollPane scrollPane = new ScrollPane(table, Styles.getInstance().scrollPaneStyle);
            this.masterSplitPane.setSecondWidget(scrollPane);
            this.masterSplitPane.setSplitAmount(0.73F);
            this.getStage().setScrollFocus(table);
        }
    }

    @NotNull
    public GenGalaxyWindow setGalaxySize(int size) {
        this.galaxySize = size;
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
        return this;
    }

    @NotNull
    @Contract(pure = true)
    protected ConfiguredTranslateable translate(@NotNull String key) {
        return S2DI18N.s2d(key).withContext(GenGalaxyWindow.PLACEHOLDER_KEY, this);
    }
}
