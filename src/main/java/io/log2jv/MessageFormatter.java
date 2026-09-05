package io.log2jv;

import java.lang.reflect.Array;

/**
 * Message formatting for Log2JV.
 * <p>
 * Placeholders use the {@code ${}} syntax. Examples:
 * <pre>
 *   MessageFormatter.format("My name is - ${}", "Stepan");
 *   // -&gt; "My name is - Stepan"
 *
 *   MessageFormatter.format("Cost is $${} dollars", 5);
 *   // -&gt; "Cost is $5 dollars"
 *
 *   MessageFormatter.format("$$ is a dollar literal");
 *   // -&gt; "$ is a dollar literal"
 * </pre>
 * <ul>
 *   <li>{@code ${}} — next argument (any type, converted via {@link String#valueOf}).
 *       Arrays and {@link Pair} receive special formatting.</li>
 *   <li>{@code $$} — literal dollar sign {@code $}.</li>
 *   <li>{@code $${}} — literal {@code $} followed by a placeholder.</li>
 * </ul>
 * The number of placeholders must match the number of arguments.
 */
public final class MessageFormatter {

    private MessageFormatter() {
    }

    public static String format(String template, Object... args) {
        if (template == null) {
            throw new IllegalArgumentException("Message template cannot be null");
        }
        if (args == null) {
            args = new Object[0];
        }

        StringBuilder result = new StringBuilder(template.length() + 32);
        int argumentIndex = 0;
        int i = 0;
        final int len = template.length();

        while (i < len) {
            char ch = template.charAt(i);

            if (ch != '$') {
                result.append(ch);
                i++;
                continue;
            }

            // We are at a '$'
            // Priority 1: ${}  (placeholder)
            if (i + 2 < len
                    && template.charAt(i + 1) == '{'
                    && template.charAt(i + 2) == '}') {
                if (argumentIndex >= args.length) {
                    throw new IllegalArgumentException(
                            "Too few arguments for Logger.log - expected "
                                    + countPlaceholders(template)
                                    + ", but got "
                                    + args.length
                    );
                }
                result.append(formatArgument(args[argumentIndex++]));
                i += 3;
                continue;
            }

            // Priority 2: $${}  (literal $ + placeholder)
            if (i + 3 < len
                    && template.charAt(i + 1) == '$'
                    && template.charAt(i + 2) == '{'
                    && template.charAt(i + 3) == '}') {
                if (argumentIndex >= args.length) {
                    throw new IllegalArgumentException(
                            "Too few arguments for Logger.log - expected "
                                    + countPlaceholders(template)
                                    + ", but got "
                                    + args.length
                    );
                }
                result.append('$');
                result.append(formatArgument(args[argumentIndex++]));
                i += 4;
                continue;
            }

            // Priority 3: $$  (literal $)
            if (i + 1 < len && template.charAt(i + 1) == '$') {
                result.append('$');
                i += 2;
                continue;
            }

            // Lone '$' — treat as literal
            result.append('$');
            i++;
        }

        int expected = countPlaceholders(template);
        if (argumentIndex != args.length) {
            throw new IllegalArgumentException(
                    "Too many arguments for Logger.log - expected "
                            + expected
                            + ", but got "
                            + args.length
            );
        }

        return result.toString();
    }

    private static int countPlaceholders(String template) {
        int count = 0;
        int i = 0;
        final int len = template.length();

        while (i < len) {
            if (template.charAt(i) != '$') {
                i++;
                continue;
            }

            // ${}
            if (i + 2 < len
                    && template.charAt(i + 1) == '{'
                    && template.charAt(i + 2) == '}') {
                count++;
                i += 3;
                continue;
            }

            // $${}
            if (i + 3 < len
                    && template.charAt(i + 1) == '$'
                    && template.charAt(i + 2) == '{'
                    && template.charAt(i + 3) == '}') {
                count++;
                i += 4;
                continue;
            }

            // $$
            if (i + 1 < len && template.charAt(i + 1) == '$') {
                i += 2;
                continue;
            }

            // lone $
            i++;
        }
        return count;
    }

    private static String formatArgument(Object value) {
        if (value == null) {
            return "null";
        }

        if (value.getClass().isArray()) {
            return arrayToString(value);
        }

        if (value instanceof Pair) {
            Pair<?, ?> pair = (Pair<?, ?>) value;
            return String.valueOf(pair.getKey()) + ": " + String.valueOf(pair.getValue());
        }

        return String.valueOf(value);
    }

    private static String arrayToString(Object array) {
        int length = Array.getLength(array);
        StringBuilder result = new StringBuilder("[");

        for (int i = 0; i < length; i++) {
            if (i > 0) {
                result.append(", ");
            }
            Object element = Array.get(array, i);
            if (element != null && element.getClass().isArray()) {
                result.append(arrayToString(element));
            } else {
                result.append(String.valueOf(element));
            }
        }

        return result.append(']').toString();
    }
}
