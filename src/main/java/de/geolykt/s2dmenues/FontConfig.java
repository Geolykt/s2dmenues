package de.geolykt.s2dmenues;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.github.tommyettinger.textra.Font;

import de.geolykt.starloader.api.Galimulator;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackedEvent;
import de.geolykt.starloader.api.event.lifecycle.AtlasPackingEvent;
import de.geolykt.starloader.api.gui.Drawing;
import de.geolykt.starloader.api.resource.NIOFileHandle;

public class FontConfig {

    private static class BoxedPath {
        @NotNull
        private final Path innerPath;
        @Nullable
        private final Path outerPath;

        public BoxedPath(@Nullable Path outer, @NotNull Path inner) {
            this.outerPath = outer;
            this.innerPath = Objects.requireNonNull(inner, "'inner' may not be null");
        }

        @Override
        public String toString() {
            Path outer = this.outerPath;

            if (outer == null) {
                return this.innerPath.toString();
            } else {
                return outer.toString() + "!" + this.innerPath.toString();
            }
        }
    }

    private static class FontEager implements FontPrimitive {
        @NotNull
        private final Font font;

        public FontEager(@NotNull Font font) {
            this.font = Objects.requireNonNull(font, "'font' may not be null");
        }

        @Override
        public void close() {
            // There is nothing we can do.
        }

        @Override
        @NotNull
        public String getName() {
            return this.font.getName();
        }

        @Override
        @NotNull
        public Font getTextraFont() {
            return this.font;
        }
    }

    private static class FontLazy implements FontPrimitive {
        private final boolean closeFS;

        @Nullable
        private Texture disposeTexture;

        @Nullable
        private final Path filesystemContainerPath;

        private boolean hugeFont;

        @NotNull
        private final Path jsonPath;

        @NotNull
        private final Path pngPath;

        @Nullable
        private Font textraFont;

        @NotNull
        private final String textureName;

        public FontLazy(@Nullable Path filesystemContainerPath, @NotNull Path jsonPath, @NotNull Path pngPath, @NotNull String textureName, boolean closeFS) {
            this.filesystemContainerPath = filesystemContainerPath;
            this.jsonPath = Objects.requireNonNull(jsonPath, "'jsonPath' may not be null");
            this.pngPath = Objects.requireNonNull(pngPath, "'pngPath' may not be null");
            this.textureName = Objects.requireNonNull(textureName, "'textureName' may not be null");
            this.closeFS = closeFS;

            if (Files.notExists(this.jsonPath)) {
                throw new IllegalArgumentException("Argument 'jsonPath' points to non-existing file");
            } else if (Files.notExists(this.pngPath)) {
                throw new IllegalArgumentException("Argument 'pngPath' points to non-existing file");
            } else if (filesystemContainerPath == null && closeFS) {
                throw new IllegalArgumentException("'filesystemContainerPath' may not be null if 'closeFS' is true.");
            }
        }

        public void bakeFont(@NotNull AtlasPackedEvent evt) throws IOException {
            if (this.textraFont != null) {
                throw new IllegalStateException("#submitFontTextures called twice on object");
            } else if (this.hugeFont) {
                return; // Huge fonts get loaded lazily
            }

            AtlasRegion region = evt.getAtlas().findRegion(this.textureName);

            if (region == null) {
                throw new IllegalStateException("No atlas region under given name: '" + this.textureName  + "'.");
            }

            this.textraFont = new Font(new NIOFileHandle(this.jsonPath), region, true);
            this.textraFont.scaleHeightTo(24F);
        }

        @Override
        public void close() {
            if (!this.hugeFont) {
                // Non-huge fonts are always loaded in the main texture atlas. Inefficient but w/e. Memory will be cheap again in 2030, hopefully.
                // For reference, at the time of writing, 64 GB of DDR4 memory costs roughly 600 EUR.
                return;
            }

            Galimulator.runTaskOnNextFrame(() -> {
                Galimulator.runTaskOnNextFrame(() -> {

                    Font fnt = this.textraFont;
                    if (fnt != null) {
                        this.textraFont = null;
                        fnt.dispose();
                    }

                    Texture tex = this.disposeTexture;
                    if (tex != null) {
                        tex.dispose();
                    }
                });
                if (!Boolean.getBoolean("de.geolykt.s2dmenues.forceClearBuffers")) {
                    return;
                }
                // FIXME code crashes still

            });
        }

        @Override
        @NotNull
        public String getName() {
            Font fnt = this.textraFont;
            if (fnt != null) {
                return fnt.getName();
            }

            String name = this.jsonPath.getFileName().toString();

            return name.substring(0, name.indexOf('.'));
        }

        @Override
        @NotNull
        public Font getTextraFont() {
            Font font = this.textraFont;

            if (font == null) {
                if (!this.hugeFont) {
                    throw new IllegalStateException("#getFont called before #bakeFont. Did the AtlasPackedEvent fire yet?");
                }

                synchronized (this) {
                    if ((font = this.textraFont) != null) {
                        return font;
                    }

                    if (this.closeFS) {
                        // Reopen closed filesystem
                        Path fsContainerPath = this.filesystemContainerPath;
                        if (fsContainerPath == null) {
                            throw new AssertionError("'fsContainerPath' shouldn't be null here.");
                        }

                        try {
                            FileSystem fs = FileSystems.newFileSystem(fsContainerPath, (ClassLoader) null);
                            Path pngPath = fs.getPath(this.pngPath.toString());
                            Path jsonPath = fs.getPath(this.jsonPath.toString());

                            Texture texture = new Texture(new NIOFileHandle(pngPath));
                            TextureRegion region = new TextureRegion(texture);
                            this.textraFont = font = new Font(new NIOFileHandle(jsonPath), region, true);
                            this.textraFont.scaleHeightTo(24F);
                            this.disposeTexture = texture;

                            fs.close();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    } else {
                        Texture texture = new Texture(new NIOFileHandle(this.pngPath));
                        TextureRegion region = new TextureRegion(texture);
                        this.textraFont = font = new Font(new NIOFileHandle(this.jsonPath), region, true);
                        this.disposeTexture = texture;
                    }
                }
            }

            return font;
        }

        public void packFontTextures(@NotNull AtlasPackingEvent evt) throws IOException {
            if (this.textraFont != null) {
                throw new IllegalStateException("#packFontTextures called after #bakeFont on object");
            }

            byte[] pngData = Files.readAllBytes(this.pngPath);
            Pixmap pixmap = new Pixmap(pngData, 0, pngData.length);
            LoggerFactory.getLogger(FontConfig.class).info("Pixmap data for {}: {} {}x{}@{}", this.textureName, pixmap, pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());

            if (pixmap.getWidth() <= 2048 || pixmap.getHeight() <= 2048) {
                evt.packTexture(this.textureName, pixmap);
                this.hugeFont = false;
            } else {
                this.hugeFont = true;
            }

            pixmap.dispose();
        }
    }

    public static interface FontPrimitive extends Closeable {
        @Override
        void close();

        @NotNull
        String getName();

        @NotNull
        Font getTextraFont();
    }

    @Nullable
    private static FontConfig instance;

    @NotNull
    @Contract(pure = true)
    public static FontConfig getInstance() {
        FontConfig instance = FontConfig.instance;
        if (instance == null) {
            throw new IllegalStateException("Instance not yet started. This method may only be called after the AtlasPackingEvent was fired.");
        }
        return instance;
    }

    static void start(@NotNull Path modDataDir) throws IOException {
        if (FontConfig.instance != null) {
            throw new IllegalStateException("Instance already started.");
        }

        FontConfig.instance = new FontConfig(modDataDir);
    }

    private boolean discoveredBuiltins = false;

    @NotNull
    private final Path fontConfigFile;

    @NotNull
    private final Map<String, @NotNull FontPrimitive> fontNameLookup = new TreeMap<>();

    @Nullable
    private FontPrimitive preferredFont;

    @NotNull
    private String preferredFontName = "Built-in font 'SPACE'";

    private FontConfig(@NotNull Path modDataDir) throws IOException {
        Path fontDir = modDataDir.resolve("fonts");

        this.fontConfigFile = fontDir.resolve("config.json");

        if (!Files.notExists(this.fontConfigFile)) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(new String(Files.readAllBytes(this.fontConfigFile), StandardCharsets.UTF_8));
            } catch (JSONException | IOException e) {
                LoggerFactory.getLogger(FontConfig.class).error("Unable to load currently present configuration file. Using default configurations instead.", e);
                jsonObject = new JSONObject();
            }

            String preferredFontName = jsonObject.optString("preferredFont", null);

            if (preferredFontName != null) {
                this.preferredFontName = preferredFontName;
            }
        }

        if (Files.exists(fontDir)) {
            Files.list(fontDir).flatMap(path -> {
                if (path.getFileName().toString().endsWith(".jar")) {
                    try {
                        // Cast in next line is necessary to compile with newer Java versions
                        FileSystem fs = FileSystems.newFileSystem(path, (ClassLoader) null);
                        List<@NotNull Path> rootDirectories = new ArrayList<>();

                        for (Path rootDir : fs.getRootDirectories()) {
                            if (rootDir == null) {
                                throw new AssertionError();
                            }

                            rootDirectories.add(rootDir);
                        }

                        return rootDirectories.stream().flatMap(innerRoot -> {
                            try {
                                return Files.list(innerRoot);
                            } catch (IOException e) {
                                throw new UncheckedIOException(e);
                            }
                        }).map(innerPath -> {
                            return new BoxedPath(path, innerPath);
                        });
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                } else if (this.fontConfigFile.equals(path)) {
                    return Stream.empty();
                } else {
                    return Stream.of(new BoxedPath(null, path));
                }
            }).filter(boxedPath -> {
                return boxedPath.innerPath.getFileName().toString().endsWith(".json") && Files.exists(boxedPath.innerPath);
            }).map(jsonPath -> {
                String jsonFileName = jsonPath.innerPath.getFileName().toString();
                Path pngInnerPath = jsonPath.innerPath.resolveSibling(jsonFileName.substring(0, jsonFileName.length() - 4) + "png");
                BoxedPath pngPath = new BoxedPath(jsonPath.outerPath, pngInnerPath);
                return new AbstractMap.SimpleImmutableEntry<>(jsonPath, pngPath);
            }).filter(entry ->  {
                return Files.exists(entry.getValue().innerPath);
            }).map(pair -> {
                Path fsPath = pair.getValue().outerPath;
                String textureName;
                if (fsPath == null) {
                    textureName = pair.getValue().innerPath.toString();
                } else {
                    textureName = fsPath.toString() + "!" + pair.getValue().innerPath.toString();
                }

                return new FontLazy(fsPath, pair.getKey().innerPath, pair.getValue().innerPath, textureName, fsPath != null);
            }).forEach(font -> {
                this.fontNameLookup.put(font.getName(), font);
                if (this.preferredFontName.equals(font.getName())) {
                    this.preferredFont = font;
                }
            });

        } else {
            LoggerFactory.getLogger(FontConfig.class).warn("Directory '{}' does not exist. Custom fonts will not be loaded.", fontDir);
        }
    }

    void bakeFonts(@NotNull AtlasPackedEvent evt) throws IOException {
        for (FontPrimitive font : this.fontNameLookup.values()) {
            if (font instanceof FontLazy) {
                ((FontLazy) font).bakeFont(evt);
            }
        }

        for (FontPrimitive font : this.fontNameLookup.values()) {
            if (font instanceof FontLazy && ((FontLazy) font).closeFS) {
                ((FontLazy) font).jsonPath.getFileSystem().close();
            }
        }
    }

    @NotNull
    public Iterable<@NotNull FontPrimitive> getAvailableFonts() {
        return Objects.requireNonNull(this.fontNameLookup.values());
    }

    @NotNull
    public Font getPreferredFont() {
        return this.getPreferredFontPrimitive().getTextraFont();
    }

    @NotNull
    public FontPrimitive getPreferredFontPrimitive() {
        if (!this.discoveredBuiltins) {
            for (String fntName : Drawing.getFonts()) {
                if (fntName == null) {
                    throw new AssertionError();
                }

                BitmapFont bmf = Drawing.getFontBitmap(fntName);

                if (bmf == null) {
                    LoggerFactory.getLogger(FontConfig.class).warn("No font could be found under given name '{}', even though it is a registered font name.", fntName);
                    continue;
                }

                Font font = new Font(bmf);
                font.setName("Built-in font '" + fntName + "'");

                FontPrimitive fontPrimitive = new FontEager(font);

                this.fontNameLookup.put(font.getName(), fontPrimitive);

                if (this.preferredFontName.equals(font.getName())) {
                    this.preferredFont = fontPrimitive;
                }
            }

            this.discoveredBuiltins = true;
        }

        FontPrimitive font = this.preferredFont;

        if (font == null) {
            LoggerFactory.getLogger(FontConfig.class).warn("Unable to find font with name \"" + this.preferredFontName + "\". Falling back...");
            this.preferredFont = font = Objects.requireNonNull(this.fontNameLookup.get("Built-in font 'SPACE'"));
        }

        return font;
    }

    void registerTextures(@NotNull AtlasPackingEvent evt) throws IOException {
        for (FontPrimitive font : this.fontNameLookup.values()) {
            if (font instanceof FontLazy) {
                ((FontLazy) font).packFontTextures(evt);
            }
        }
    }

    private synchronized void saveConfig() {
        JSONObject jsonObject;

        if (Files.notExists(this.fontConfigFile)) {
            jsonObject = new JSONObject();
        } else {
            try {
                jsonObject = new JSONObject(new String(Files.readAllBytes(this.fontConfigFile), StandardCharsets.UTF_8));
            } catch (JSONException | IOException e) {
                LoggerFactory.getLogger(FontConfig.class).error("Unable to load currently present configuration file. Configuration might get overwritten.", e);
                jsonObject = new JSONObject();
            }
        }

        jsonObject.put("preferredFont", this.preferredFontName);

        try {
            Files.write(this.fontConfigFile, jsonObject.toString(2).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (JSONException | IOException e) {
            LoggerFactory.getLogger(FontConfig.class).error("Unable to write to font configuration file.", e);
        }
    }

    public void setPreferredFont(@NotNull FontPrimitive font) {
        FontPrimitive previousActive = this.preferredFont;

        if (previousActive == font) {
            return;
        }

        this.preferredFontName = Objects.requireNonNull(Objects.requireNonNull(font, "'font' may not be null").getName(), "'font.getName()' may not return null");
        this.preferredFont = font;

        this.saveConfig();

        if (previousActive != null) {
            previousActive.close(); // Optionally dispose unused resources
        }
    }
}
