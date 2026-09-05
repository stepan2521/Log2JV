package io.log2jv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageFormatterTest {

    @Test
    void formatsPlaceholders() {
        assertEquals("Log2JV 1 2.5 [0, 5, 2, 4, 6] Name: 5",
                MessageFormatter.format(
                        "${} ${} ${} ${} ${}",
                        "Log2JV",
                        1,
                        2.5,
                        new int[]{0, 5, 2, 4, 6},
                        new Pair<>("Name", 5)
                ));
    }

    @Test
    void supportsDollarLiteral() {
        assertEquals("$ is a dollar literal", MessageFormatter.format("$$ is a dollar literal"));
        assertEquals("Cost is $5 dollars", MessageFormatter.format("Cost is $${} dollars", 5));
        assertEquals("100$", MessageFormatter.format("100$$"));
        assertEquals("$5$", MessageFormatter.format("$$${}$$", 5));
    }

    @Test
    void rejectsTooFewArguments() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MessageFormatter.format("${} ${} ${}", "a", "b")
        );
        assertEquals("Too few arguments for Logger.log - expected 3, but got 2", ex.getMessage());
    }

    @Test
    void rejectsTooManyArguments() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> MessageFormatter.format("${}", "a", "b")
        );
        assertEquals("Too many arguments for Logger.log - expected 1, but got 2", ex.getMessage());
    }

    @Test
    void handlesNullAndPlainText() {
        assertEquals("hello null world", MessageFormatter.format("hello ${} world", (Object) null));
        assertEquals("no placeholders", MessageFormatter.format("no placeholders"));
        assertEquals("", MessageFormatter.format(""));
    }

    @Test
    void handlesNestedArrays() {
        assertEquals("[[1, 2], [3, 4]]",
                MessageFormatter.format("${}", (Object) new int[][]{{1, 2}, {3, 4}}));
    }
}
