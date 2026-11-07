package de.geolykt.s2dmenues.render.tosvg.path;

public final class VLinePathCommand extends PathCommand {

    public VLinePathCommand(boolean absolute, double value) {
        super(absolute);
        this.value = value;
    }

    private final double value;
}
