package com.campus.errand.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MD5UtilTest {

    @Test
    void encryptShouldReturnNullWhenPasswordIsBlank() {
        assertThat(MD5Util.encrypt(null)).isNull();
        assertThat(MD5Util.encrypt("")).isNull();
    }

    @Test
    void verifyShouldReturnFalseForBlankInput() {
        assertThat(MD5Util.verify("", "abc")).isFalse();
        assertThat(MD5Util.verify("123456", "")).isFalse();
    }

    @Test
    void verifyShouldMatchEncryptedPassword() {
        String encrypted = MD5Util.encrypt("123456");

        assertThat(encrypted).isNotBlank();
        assertThat(MD5Util.verify("123456", encrypted)).isTrue();
    }
}
