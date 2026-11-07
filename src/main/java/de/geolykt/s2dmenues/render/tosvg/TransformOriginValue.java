package de.geolykt.s2dmenues.render.tosvg;

import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.badlogic.gdx.math.Rectangle;

public final class TransformOriginValue {

    private static final class FractionDimension extends TransformOriginDimension {
        private final double fraction;

        public FractionDimension(double fraction) {
            this.fraction = fraction;
        }

        @Override
        public double compute(double fullRange) {
            return fullRange * this.fraction;
        }
    }

    private static final class LengthDimension extends TransformOriginDimension {
        private final double pixelValue;

        public LengthDimension(double pixelValue) {
            this.pixelValue = pixelValue;
        }

        @Override
        public double compute(double fullRange) {
            return this.pixelValue;
        }
    }

    private static abstract sealed class TransformOriginDimension permits LengthDimension, FractionDimension {
        public abstract double compute(double fullRange);
    }

    @NonNull
    public static final TransformOriginValue parse(@NonNull CharSequence sequence, int start, int end) {
        TOSubstringScanner scanner = new TOSubstringScanner(sequence, start, end);

        TransformOriginDimension x = null;
        TransformOriginDimension y = null;
        TransformOriginDimension z = null;

        char ch = scanner.peekCharacter();

        if (ch == 'l') {
            scanner.assertToNextWhitespaceMatches("left");

            x = new FractionDimension(0.0D);
        } else if (ch == 'c') {
            scanner.assertToNextWhitespaceMatches("center");

            if (!scanner.hasRemaining()) {
                return new TransformOriginValue(new FractionDimension(0.5D), new FractionDimension(0.5D), null);
            } else {
                x = new FractionDimension(0.5D);
            }
        } else if (ch == 'r') {
            scanner.assertToNextWhitespaceMatches("right");

            x = new FractionDimension(1.0D);
        } else if (ch == 't') {
            scanner.assertToNextWhitespaceMatches("top");

            y = new FractionDimension(1.0D);
        } else if (ch == 'b') {
            scanner.assertToNextWhitespaceMatches("bottom");

            y = new FractionDimension(0.0D);
        } else if (ch == '0' && scanner.getRemaining() == 1) {
            return new TransformOriginValue(new LengthDimension(0.0D), null, null);
        } else {
            double value = scanner.readNumber();

            if (!scanner.hasRemaining()) {
                return new TransformOriginValue(new LengthDimension(value), null, null);
            }

            ch = scanner.peekCharacter();

            if (ch == '%') {
                scanner.skipCharacter();
                x = new FractionDimension(value / 100.0D);
            } else if (ch == 'p') {
                scanner.assertToNextWhitespaceMatches("px");

                x = new LengthDimension(value);
            } else {
                throw new IllegalStateException("Unexpected character: " + ch);
            }
        }

        scanner.skipWhitespace();

        if (!scanner.hasRemaining()) {
            return new TransformOriginValue(x, y, z);
        }

        ch = scanner.peekCharacter();

        if (ch == 'l') {
            scanner.assertToNextWhitespaceMatches("left");

            throw new IllegalStateException("Token cannot be 'left' at this position");
        } else if (ch == 'c') {
            scanner.assertToNextWhitespaceMatches("center");

            if (y != null) {
                throw new IllegalStateException("Vertical position already set");
            }

            y = new FractionDimension(0.5D);
        } else if (ch == 'r') {
            scanner.assertToNextWhitespaceMatches("right");

            throw new IllegalStateException("Token cannot be 'right' at this position");
        } else if (ch == 't') {
            scanner.assertToNextWhitespaceMatches("top");

            if (y != null) {
                throw new IllegalStateException("Vertical position already set");
            }

            y = new FractionDimension(1.0D);
        } else if (ch == 'd') {
            scanner.assertToNextWhitespaceMatches("down");

            if (y != null) {
                throw new IllegalStateException("Vertical position already set");
            }

            y = new FractionDimension(0.0D);
        } else if (ch == '0' && scanner.getRemaining() == 1) {
            if (y != null) {
                throw new IllegalStateException("Vertical position already set");
            }

            return new TransformOriginValue(x, new LengthDimension(0.0D), null);
        } else {
            double value = scanner.readNumber();

            if (!scanner.hasRemaining()) {
                return new TransformOriginValue(new LengthDimension(value), null, null);
            }

            ch = scanner.peekCharacter();

            @NonNull
            final TransformOriginDimension v;

            if (ch == '%') {
                scanner.skipCharacter();
                v = new FractionDimension(value / 100.0D);
            } else if (ch == 'p') {
                scanner.assertToNextWhitespaceMatches("px");

                v = new LengthDimension(value);
            } else {
                throw new IllegalStateException("Unexpected character: " + ch);
            }

            if (x == null) {
                x = v;
            } else {
                y = v;
            }
        }

        scanner.skipWhitespace();

        if (scanner.hasRemaining()) {
            throw new IllegalStateException("Three-dimensional transform origin not supported.");
        }

        return new TransformOriginValue(x, y, z);
    }
    @Nullable
    private final TransformOriginDimension x;
    @Nullable
    private final TransformOriginDimension y;

    @Nullable
    private final TransformOriginDimension z;

    private TransformOriginValue(@Nullable TransformOriginDimension x, @Nullable TransformOriginDimension y, @Nullable TransformOriginDimension z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @NonNull
    public Vector3dc getOriginTransformation(@NonNull Rectangle box) {
        double x = 0.0d;
        double y = 0.0d;

        if (this.x != null) {
            x = this.x.compute(box.getWidth()) + box.getX();
        }

        if (this.y != null) {
            y = this.y.compute(box.getHeight()) + box.getY();
        }

        return new Vector3d(x, y, 0);
    }
}
