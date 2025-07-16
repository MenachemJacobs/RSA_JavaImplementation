import java.math.BigInteger;

public class RSAEncryptDecryptMethods {
    public static int[] encrypt(String plainText, BigInteger e, BigInteger n) {
        int[] encrypted = new int[plainText.length()];
        for (int i = 0; i < plainText.length(); i++) {
            BigInteger m = BigInteger.valueOf(plainText.charAt(i));
            encrypted[i] = m.modPow(e, n).intValue();
        }
        return encrypted;
    }

    public static String decrypt(int[] encrypted, BigInteger d, BigInteger n) {
        StringBuilder decrypted = new StringBuilder();
        for (int i : encrypted) {
            BigInteger c = BigInteger.valueOf(i);
            char m = (char) c.modPow(d, n).intValue();
            decrypted.append(m);
        }
        return decrypted.toString();
    }
}
