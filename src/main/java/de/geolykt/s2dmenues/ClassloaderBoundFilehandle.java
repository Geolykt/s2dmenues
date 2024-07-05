package de.geolykt.s2dmenues;

import java.io.InputStream;

import org.jetbrains.annotations.NotNull;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;

public class ClassloaderBoundFilehandle extends FileHandle {

    @NotNull
    private final ClassLoader loader;

    @NotNull
    private final String path;

    public ClassloaderBoundFilehandle(@NotNull String path, @NotNull ClassLoader cl) {
        super(path, FileType.Classpath);
        this.path = path;
        this.loader = cl;
    }

    @Override
    public InputStream read() {
        return this.loader.getResourceAsStream(this.path);
    }

    @Override
    public boolean exists() {
        return this.loader.getResource(this.path) != null;
    }
}
