package pro.verron.officestamper.utils;

import org.jvnet.jaxb2_commons.ppp.Child;
import pro.verron.officestamper.api.OfficeStamperException;

import java.util.Optional;

public final class WmlUtils {
    private WmlUtils() {
        throw new OfficeStamperException("Utility class shouldn't be instantiated");
    }

    public static <T> Optional<T> getFirstParentWithClass(Child child, Class<T> aClass, int depth) {
        var parent = child.getParent();
        var currentDepth = 0;
        while (currentDepth <= depth) {
            currentDepth++;
            if (parent == null) return Optional.empty();
            if (aClass.isInstance(parent)) return Optional.of(aClass.cast(parent));
            if (parent instanceof Child next) parent = next.getParent();
        }
        return Optional.empty();
    }
}
