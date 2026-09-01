package de.geolykt.s2dmenues.cjk;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

public final class TWRAutoCloseablePassthrough implements AutoCloseable {
    private boolean blockedPassthrough = false;
    @NotNull
    private final List<AutoCloseable> closeables;

    public TWRAutoCloseablePassthrough() {
        this.closeables = new ArrayList<>(16);
    }

    public void addResource(@NotNull AutoCloseable resource) {
        this.closeables.add(Objects.requireNonNull(resource, "'resource' may not be null"));
    }

    @Override
    public void close() {
        if (!this.blockedPassthrough) {
            List<Throwable> suppressed = new ArrayList<>(8);

            for (int i = 0; i < this.closeables.size(); i++) {
                try {
                    this.closeables.get(i).close();
                } catch (Throwable t) {
                    suppressed.add(t);
                }
            }

            if (suppressed.size() == 0) {
                return;
            } else if (suppressed.size() == 1) {
                Throwable rethrow = suppressed.get(0);

                if (rethrow instanceof RuntimeException) {
                    throw (RuntimeException) rethrow;
                } else if (rethrow instanceof Error) {
                    throw (Error) rethrow;
                } else if (rethrow instanceof IOException) {
                    throw new UncheckedIOException("Unable to close resource", (IOException) rethrow);
                } else {
                    throw new RuntimeException("Unable to close resource", rethrow);
                }
            } else {
                RuntimeException ex = new RuntimeException("Unable to close resource");
                suppressed.forEach(ex::addSuppressed);
                throw ex;
            }
        }
    }

    public void disablePassthrough() {
        this.blockedPassthrough = true;
    }
}
