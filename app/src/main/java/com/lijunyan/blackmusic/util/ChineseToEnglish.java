package com.lijunyan.blackmusic.util;

import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

public class ChineseToEnglish {
    private static final String TAG = "ChineseToEnglish";
    /**
     * 返回一个字的拼音
     */
    public static String toPinYin(char hanzi) {
        HanyuPinyinOutputFormat hanyuPinyin = new HanyuPinyinOutputFormat();
        hanyuPinyin.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        hanyuPinyin.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        hanyuPinyin.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
        String[] pinyinArray = null;
        try {
            //是否在汉字范围内
            if (hanzi >= 0x4e00 && hanzi <= 0x9fa5) {
                pinyinArray = PinyinHelper.toHanyuPinyinStringArray(hanzi, hanyuPinyin);
            }
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
            Log.e(TAG, "toPinYin: hanzi = "+hanzi );
            Log.e(TAG, "toPinYin: pinyinArray.toString() = "+pinyinArray.toString() );
        }
        //将获取到的拼音返回
        if (pinyinArray != null && pinyinArray.length > 0) {
            return pinyinArray[0];
        } else {
            Log.e(TAG, "toPinYin: hanzi = "+hanzi );
            return "#";
        }
    }

    //字符串转换成拼音
    public static String StringToPingYin(String input) {
        if (input == null){
            return null;
        }
        String result = null;
        for (int i = 0; i < input.length(); i++) {
            //是否在汉字范围内
            if (input.charAt(i) >= 0x4e00 && input.charAt(i) <= 0x9fa5) {
                result += toPinYin(input.charAt(i));
            } else {
                result += input.charAt(i);
            }
        }
        if (result.length() > 4) {
            result = result.substring(4, result.length());
        }
        return result;
    }

    public static String StringToPinyinSpecial(String input){
        if (input == null){
            return null;
        }
        String result = null;
        for (int i = 0; i < input.length(); i++) {
            //是否在汉字范围内
            if (input.charAt(i) >= 0x4e00 && input.charAt(i) <= 0x9fa5) {
                result += toPinYin(input.charAt(i));
            } else {
                result += input.charAt(i);
            }
        }
        if (result.length() > 4) {
            result = result.substring(4, result.length());
        }
        //如果首字母不在[a,z]和[A,Z]内则首字母改为‘#’
        if (!(result.toUpperCase().charAt(0) >= 'A' && result.toUpperCase().charAt(0) <= 'Z')){
            StringBuilder builder = new StringBuilder(result);
            builder.replace(0,1,"#");
            result = builder.toString();
        }
        return result;
    }
}