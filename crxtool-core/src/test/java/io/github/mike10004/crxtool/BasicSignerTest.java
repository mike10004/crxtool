package io.github.mike10004.crxtool;

import com.google.common.io.BaseEncoding;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

import static org.junit.Assert.*;

public class BasicSignerTest {

    private static final BaseEncoding hex = BaseEncoding.base16();
    private static final int trials = 3;

    @Test
    public void sign_sha1() throws Exception {
        sign("SHA1");
    }

    @Test
    public void sign_sha256() throws Exception {
        sign("SHA256");
    }

    private static final Charset dataCharset = StandardCharsets.UTF_8;
    private static final Charset sigCharset = StandardCharsets.US_ASCII;

    @SuppressWarnings("SameParameterValue")
    private static String readString(Path p, Charset cs) throws IOException {
        return new String(java.nio.file.Files.readAllBytes(p), cs);
    }

    @SuppressWarnings("SameParameterValue")
    private static void writeString(Path p, String s, Charset cs, OpenOption...options) throws IOException {
        java.nio.file.Files.write(p, s.getBytes(cs), options);
    }

    private void sign(String hashFunction) throws IOException, GeneralSecurityException {
        File previousFile = new File(FileUtils.getTempDirectory(), String.format("%s.%s.previous", getClass().getSimpleName(), hashFunction));
        String previousSig = null;
        if (previousFile.isFile()) {
            previousSig = readString(previousFile.toPath(), sigCharset).trim();
        }
        PrivateKey privateKey = KeyPairs.loadRsaPrivateKeyFromKeyBytes(PemParser.getInstance().extractBytes(new StringReader(TEST_KEY_PKCS8_PEM)));
        byte[] theData = "This is the input data".getBytes(dataCharset);
        String hexSignature = null;
        for (int i = 0; i < trials; i++) {
            Signer signer = new BasicSigner(hashFunction, "RSA");
            byte[] signature = signer.sign(theData, privateKey);
            hexSignature = hex.encode(signature);
            System.out.print(hexSignature);
            if (previousSig != null) {
                assertEquals("signature does not match previously computed", previousSig, hexSignature);
                if (hexSignature.equalsIgnoreCase(previousSig)) {
                    System.out.print(" (checks out)");
                }
            }
            System.out.println();
        }
        writeString(previousFile.toPath(), hexSignature, sigCharset, StandardOpenOption.CREATE);
        System.out.format("%s stores previous signature%n", previousFile);
    }

    private static final String TEST_KEY_PKCS8_PEM =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAKsNkY+RmzFgCaRr\n" +
            "ZEisb6Umv1oMtW+hwO4HFzanAmm8lkW770FOmTzxCwwe9TR0q6y/h6T8NPg0q5Lo\n" +
            "FacrtaKR7qIpeXhCNnzFtsB+GAGKLs/v+j3k6SUz9hNpQ+I+x3nWJqj2zByUnMef\n" +
            "27oEEYnkdH8IZjhGjze+udhjY2djAgMBAAECgYEAofUC8ZDIBIQOHSOhnD3Ip/J8\n" +
            "E2MdF0lsRvNnw1N2MW9iSTycWJV2+gCwmRVmzff55GKkqE40SR51pW8hsVrtaidF\n" +
            "mt3fqgGwoWvMWdBA9DvULYfcEoModBLOe2Fq+Ye6uly/bc6R/Okju8adWnQIiPBm\n" +
            "0V2rzDDyeyiGiW5CSCECQQDcBtBPnUrDEfFn1ve04TjaT4F+6Yz5HLvORw/7ddwI\n" +
            "pxejPNe2JlEHG/f1Y1vReLi2c/XYLicp5fLnkkYvjoQ/AkEAxwT4b34MleN2if50\n" +
            "lr0T2giwRF7R1G99kOi6uGKqKsFfDJI2c+vMy0TBbmWu1q7xUoGTlTEUJTLIM+gt\n" +
            "gJGD3QJAb8tafolKGlF8milElPg2nd31yxk50r1Bw26h9T+OPCtCPGPGqrtPQlB4\n" +
            "rYr9dTJkj/fdeykAJy66O+U7miBMmwJBAItjytDj2tK8TpDV3DibUyUZgsNQGeyM\n" +
            "7cgpiGWODRsG9phqa6MDGxufG09D/pG0U6byxDfw+OgYyim4yu3KfS0CQGJcbGiL\n" +
            "ahyaxByX8HF/oJazwB8LViMIQEGPJ1zthpGfvK9kMR65VM5NSfgJHIslj6ri+D54\n" +
            "0Z9PbGbf3ZQ3MXA=\n" +
            "-----END PRIVATE KEY-----\n";
}