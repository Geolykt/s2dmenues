package de.geolykt.s2dmenues;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.empire.StarlaneGenerator;
import de.geolykt.starloader.util.JavaInterop;

public final class S2DI18N {

    public static class ConfiguredTranslateable implements Supplier<@NotNull String> {
        @NotNull
        private final Map<NamespacedKey, PlaceholderContext> placeholderContexts = new HashMap<>();
        @NotNull
        private final NamespacedKey translationkey;
        @Nullable
        private Supplier<@NotNull String> defaultValue = null;

        @Contract(pure = true)
        protected ConfiguredTranslateable(@NotNull NamespacedKey translationKey) {
            this.translationkey = translationKey;
        }

        @NotNull
        @Contract(mutates = "this", pure = false, value = "null, _ -> fail; _, null -> fail; !null, !null -> this")
        public ConfiguredTranslateable withContext(@NotNull NamespacedKey nsKey, @NotNull PlaceholderContext placeholders) {
            this.placeholderContexts.put(Objects.requireNonNull(nsKey, "'nsKey' may not be null"), Objects.requireNonNull(placeholders, "'placeholders' may not be null"));
            return this;
        }

        @Override
        @NotNull
        @Contract(pure = true)
        public String get() {
            String value = S2DI18N.activeTranslation.get(this.translationkey);

            if (value == null) {
                Supplier<@NotNull String> defaultValue = this.defaultValue;

                if (defaultValue == null) {
                    return this.translationkey.getNamespace() + ":" + this.translationkey.getKey();
                }

                value = Objects.requireNonNull(defaultValue.get(), () -> ("Default value provider returned null for " + this.translationkey));
            }

            int percentIndex = value.indexOf('%');

            if (percentIndex < 0) {
                // No placeholders. Not further to do.
                return value;
            }

            StringBuilder builder = new StringBuilder(value.length());

            int priorIndex = 0;

            surrogateLoop:
            do {
                builder.append(value, priorIndex, percentIndex);

                if (value.codePointAt(percentIndex + 1) == '%') {
                    // '%' escape symbol
                    builder.append('%');
                    priorIndex = percentIndex + 1;
                    percentIndex = value.indexOf('%', priorIndex);
                } else {
                    // Placeholder replacement logic
                    int colonIndex = value.indexOf(':', percentIndex);

                    if (colonIndex < 0) {
                        throw new IllegalStateException("Cannot parse placeholders in string: '" + value + "': Colon missing. '%' symbols can be escaped through \"%%\".");
                    }

                    String namespace = value.substring(percentIndex + 1, colonIndex);

                    int dotIndex = value.indexOf('.', colonIndex);

                    while (dotIndex >= 0) {
                        String key = value.substring(colonIndex + 1, dotIndex);
                        NamespacedKey nsKey = NamespacedKey.fromString(namespace, key);
                        PlaceholderContext context = this.placeholderContexts.get(nsKey);

                        if (context != null) {
                            percentIndex = value.indexOf('%', dotIndex);

                            if (percentIndex < 0) {
                                throw new IllegalStateException("Cannot parse placeholders in string: '" + value + "': No closing '%'.");
                            }

                            builder.append(context.applyPlaceholder(value.substring(dotIndex + 1, percentIndex)));
                            priorIndex = percentIndex + 1;
                            percentIndex = value.indexOf('%', priorIndex);

                            continue surrogateLoop;
                        } else {
                            dotIndex = value.indexOf('.', dotIndex + 1);
                        }
                    }

                    throw new IllegalStateException("Cannot parse placeholders in string: '" + value + "': Placeholder context missing. '%' symbols can be escaped through \"%%\".");
                }
            } while (percentIndex >= 0);

            builder.append(value, priorIndex, value.length());

            return builder.toString();
        }

        @NotNull
        @Contract(mutates = "this", pure = false, value = "_ -> this")
        public ConfiguredTranslateable withDefault(@Nullable Supplier<@NotNull String> defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
    }

    public static interface PlaceholderContext {
        @Contract(pure = true)
        @NotNull
        public String applyPlaceholder(@NotNull String key);
    }

    @NotNull
    private static Locale activeLocale = Locale.ENGLISH;

    @NotNull
    @Unmodifiable
    private static Map<NamespacedKey, String> activeTranslation = Collections.emptyMap();

    private static final Map<@NotNull Locale, @NotNull Map<NamespacedKey, String>> LANGUAGES = new TreeMap<>((localeA, localeB) -> {
        return localeA.getDisplayName().compareTo(localeB.getDisplayName());
    });

    @NotNull
    public static Locale getActiveLocale() {
        return S2DI18N.activeLocale;
    }

    @NotNull
    public static Iterable<@NotNull Locale> getAvailableLocales() {
        return S2DI18N.LANGUAGES.keySet();
    }

    private static void loadBuiltinLanguage(@NotNull String name) throws IOException {
        try (InputStream in = S2DI18N.class.getClassLoader().getResourceAsStream("" + name + ".json")) {
            if (in == null) {
                throw new IOException("Resource '/" + name + ".json' couldn't be found in classloader '" + JavaInterop.getClassloaderName(S2DI18N.class.getClassLoader()) + "'.");
            }

            S2DI18N.loadLanguage(Locale.forLanguageTag(name), new JSONObject(new String(JavaInterop.readAllBytes(in), StandardCharsets.UTF_8)));
        }
    }

    public static void loadLanguage(@NotNull Locale language, @NotNull JSONObject translations) {
        Map<NamespacedKey, String> translationMappings = S2DI18N.LANGUAGES.getOrDefault(language, new HashMap<>());

        for (String keyName : translations.keySet()) {
            translationMappings.put(NamespacedKey.fromString(keyName), translations.getString(keyName));
        }

        S2DI18N.LANGUAGES.put(language, translationMappings);
    }

    @NotNull
    @Contract(pure = true)
    public static ConfiguredTranslateable s2d(@NotNull String key) {
        return S2DI18N.translate(NamespacedKey.fromString("s2dmenues", key));
    }

    private static void saveConfig() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("locale", S2DI18N.activeLocale.toLanguageTag());

        try {
            Files.write(S2DMenues.MOD_DATA_DIR.resolve("i18n.json"), jsonObject.toString(2).getBytes(StandardCharsets.UTF_8), StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (JSONException | IOException e) {
            LoggerFactory.getLogger(S2DI18N.class).error("Unable to save I18N configuration file.", e);
        }
    }

    public static void setActiveLocale(@NotNull Locale activeLocale) {
        S2DI18N.activeLocale = Objects.requireNonNull(activeLocale, "'activeLocale' may not be null.");
        S2DI18N.activeTranslation = Collections.unmodifiableMap(S2DI18N.LANGUAGES.getOrDefault(S2DI18N.activeLocale, Collections.emptyMap()));
        S2DI18N.saveConfig();
    }

    static void start() throws IOException {
        // Load configuration
        Path configFile = S2DMenues.MOD_DATA_DIR.resolve("i18n.json");
        if (Files.exists(configFile)) {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8));
            } catch (IOException e) {
                LoggerFactory.getLogger(S2DI18N.class).warn("Unable to read localisation configuration file. Default are used instead.", e);
                jsonObject = new JSONObject();
            }

            String language = jsonObject.optString("locale", null);

            if (language != null) {
                S2DI18N.activeLocale = Locale.forLanguageTag(language);
            }
        }

        Path languageDir = S2DMenues.MOD_DATA_DIR.resolve("languages");
        (Files.notExists(languageDir) ? Stream.<@NotNull Path>empty() : Files.list(languageDir)).filter(path -> {
            return path.getFileName().toString().endsWith(".json");
        }).map(path -> {
            try {
                return new AbstractMap.SimpleImmutableEntry<>(path, new JSONObject(new String(Files.readAllBytes(path), StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read contents of path: " + path, e);
            } catch (JSONException e) {
                throw new IllegalStateException("Failed to parse JSON contents of path: " + path, e);
            }
        }).forEach(entry -> {
            String languageTag = entry.getKey().getFileName().toString();
            languageTag = languageTag.substring(0, languageTag.lastIndexOf('.'));
            Locale locale = Locale.forLanguageTag(languageTag);
            S2DI18N.loadLanguage(locale, entry.getValue());
        });

        S2DI18N.loadBuiltinLanguage("de-DE");
        S2DI18N.loadBuiltinLanguage("en");

        S2DI18N.activeTranslation = Collections.unmodifiableMap(S2DI18N.LANGUAGES.getOrDefault(S2DI18N.activeLocale, Collections.emptyMap()));
    }

    @NotNull
    @Contract(pure = true)
    public static ConfiguredTranslateable translate(@NotNull NamespacedKey key) {
        return new ConfiguredTranslateable(key);
    }

    @NotNull
    @Contract(pure = true)
    public static ConfiguredTranslateable translate(@NotNull StarlaneGenerator generator) {
        String generatorKey = Objects.requireNonNull(generator, "'generator' may not be null").getRegistryKey().toString().toLowerCase(Locale.ROOT);
        String key = "registries.starlanes." + generatorKey;

        return S2DI18N.s2d(key).withDefault(generator::getDisplayName);
    }

    private S2DI18N() {
        throw new UnsupportedOperationException("This class is stateless!");
    }
}
