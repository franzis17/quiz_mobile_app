package curtin.edu.math_test.utils;

/**
 * Common Data Validations that could lessen line of code :D
 */
public class ValidateData {

    public static boolean isEmpty(String input) {
        return input.equals("") || input.contains(" ");
    }

}
