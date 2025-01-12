package curtin.edu.lib;

import java.util.*;

public class DivideNumOptions {

    public static void main(String[] args) {
        int[] numOptions = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 15 };
        for (int i = 0; i < numOptions.length; i++) {
            LinkedList<Integer> numbers = divideNumOptions(numOptions[i]);
            displayArray(numOptions[i], numbers);
        }
    }

    /** Only allowed 4, 3 or 2 as the layout of buttons */
    public static LinkedList<Integer> divideNumOptions(int numOptions) {
        LinkedList<Integer> numbers = new LinkedList<>();

        for (int i = 0; i < numOptions; i++) {
            int number = (i % 4) + 1;

            // Everytime it hits 4, or just when the loop ends, store the number
            if ((i+1) % 4 == 0 || i == numOptions - 1) {
                // Can't have 1 as layout so subtract 1 from last number and add 1 to new number
                if (number == 1 && numbers.size() != 0) {
                    int changeNumber = numbers.removeLast();
                    numbers.addLast(changeNumber - 1);
                    number = number + 1;
                }
                numbers.addLast(number);
            }
        }

        return numbers;
    }
    
    public static void displayArray(int numOptions, LinkedList<Integer> numbers) {
        String result = "";
        for (int i = 0; i < numbers.size(); i++) {
            if (i < numbers.size() - 1) {
                result += String.valueOf(numbers.get(i)) + ", ";
            } else {
                result += String.valueOf(numbers.get(i));
            }
        }
        System.out.println(numOptions+" - Divided numbers: "+result);
    }

}
