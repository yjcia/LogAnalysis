package com.kewill.logUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YanJun on 2016/2/25.
 */
public class StringUtil {
    public static boolean isEmpty(String s){
        return s == null || "".equals(s);
    }

    public static String removeMultiSpaceToOne(String str){
        String s = "";
        for (int i = 0; i < str.length() - 1; i++) {
            //空格转成int型代表数字是32
            if ((int) str.charAt(i) == 32 && (int) str.charAt(i + 1) == 32) {
                continue;
            }
            s += str.charAt(i);
        }
        if ((int) str.charAt(str.length() - 1) != 32)
            s += str.charAt(str.length() - 1);
        return s;

    }
    public static boolean matchDatePattern(String line){
        String eL = "[0-9]{4}-[0-9]{2}-[0-9]{2}";
        Pattern p = Pattern.compile(eL);
        Matcher m = p.matcher(line);
        boolean dateFlag = m.matches();
        return dateFlag;
    }

}
