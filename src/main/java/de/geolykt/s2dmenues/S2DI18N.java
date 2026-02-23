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
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.util.JavaInterop;

public final class S2DI18N {
    private S2DI18N() {
        throw new UnsupportedOperationException("This class is stateless!");
    }

    private static final Map<@NotNull Locale, @NotNull Map<NamespacedKey, String>> LANGUAGES = new TreeMap<>((localeA, localeB) -> {
        return localeA.getDisplayName().compareTo(localeB.getDisplayName());
    });

    @NotNull
    @Unmodifiable
    private static Map<NamespacedKey, String> activeTranslation = Collections.emptyMap();

    @NotNull
    private static Locale activeLocale = Locale.UK;

    @NotNull
    public static String translate(@NotNull NamespacedKey key) {
        String value = S2DI18N.activeTranslation.get(key);

        if (value == null) {
            return key.getNamespace() + ":" + key.getKey();
        }

        return value;
    }

    @NotNull
    public static Locale getActiveLocale() {
        return S2DI18N.activeLocale;
    }

    public static void setActiveLocale(@NotNull Locale activeLocale) {
        S2DI18N.activeLocale = Objects.requireNonNull(activeLocale, "'activeLocale' may not be null.");
        S2DI18N.saveConfig();
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

    @NotNull
    public static Iterable<@NotNull Locale> getAvailableLocales() {
        return S2DI18N.LANGUAGES.keySet();
    }

    public static void loadLanguage(@NotNull Locale language, @NotNull JSONObject translations) {
        Map<NamespacedKey, String> translationMappings = new HashMap<>();

        for (String keyName : translations.keySet()) {
            translationMappings.put(NamespacedKey.fromString(keyName), translations.getString(keyName));
        }

        S2DI18N.LANGUAGES.put(language, translationMappings);
    }

    private static void loadBuiltinLanguage(@NotNull String name) throws IOException {
        try (InputStream in = S2DI18N.class.getClassLoader().getResourceAsStream("" + name + ".json")) {
            if (in == null) {
                throw new IOException("Resource '/" + name + ".json' couldn't be found in classloader '" + JavaInterop.getClassloaderName(S2DI18N.class.getClassLoader()) + "'.");
            }

            S2DI18N.loadLanguage(Locale.forLanguageTag(name), new JSONObject(new String(JavaInterop.readAllBytes(in), StandardCharsets.UTF_8)));
        }
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
        S2DI18N.loadBuiltinLanguage("en-GB");

        S2DI18N.activeTranslation = Collections.unmodifiableMap(S2DI18N.LANGUAGES.getOrDefault(S2DI18N.activeLocale, Collections.emptyMap()));
    }
}
