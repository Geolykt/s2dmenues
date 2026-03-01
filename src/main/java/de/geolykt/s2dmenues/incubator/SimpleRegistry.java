package de.geolykt.s2dmenues.incubator;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import de.geolykt.starloader.api.NamespacedKey;
import de.geolykt.starloader.api.registry.Registry;

public class SimpleRegistry<T> extends Registry<T> {

    protected boolean dirtyValues = true;

    @Override
    @Nullable
    @Deprecated
    public T getIntern(@NotNull String key) {
        throw new UnsupportedOperationException("Registry does not wrap an enum.");
    }

    @Override
    public int getSize() {
        return super.keyedValues.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull T[] getValues() {
        if (this.dirtyValues) {
            super.values = (@NotNull T[]) super.keyedValues.values().toArray();
            this.dirtyValues = false;
        }

        return super.getValues();
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public T nextValue(@NotNull T value) {
        if (this.dirtyValues) {
            super.values = (@NotNull T[]) super.keyedValues.values().toArray();
            this.dirtyValues = false;
        }

        return super.nextValue(value);
    }

    @Override
    public void register(@NotNull NamespacedKey key, @NotNull T value) {
        super.keyedValues.put(key, value);
    }

    @SuppressWarnings("null")
    @NotNull
    @UnmodifiableView
    public Collection<T> valuesView() {
        return Collections.unmodifiableCollection(super.keyedValues.values());
    }
}
