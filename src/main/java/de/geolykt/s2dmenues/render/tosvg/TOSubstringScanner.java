package de.geolykt.s2dmenues.render.tosvg;

import java.util.NoSuchElementException;

import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.jspecify.annotations.NonNull;

import de.geolykt.s2dmenues.render.tosvg.path.EllipticalArcPathCommand.ArcSize;
import de.geolykt.s2dmenues.render.tosvg.path.EllipticalArcPathCommand.ArcSweep;

@Internal
public final class TOSubstringScanner {
    private final int endPosition;
    private int position;
    @NonNull
    private final CharSequence string;

    public TOSubstringScanner(@NonNull CharSequence string, int position, int end) {
        this.string = string;
        this.position = position;
        this.endPosition = end;

        if (this.position > this.endPosition) {
            throw new IllegalArgumentException("position exceeds endPosition");
        }

        this.skipWhitespace();
    }

    @Contract(pure = false, mutates = "this")
    private void advanceToNextWhitespace() {
        while (++this.position < this.endPosition && !Character.isWhitespace(this.string.charAt(this.position)));
    }

    @Contract(pure = false, mutates = "this")
    public void assertToNextWhitespaceMatches(@NonNull CharSequence sequence) {
        final int start = this.position;

        this.advanceToNextWhitespace();

        int length = this.position - start;

        if (sequence.length() != length) {
            throw new IllegalStateException(length + " characters in sequence till next whitespace, but expected " + sequence.length() + ". Expected sequence '" + sequence + "' but got '" + this.string.subSequence(start, this.position) + "' instead within the scanner's buffer.");
        }

        while (--length != 0) {
            if (this.string.charAt(start + length) != sequence.charAt(length)) {
                throw new IllegalStateException("Expected sequence '" + sequence + "' but the buffer contains sequence '" + this.string.subSequence(start, this.position) + "'");
            }
        }

        this.skipWhitespace();
    }

    @Contract(pure = true)
    public boolean hasRemaining() {
        return this.position < this.endPosition;
    }

    @Contract(pure = true)
    public int getRemaining() {
        return this.endPosition - this.position;
    }

    @Contract(pure = true)
    public boolean isNumericChar() {
        char c = this.peekCharacter();

        return c == '+' || c == '-' || Character.isDigit(c);
    }

    @Contract(pure = true)
    public char peekCharacter() {
        if (!this.hasRemaining()) {
            throw new NoSuchElementException("Exhausted input string: " + this.string);
        }

        return this.string.charAt(this.position);
    }

    @NonNull
    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    public ArcSize readArcSizeMulti() {
        char c = this.readCharacter();
        this.skipSeparator();

        if (c == '0') {
            return ArcSize.SMALL;
        } else if (c == '1') {
            return ArcSize.LARGE;
        } else {
            throw new UnsupportedOperationException("Unknown arc size flag: " + c);
        }
    }

    @NonNull
    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    public ArcSweep readArcSweepMulti() {
        char c = this.readCharacter();
        this.skipSeparator();

        if (c == '0') {
            return ArcSweep.COUNTERCLOCKWISE;
        } else if (c == '1') {
            return ArcSweep.CLOCKWISE;
        } else {
            throw new UnsupportedOperationException("Unknown arc sweep flag: " + c);
        }
    }

    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    public char readCharacter() {
        if (!this.hasRemaining()) {
            throw new NoSuchElementException("Exhausted input string: " + this.string);
        }

        return this.string.charAt(this.position++);
    }

    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    public double readNumber() {
        double v = this.readNumber0();
        this.skipWhitespace();

        return v;
    }

    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    private double readNumber0() {
        int start = this.position;

        char ch = this.peekCharacter();

        if (ch != '-' && ch != '+' && (ch < '0' || ch > '9')) {
            throw new NoSuchElementException("Sequence does not contain a number value: " + this.string.subSequence(start, this.endPosition));
        }

        boolean exponential = false;
        boolean decimal = false;

        while (++this.position < this.endPosition) {
            ch = this.peekCharacter();

            if (ch == 'e' && !exponential) {
                exponential = true;
            } else if (ch == '.' && !decimal) {
                if (exponential) {
                    break;
                }

                decimal = true;
            } else if (ch < '0' || ch > '9') {
                break;
            }
        }

        return Double.parseDouble(this.string.subSequence(start, this.position).toString());
    }

    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    public double readNumberMulti() {
        double v = this.readNumber0();
        this.skipSeparator();

        return v;
    }

    @NonNull
    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    public Vector2dc readVector2() {
        int pos = this.position;

        try {
            double x = this.readNumber0();
            this.skipSeparator();
            double y = this.readNumber0();
            this.skipWhitespace();

            return new Vector2d(x, y);
        } catch (RuntimeException e) {
            throw new IllegalStateException("Cannot parse vector2 in sequence: " + this.string.subSequence(pos, this.endPosition), e);
        }
    }

    /**
     * Like {@link #readVector2()}, but consume a single ',' token coming after the vector, and any whitespace after that token.
     *
     * <p>This method is for use when there are additional arguments coming <b>after</b> this vector.
     *
     * @return The read {@link Vector2dc} argument within this string. The two numbers making up the vector must be separated with whitespace, ',', or both.
     */
    @NonNull
    @Contract(pure = false, mutates = "this")
    @CheckReturnValue
    public Vector2dc readVector2Multi() {
        Vector2dc ret = this.readVector2();

        if (this.string.charAt(this.position) == ',') {
            this.position++; // Skip over ','
            this.skipWhitespace();

            if (this.string.charAt(this.position) == ',') {
                throw new IllegalStateException("Double ',' in string near index " + this.position + ": " + this.string);
            }
        }

        return ret;
    }

    @Contract(pure = false, mutates = "this")
    private void skipSeparator() {
        this.skipWhitespace();

        if (this.string.charAt(this.position) != ',') {
            return;
        }

        this.position++; // Skip over ','
        this.skipWhitespace();

        if (this.string.charAt(this.position) == ',') {
            throw new IllegalStateException("Double ',' in string near index " + this.position + ": " + this.string);
        }
    }

    @Contract(pure = false, mutates = "this")
    public void skipWhitespace() {
        while (this.hasRemaining() && Character.isWhitespace(this.string.charAt(this.position))) {
            this.position++;
        }
    }

    public void skipCharacter() {
        if (!this.hasRemaining()) {
            throw new IndexOutOfBoundsException();
        }

        this.position++;
    }
}
