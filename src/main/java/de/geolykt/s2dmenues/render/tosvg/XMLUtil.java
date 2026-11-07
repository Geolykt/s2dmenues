package de.geolykt.s2dmenues.render.tosvg;

import java.util.NoSuchElementException;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import xmlparser.model.XmlElement;

final class XMLUtil {

    private XMLUtil() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static XmlElement getSingleElement(@NonNull XmlElement parent, @NonNull String name) {
        XmlElement selected = null;

        for (XmlElement child : parent.children) {
            if (child.name.equals(name)) {
                if (selected == null) {
                    throw new IllegalStateException("Duplicate element with name '" + name + "' in parent " + parent);
                } else {
                    selected = child;
                }
            }
        }

        if (selected == null) {
            throw new NoSuchElementException("No element with name '" + name + "' in parent " + parent);
        }

        return selected;
    }

    @Nullable
    public static String optAttributeString(@NonNull XmlElement element, @NonNull String attributeName) {
        return element.attributes.get(attributeName);
    }
}
