package com.elcom.metacen.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.apache.commons.lang3.StringUtils;

/**
 * @author hanh
 */
public final class PlateUtils {

    public static final List<String> PROVINCE_PLATE_SUFFIX_LIST = Arrays.asList("11,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,40,34,35,36,37,38,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,59,41,39,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,88,89,90,92,93,94,95,97,13,98,99".split(","));
    public static final List<String> CHARACTER_PLATE_LIST = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "R", "S", "T", "U", "V", "X", "Y", "Z");

    public static String normalizePlate(String plate) {
        if (StringUtils.isNotBlank(plate)) {
            plate = plate.toUpperCase();
            plate = plate.replaceAll("[^\\.\\*A-Z0-9]", "");
        }

        return plate;
    }

    public static boolean isPlateProvided(String plate) {
        return isPlateProvided(plate, true);
    }

    public static boolean isPlateProvided(String plate, boolean wasNormalized) {
        String p = plate;
        if (!wasNormalized) {
            p = PlateUtils.normalizePlate(plate);
        }

        return StringUtils.isNotBlank(p);
    }

    public static boolean isFullPlateProvided(String plate, boolean wasNormalized) {
        String p = plate;
        if (!wasNormalized) {
            p = PlateUtils.normalizePlate(p);
        }

        if (StringUtils.isNotBlank(p)) {
            return p.matches("[A-Z0-9]{9}");
        }

        return false;
    }

    public static boolean isFullPlateProvided(String plate) {
        return isFullPlateProvided(plate, true);
    }

    public static String getRegexPlate(String plate, boolean wasNormalized) {
        String p = plate;

        if (!wasNormalized) {
            p = PlateUtils.normalizePlate(p);
        }

        p = p.replaceAll("(\\.(\\*)+|\\*(\\.)+|(\\*){2,})", "");
        // refine later
        if (p.endsWith(".")) {
            p = p + "*";
        } else if (p.endsWith(".*")) {
            // continue..
        } else {
            p = p + ".*";
        }

        return p;
    }

    private static String genSuffixPlate(int suffix, int maxSuffixLength) {
        String result = String.valueOf(suffix);
        int suffixLength = result.length();
        if (suffixLength > maxSuffixLength) {
            result = result.substring(0, maxSuffixLength);
        } else if (suffixLength < maxSuffixLength) {
            int needApendLength = maxSuffixLength - suffixLength;
            for (int i = 0; i < needApendLength; i++) {
                result = "0" + result;
            }
        }
        return result;
    }

    public static String genRandomPlate(String provinceCode) {
        //Ha noi : 29,30,31,32,33,40
        //TpHCM : 50,51,52,53,..,59
        //..
        Random random = new Random();
        String prefix = StringUtils.isNotBlank(provinceCode) ? provinceCode
                : PROVINCE_PLATE_SUFFIX_LIST.get(random.nextInt(PROVINCE_PLATE_SUFFIX_LIST.size()));
        String character = CHARACTER_PLATE_LIST.get(random.nextInt(CHARACTER_PLATE_LIST.size()));
        String suffix = genSuffixPlate(random.nextInt(99999), random.nextBoolean() ? 5 : 4);
        return prefix + character + suffix;
    }

    public static void main(String[] args) {
        System.out.println("plate: " + genRandomPlate(null));
    }
}
