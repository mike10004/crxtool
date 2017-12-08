package io.github.mike10004.crxtool;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Class that provides a private key for testing purposes only.
 */
public class TestKey {

    /**
     * The private key, encoded in base-64. This key is for testing purposes only.
     * It is shared here intentionally.
     */
    private static final String PRIVATE_KEY =
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC763XlSxZN/Ie8\n" +
            "ZhP82AU35JwEi7RkJNYtSU41iK0Qv28S1VxBaMS2fvWro6cz5OVtRIDNP25eMt2s\n" +
            "KWtb0IkeM4mzyogE1VUnOL29idzntg9VM0kIpVLj5crJP7v0BSIt46EwRuvql52W\n" +
            "es2qZvgCw4YqZmul8NZE/DgSh/KnH3P525h5RZGAH/GaG6FX3z3xhdnrxsIKg2xS\n" +
            "6AosFUVhfHD9PXQB0iS9jmnLGSdVd6L6K5Cyhow6UtnQnxb7zaqGI4JSQ+9OY8FO\n" +
            "up4WHGMLpOc7Ro/A8xwquyVMJ8KH6R5OOZ6iTxHdcaXAWk9k5AZL9p6wdnkTq/1S\n" +
            "FRaVmv5RAgMBAAECggEAP9qRc/lgud0uTkEtV4QbudNPhGfoQwMVV8wJ7Dzl+cPi\n" +
            "vTaMo5kzBoP7fHgeb46+urNFRPjhytZj5oUoeslQXlfK2bHfKC1JQwuPxaDb1ExT\n" +
            "x3KmxW5PzSqfDmU+u3snEwq/EcV3kn40SRavXdChQ2cm5q9osrk0RbqiXwGc3VZH\n" +
            "ZFfrl5jvZIzHbzw0rZfQvyPthQonTh4YoCQZ9PLsIEno+7IuS94MXJ7R/qrCgc5G\n" +
            "5mpLJU0bI/jOo3iHiDrTm4pYJ+NXWOOSnAfXRCKuFK5V8EHJfpORKfuy7A/GF0ib\n" +
            "mJBeD9OKDh4/OC8pMhP/zDVO6yMPIB5RD1oX+/JEFQKBgQD5vEdnuWCr7bHF8OKT\n" +
            "MO1kXwUjmE0uMvTRM0MEKUfs+MJYVuNve4hzEGX48Jeahwlb94Lj9kgD6XAmwhzX\n" +
            "vu7eo3xF0+mgvDOltF4x/vxEpzISPIsP8PrSfW2vOlKF8UH8EhrQ2lqs9ABjy27A\n" +
            "fPSaQHQmhG/Lcb6MXIh3pd78nwKBgQDAojiUxMMlLDUsrxny2hbxZF+2MEFqo1DF\n" +
            "4qDHyJmrvV6/Xc+tYsX76JnFV5Ja2cGxva6N3L1w6NxS4S3MgD45leho1If0PAE7\n" +
            "1IwvEiX9lziBtnBAJ+xtV5R4zALN5Q7FDzTnLz/AIo/DfIggbbicixmvCUwq177q\n" +
            "aodf+TcvDwKBgGdoh+SyRQ5MCT2eRiOOse3KzrTzfpnvraHmaQyTD15XjxnHOLvT\n" +
            "2idTFfZjoVLyMXTsYz2DoERGaBMUc57+R6jW50F+wxG0fGpT6mEnZUwEM16LxUVf\n" +
            "kmyQVVemfb9Kq/j1gjcTA1L0YijwdrarxxkMMUw1sJj8S1uRgUyR2WifAoGBAK1j\n" +
            "FoSpmnDiGrauoS+bJ919GD1Xr/n5KCIFu+w3XkJcAGVFXkHVTxlQIuCi0yQdLp/6\n" +
            "eJxOHOH9Zxrefie9IsUYbom1Cu5F+GJPDSLtNe7M51qxyNWmWcONeiFEmB2VWDb9\n" +
            "WgPg3oPlV/uAItL1j56wAZ0wRDOn37sFyfYTzpz3AoGBALBF5KHXb9ybnpw4oxQy\n" +
            "cP3VP/qj7FADrV6zhNeJ+X7X4clj1ms9F4/Pfv7uODy7JXA4t1TF8/cN/QEfbuR/\n" +
            "9wSpWZBvK51litgLrpnEJ/k46/Sv+p2G/6ML2Dd3eAyo0cPyrBxO9fBmUp/74RQd\n" +
            "zIQZ3o5XayUZKHOcjYS0uG+X";

    public static byte[] getPrivateKeyBytes() {
        return new Base64().decode(PRIVATE_KEY);
    }

    public static class TestKeyTest {

        @Test
        public void getPrivateKeyBytes() {
            byte[] bytes = TestKey.getPrivateKeyBytes();
            System.out.format("%d bytes in private key%n", bytes.length);
            assertEquals("num bytes", 1218, bytes.length);
        }

    }
}
