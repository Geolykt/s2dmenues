package de.geolykt.s2dmenues.cjk.harfbuzz;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.util.harfbuzz.HarfBuzz;

public class HarfBuzzLanguage {

    private final long address;

    protected HarfBuzzLanguage(long address) {
        this.address = address;
    }

    /**
     * Create a new HarfBuzz language from a given IETF BCP 47 language tag.
     *
     * <p>This instance does not need to be disposed; the callee is responsible for maintaining the underlying data.
     *
     * @param language A BCP 47 language tag
     */
    public HarfBuzzLanguage(@NotNull CharSequence language) {
        this(HarfBuzz.hb_language_from_string(language));
    }

    public HarfBuzzLanguage(@NotNull Locale locale) {
        this(locale.toLanguageTag());
    }

    public long getAddress() {
        return this.address;
    }
}
