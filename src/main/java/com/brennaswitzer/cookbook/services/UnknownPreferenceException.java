package com.brennaswitzer.cookbook.services;

public class UnknownPreferenceException extends IllegalArgumentException {

    public UnknownPreferenceException(String prefName) {
        super("No '" + prefName + "' preference is known");
    }

}
