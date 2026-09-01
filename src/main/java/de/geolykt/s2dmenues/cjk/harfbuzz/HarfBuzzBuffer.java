package de.geolykt.s2dmenues.cjk.harfbuzz;

import java.io.Closeable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.harfbuzz.HarfBuzz;
import org.lwjgl.util.harfbuzz.hb_glyph_info_t;
import org.lwjgl.util.harfbuzz.hb_glyph_position_t;
import org.stianloader.micromixin.transform.internal.util.Objects;

import com.badlogic.gdx.utils.Disposable;

public class HarfBuzzBuffer implements AutoCloseable, Closeable, Disposable {

    public static enum ContentType {
        /**
         * A buffer that contains shaped outputs.
         *
         * <p>This is the value after {@link HarfBuzzBuffer#shape(HarfbuzzFont)} was called.
         * When reusing a buffer, {@link #UNICODE} might need to be set again manually.
         */
        GLYPH(HarfBuzz.HB_BUFFER_CONTENT_TYPE_GLYPHS),

        /**
         * The initial content type of a buffer.
         */
        INVALID(HarfBuzz.HB_BUFFER_CONTENT_TYPE_INVALID),

        /**
         * A buffer that contains unshaped unicode codepoints.
         *
         * <p>Set automatically from the {@link HarfBuzzBuffer#addText(CharSequence, int, int)}
         * and {@link HarfBuzzBuffer#addText(CharSequence)} methods.
         */
        UNICODE(HarfBuzz.HB_BUFFER_CONTENT_TYPE_UNICODE);

        private final int cEnumValue;

        private ContentType(int cEnumValue) {
            this.cEnumValue = cEnumValue;
        }
    }

    public static enum TextDirection {
        BOTTOM_TO_TOP(HarfBuzz.HB_DIRECTION_BTT),
        LEFT_TO_RIGHT(HarfBuzz.HB_DIRECTION_LTR),
        RIGHT_TO_LEFT(HarfBuzz.HB_DIRECTION_RTL),
        TOP_TO_BOTTOM(HarfBuzz.HB_DIRECTION_TTB);

        private final int cEnumValue;

        private TextDirection(int cEnumValue) {
            this.cEnumValue = cEnumValue;
        }
    }

    private final long address;
    private boolean disposed = false;

    public HarfBuzzBuffer() {
        this.address = HarfBuzz.hb_buffer_create();
    }

    /**
     * Note: For paragraphs requiring multiple shaping runs, {@link #addText(CharSequence, int, int)} ought to be
     * preferred.
     *
     * @param text The text to be shaped.
     */
    public void addText(@NotNull CharSequence text) {
        this.checkFreed();
        HarfBuzz.hb_buffer_add_utf8(this.address, text, 0, text.length());
    }

    public void addText(@NotNull CharSequence text, int begin, int end) {
        this.checkFreed();
        HarfBuzz.hb_buffer_add_utf8(this.address, Objects.requireNonNull(text), 0, end);
    }

    protected final void checkFreed() {
        if (this.disposed) {
            throw new IllegalStateException("use-after-free");
        }
    }

    /**
     * Clears the contents, but not the options of this HarfBuzz buffer.
     *
     * <p>This method does not dispose this buffer.
     */
    public void clearContents() {
        this.checkFreed();
        HarfBuzz.hb_buffer_clear_contents(this.address);
    }

    /**
     * Clears the contents and the options of this HarfBuzz buffer.
     *
     * <p>This method does not dispose this buffer.
     */
    public void clearEverything() {
        this.checkFreed();
        HarfBuzz.hb_buffer_reset(this.address);
    }

    @Override
    public void close() {
        this.checkFreed();
        this.disposed = true;
        HarfBuzz.hb_buffer_destroy(this.address);
    }

    @Override
    public void dispose() {
        this.close();
    }

    public long getAddress() {
        this.checkFreed();
        return this.address;
    }

    @NotNull
    public ContentType getContentType() {
        this.checkFreed();

        switch (HarfBuzz.hb_buffer_get_content_type(this.address)) {
        case HarfBuzz.HB_BUFFER_CONTENT_TYPE_INVALID:
            return ContentType.INVALID;
        case HarfBuzz.HB_BUFFER_CONTENT_TYPE_UNICODE:
            return ContentType.UNICODE;
        case HarfBuzz.HB_BUFFER_CONTENT_TYPE_GLYPHS:
            return ContentType.GLYPH;
        default:
            throw new IllegalStateException("Unknown/unimplemented content type: " + HarfBuzz.hb_buffer_get_content_type(this.address));
        }
    }

    public void setContentType(@NotNull ContentType type) {
        this.checkFreed();
        HarfBuzz.hb_buffer_set_content_type(this.address, type.cEnumValue);
    }

    public void setDirection(@NotNull TextDirection direction) {
        this.checkFreed();
        HarfBuzz.hb_buffer_set_direction(this.address, direction.cEnumValue);
    }

    public void setLanguage(@NotNull HarfBuzzLanguage language) {
        this.checkFreed();
        HarfBuzz.hb_buffer_set_language(this.address, language.getAddress());
    }

    public void setScript(int script) {
        this.checkFreed();
        HarfBuzz.hb_buffer_set_script(this.address, script);
    }

    @NotNull
    public ShapedGlyph @NotNull[] shape(@NotNull HarfbuzzFont font) {
        this.checkFreed();
        HarfBuzz.hb_shape(font.getAddress(), this.address, null);

        hb_glyph_info_t.@Nullable Buffer glyphInfos = HarfBuzz.hb_buffer_get_glyph_infos(this.address);
        hb_glyph_position_t.@Nullable Buffer glyphPositions = HarfBuzz.hb_buffer_get_glyph_positions(this.address);

        if (glyphInfos == null) {
            throw new IllegalStateException("Cannot obtain glyph infos from buffer.");
        } else if (glyphPositions == null) {
            throw new IllegalStateException("Cannot obtain glyph positions from buffer.");
        } else if (glyphInfos.remaining() != glyphPositions.remaining()) {
            throw new IllegalStateException("Mismatching glyph count for glyph infos and glyph positions.");
        }

        @NotNull ShapedGlyph[] glyphs = new @NotNull ShapedGlyph[glyphInfos.remaining()];

        for (int i = 0; glyphInfos.hasRemaining(); i++) {
            hb_glyph_info_t glyphInfo = glyphInfos.get();
            hb_glyph_position_t glyphPosition = glyphPositions.get();

            glyphs[i] = new ShapedGlyph(glyphInfo.codepoint(), glyphInfo.cluster(), glyphPosition.x_offset(), glyphPosition.y_offset(), glyphPosition.x_advance(), glyphPosition.y_advance());
        }

        return glyphs;
    }

    /**
     * Used when the script, language, and direction is unknown.
     *
     * <p>Warning: This method is globally not thread-safe as it calls hb_language_get_default(),
     * which is not thread-safe.
     */
    public void withGuessedSegmentProperties() {
        this.checkFreed();
        HarfBuzz.hb_buffer_guess_segment_properties(this.address);
    }
}
