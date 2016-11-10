/**
 * 2016-4-7
 * AutoCycleTestUtils.java
 * TODO xxxx
 * liunianliang
 */

package com.huaqin.autocycletest.util;

/**
 * @author liunianliang
 */
public class AutoCycleTestUtils {

    public static final int LIST_ITEM_0 = 0;
    public static final int LIST_ITEM_1 = 1;
    public static final int LIST_ITEM_2 = 2;
    public static final int LIST_ITEM_3 = 3;
    public static final int LIST_ITEM_4 = 4;
    public static final int LIST_ITEM_5 = 5;
    public static final int LIST_ITEM_6 = 6;
    public static final int LIST_ITEM_7 = 7; //special for all
    public static final int[] LIST_ITEM_ALL = {
            LIST_ITEM_0, LIST_ITEM_1, LIST_ITEM_2, LIST_ITEM_3, LIST_ITEM_4, LIST_ITEM_5,
            LIST_ITEM_6
    };

    public static final int VIDEO_TEST = 0;
    public static final int AUDIO_TEST = 1;
    public static final int BLUETOOTH_TEST = 2;
    public static final int WIFI_TEST = 3;
    public static final int VIBRATOR_TEST = 4;
    public static final int EARPIECE_TEST = 5;
    public static final int SD_TEST = 6;
    public static final int CAMEAR_TEST = 7;
    public static final int ALPSGSENSOR_TEST = 8;
    public static final int LCD_TEST = 9;
    public static final int GYRMAGSENSOR_TEST = 10;
    public static final int GPS_TEST = 11;

    public static final int[] FIRST_ITEM = {
            VIDEO_TEST
    };
    public static final int[] SENCOND_ITEM = {
            AUDIO_TEST, BLUETOOTH_TEST, WIFI_TEST, VIBRATOR_TEST
    };
    public static final int[] THIRD_ITEM = {
            EARPIECE_TEST, SD_TEST, CAMEAR_TEST, ALPSGSENSOR_TEST
    };
    public static final int[] FOURTH_ITEM = {
            LCD_TEST, GYRMAGSENSOR_TEST, EARPIECE_TEST
    };
    public static final int[] FIFTH_ITEM = {
            LCD_TEST, EARPIECE_TEST, SD_TEST, GPS_TEST
    };
    public static final int[] SIXTH_ITEM = {
            EARPIECE_TEST, ALPSGSENSOR_TEST, BLUETOOTH_TEST, WIFI_TEST, VIBRATOR_TEST
    };
    public static final int[][] TOTAL_ITEM = {
            FIRST_ITEM, SENCOND_ITEM, THIRD_ITEM, FOURTH_ITEM, FIFTH_ITEM, SIXTH_ITEM
    };
    public static int[] SEVEN_ITEM = null;
    static {
        int len = 0;
        int index = 0;
        for (int[] array : TOTAL_ITEM) {
            len += array.length;
        }
        SEVEN_ITEM = new int[len];
        for (int[] element : TOTAL_ITEM) {
            for (int element2 : element) {
                SEVEN_ITEM[index++] = element2;
            }
        }
    }
    public static final int[][] FULL_TEST = {
            FIRST_ITEM, SENCOND_ITEM, THIRD_ITEM, FOURTH_ITEM, FIFTH_ITEM, SIXTH_ITEM, SEVEN_ITEM
    };
}
