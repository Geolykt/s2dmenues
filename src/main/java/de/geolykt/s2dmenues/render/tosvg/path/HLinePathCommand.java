package de.geolykt.s2dmenues.render.tosvg.path;

public final class HLinePathCommand extends PathCommand {
    public HLinePathCommand(boolean absolute, double value) {
        super(absolute);
        this.value = value;
    }

    private final double value;
}
