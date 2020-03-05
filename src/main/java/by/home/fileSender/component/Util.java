package by.home.fileSender.component;

/**
 * The type Util.
 */

public class Util {

    /**
     * Validate.
     *
     * @param expression   the expression
     * @param errorMessage the error message
     */
    public static void validate(boolean expression, String errorMessage) {
        if (expression) {
            throw new RuntimeException(errorMessage);
        }
    }

    public static long convertStringMbToByte(String mb) {
        validate(mb == null, "file.size.value");
        return Long.parseLong(mb) * 1000 * 1000;
    }
}
