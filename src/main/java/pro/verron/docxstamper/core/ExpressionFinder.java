package pro.verron.docxstamper.core;

import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;

public class ExpressionFinder {
    private final Pattern pattern;
    private final Matcher matcher;

    public ExpressionFinder(
            Pattern pattern,
            Matcher matcher
    ) {
        this.pattern = pattern;
        this.matcher = matcher;
    }

    public List<Expression> find(@NonNull String text) {
        if (text.isEmpty())
            return emptyList();
        var matcher = pattern.matcher(text);
        int index = 0;
        List<Expression> matches = new ArrayList<>();
        while (matcher.find(index)) {
            String match = matcher.group();
            matches.add(new Expression(this.matcher, match));
            index = matcher.end();
        }
        return matches;
    }
}
