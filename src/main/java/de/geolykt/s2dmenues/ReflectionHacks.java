package de.geolykt.s2dmenues;

import java.lang.reflect.Field;

import org.slf4j.LoggerFactory;

import de.geolykt.starloader.starplane.annotations.ReferenceSource;
import de.geolykt.starloader.starplane.annotations.RemapMemberReference;
import de.geolykt.starloader.starplane.annotations.RemapMemberReference.ReferenceFormat;

import snoddasmannen.galimulator.ProceduralStarGenerator;

public class ReflectionHacks {
    @RemapMemberReference(
        format = ReferenceFormat.NAME,
        owner = "snoddasmannen/galimulator/ProceduralStarGenerator$24",
        name = "data",
        desc = "Lsnoddasmannen/galimulator/ProceduralStarGenerator$24$Local0;"
    )
    private static final String MOVING_PLANETS_METADATA_FIELD = ReferenceSource.getStringValue();

    @RemapMemberReference(
        format = ReferenceFormat.NAME,
        owner = "snoddasmannen/galimulator/ProceduralStarGenerator$24$Local0",
        name = "a",
        descType = int.class
    )
    private static final String MOVING_PLANETS_METADATA_PLANET_FIELD = ReferenceSource.getStringValue();

    public static int getPlanetaryStarGeneratorPlanetCount() {
        try {
            Field f = ProceduralStarGenerator.MOVING_PLANETS.getClass().getDeclaredField(ReflectionHacks.MOVING_PLANETS_METADATA_FIELD);
            f.setAccessible(true);
            Object metadata = f.get(ProceduralStarGenerator.MOVING_PLANETS);
            f = metadata.getClass().getDeclaredField(ReflectionHacks.MOVING_PLANETS_METADATA_PLANET_FIELD);
            f.setAccessible(true);
            return f.getInt(metadata);
        } catch (ReflectiveOperationException e) {
            LoggerFactory.getLogger(ReflectionHacks.class).error("Unable to get the amount of stars of the moving planets star generator.", e);
            return 3;
        }
    }

    public static boolean setPlanetaryStarGeneratorPlanetCount(int count) {
        try {
            Field f = ProceduralStarGenerator.MOVING_PLANETS.getClass().getDeclaredField(ReflectionHacks.MOVING_PLANETS_METADATA_FIELD);
            f.setAccessible(true);
            Object metadata = f.get(ProceduralStarGenerator.MOVING_PLANETS);
            f = metadata.getClass().getDeclaredField(ReflectionHacks.MOVING_PLANETS_METADATA_PLANET_FIELD);
            f.setAccessible(true);
            f.setInt(metadata, count);
            return true;
        } catch (ReflectiveOperationException e) {
            LoggerFactory.getLogger(ReflectionHacks.class).error("Unable to set the amount of stars of the moving planets star generator.", e);
            return false;
        }
    }

    private ReflectionHacks() {
        throw new UnsupportedOperationException();
    }
}
