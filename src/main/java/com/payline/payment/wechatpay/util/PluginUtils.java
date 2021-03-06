package com.payline.payment.wechatpay.util;

import com.payline.pmapi.bean.common.Amount;
import lombok.experimental.UtilityClass;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.stream.Collectors;

@UtilityClass
public class PluginUtils {
    public final int ERROR_CODE_MAX_LENGTH = 50;

    public String truncate(String value, int length) {
        if (value != null && value.length() > length) {
            value = value.substring(0, length);
        }
        return value;
    }

    /**
     * Convert an InputStream into a String
     *
     * @param stream the InputStream to convert
     * @return the converted String encoded in UTF-8
     */
    public String inputStreamToString(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        return br.lines().collect(Collectors.joining(System.lineSeparator()));
    }


    /**
     * Check if a String is null, empty or filled with space
     *
     * @param value the String to check
     * @return true if the string is empty
     */
    public boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Return a string which was converted from cents to currency units
     *
     * @param amount the amount to convert
     * @return a String of the converted amount for example
     */
    public String createStringAmount(Amount amount) {
        StringBuilder sb = new StringBuilder();
        sb.append(amount.getAmountInSmallestUnit());

        // get digit number of the currency and add a dot in the right place
        int nbDigits = amount.getCurrency().getDefaultFractionDigits();
        for (int i = sb.length(); i < nbDigits + 1; i++) {
            sb.insert(0, "0");
        }
        sb.insert(sb.length() - nbDigits, ".");

        return sb.toString();
    }

    /**
     * Return a string which was converted from cents to currency units and add the device symbol right after it
     * @param amount the amount to convert
     * @return the string to show
     */
    public String createStringAmountToShow(Amount amount) {
        return createStringAmount(amount) + amount.getCurrency().getSymbol();
    }

    public String addIfExist(String s) {
        String toReturn = "";
        if (!isEmpty(s)) {
            toReturn = " " + s;
        }
        return toReturn;
    }

    /**
     * Utility static method to build an error code from a {@link RuntimeException}.
     *
     * @param e The exception
     * @return A truncated errorCode to insert into any FailureResponse object.
     */
    public String runtimeErrorCode(RuntimeException e) {
        String errorCode = "plugin error: " + e.toString().substring(e.toString().lastIndexOf('.') + 1);
        return PluginUtils.truncate(errorCode, ERROR_CODE_MAX_LENGTH);
    }

    private final String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final Random RANDOM = new SecureRandom();
    public String generateRandomString(int l) {
        char[] nonceChars = new char[l];
        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        }
        return new String(nonceChars);
    }

    public String createDate() {
        String pattern = "yyyyMMdd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }
    /**
     * Left pad a string with zeros
     * @param input the string to pad
     * @param targetSize   final size
     * @return padded string
     */
    public static String leftPad(final String input, final int targetSize) {
        final String result;
        if (input == null || input.length() >= targetSize) {
            result = input;
        } else {
            final StringBuilder stringBuilder = new StringBuilder();
            // On construit un chaîne de 0 de taille targetSize
            for (int i = 0; i < targetSize; i++) {
                stringBuilder.append('0');
            }
            // On prend le nombre de 0 dont on a besoin (targetSize - input.size) et on concatene input
            result = stringBuilder.substring(input.length()) + input;
        }
        return result;
    }
}