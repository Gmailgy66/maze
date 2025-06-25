package com.caicai.game.common;

import java.util.Random;

public class RandUtil {
    public static int randOdd(int min, int max) {
        try {

            int result;
            do {
                result = random.nextInt(min, max );
            } while ((result & 1) == 0); // Ensure the number is odd
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    static Random random = new Random();

    public static int randEven(int min, int max) {
        int result;
        try {

            do {
                result = random.nextInt(min, max);
            } while ((result & 1) == 1); // Ensure the numbder is od
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
