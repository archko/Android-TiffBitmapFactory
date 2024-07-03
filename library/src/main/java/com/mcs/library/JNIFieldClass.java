package com.mcs.library;

/**
 * @author: archko 2024/7/3 :13:41
 */
public class JNIFieldClass {

    private String mString = "Hello JNI, this is normal string !";

    private int mInt = 1;

    @Override
    public String toString() {
        return "JNIFieldClass [mString=" + mString + ", mInt=" + mInt + "]";
    }
}