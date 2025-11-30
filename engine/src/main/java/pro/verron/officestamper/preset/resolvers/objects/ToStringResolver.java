package pro.verron.officestamper.preset.resolvers.objects;

import org.springframework.lang.Nullable;
import pro.verron.officestamper.api.DocxPart;
import pro.verron.officestamper.api.Insert;
import pro.verron.officestamper.api.ObjectResolver;
import pro.verron.officestamper.utils.Inserts;

import java.util.ArrayList;
import java.util.List;

import static pro.verron.officestamper.utils.WmlFactory.*;

/// This class is an implementation of the [ObjectResolver] interface that resolves objects by converting them to a
/// string representation using the [Object#toString()] method and creating a new run with the resolved content.
///
/// @author Joseph Verron
/// @version ${version}
/// @since 1.6.7
public class ToStringResolver
        implements ObjectResolver {

    private final String linebreakPlaceholder;

    public ToStringResolver(String linebreakPlaceholder) {
        this.linebreakPlaceholder = linebreakPlaceholder;
    }

    @Override
    public Insert resolve(DocxPart document, String expression, Object object) {
        var string = String.valueOf(object);

        var split = string.split(linebreakPlaceholder);
        if (split.length == 1) return Inserts.of(newRun(string));
        var elements = new ArrayList<>();
        for (int i = 0; i < split.length - 1; i++) {
            var line = split[i];
            elements.add(newRun(List.of(newText(line), newBr())));
        }
        elements.add(newRun(split[split.length - 1]));
        return Inserts.of(elements);
    }

    @Override
    public boolean canResolve(@Nullable Object object) {
        return object != null;
    }
}
