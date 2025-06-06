package com.staticconstants.flowpad.backend.security;

public class HashedPassword {
    public final String hashBase64;
    public final String saltBase64;

    public HashedPassword(String hashBase64, String saltBase64) {
        this.hashBase64 = hashBase64;
        this.saltBase64 = saltBase64;
    }
    public String getHashedPasswordBase64() {
        return hashBase64;
    }

    public String getEncodedSaltBase64() {
        return saltBase64;
    }
}
