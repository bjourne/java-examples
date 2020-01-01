import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;

class GCM {
    private final static int SALT_LENGTH = 256/8;
    private final static int IV_LENGTH = 12;
    private final static int KEY_LENGTH = 256;
    private final static int GCM_TAG_LENGTH = 16;

    private static SecretKeySpec generateKey(char[] password, byte[] salt)
        throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec secretKeySpec =
            new SecretKeySpec(tmp.getEncoded(), "AES");
        return secretKeySpec;
    }
    public static byte[] decrypt(byte[] data, KeyPair keyPair)
        throws GeneralSecurityException {
        byte[] symmetricKey = Arrays.copyOfRange(data, 0, 4096/8);
        byte[] iv = Arrays.copyOfRange(data, 4096/8, 4096/8 + IV_LENGTH);
        byte[] encryptedData = Arrays.copyOfRange(data, 4096/8 + IV_LENGTH,
                                                  data.length);
        Cipher cipher = Cipher.getInstance(
            "RSA/NONE/OAEPWithSHA3-512AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decryptedSymmetricKey = cipher.doFinal(symmetricKey);

        SecretKeySpec secretKeySpec =
            new SecretKeySpec(decryptedSymmetricKey, "AES");
        return decrypt(encryptedData, secretKeySpec, iv);
    }
    private static byte[] decrypt(byte[] data,
                                  SecretKeySpec secretKeySpec,
                                  byte[] iv)
        throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec =
            new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);

        return cipher.doFinal(data);
    }
    public static byte[] decrypt(byte[] data, char[] password)
        throws GeneralSecurityException {
        byte[] salt = Arrays.copyOfRange(data, 0, SALT_LENGTH);
        byte[] iv =
            Arrays.copyOfRange(data, SALT_LENGTH, SALT_LENGTH + IV_LENGTH);
        byte[] encryptedData =
            Arrays.copyOfRange(data, SALT_LENGTH + IV_LENGTH, data.length);

        SecretKeySpec secretKeySpec = generateKey(password, salt);
        return decrypt(encryptedData, secretKeySpec, iv);
    }
    public static byte[] encrypt(byte[] data, byte[] publicKey)
        throws GeneralSecurityException {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        SecretKey secretKey = keyGen.generateKey();
        SecretKeySpec secretKeySpec =
            new SecretKeySpec(secretKey.getEncoded(), "AES");
        byte[] encrypted = encrypt(data, secretKeySpec, iv);

        Cipher cipher =
            Cipher.getInstance("RSA/NONE/OAEPWithSHA3-512AndMGF1Padding");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, keyFactory.generatePublic(keySpec));
        byte[] symmetricKey = cipher.doFinal(secretKeySpec.getEncoded());

        byte[] result = new byte[encrypted.length + iv.length
                                 + symmetricKey.length];
        System.arraycopy(symmetricKey, 0, result, 0, symmetricKey.length);
        System.arraycopy(iv, 0, result, symmetricKey.length, iv.length);
        System.arraycopy(encrypted,0, result, iv.length + symmetricKey.length, encrypted.length);
        return result;
    }
    public static byte[] encrypt(byte[] data, char[] password)
        throws GeneralSecurityException {
        byte[] salt = new byte[SALT_LENGTH];
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(salt);
        new SecureRandom().nextBytes(iv);

        SecretKeySpec secretKeySpec = generateKey(password, salt);
        byte[] encrypted = encrypt(data, secretKeySpec, iv);
        byte[] result = new byte[encrypted.length + salt.length + iv.length];
        System.arraycopy(salt, 0, result, 0, salt.length);
        System.arraycopy(iv, 0, result, salt.length, iv.length);
        System.arraycopy(encrypted, 0, result, salt.length + iv.length, encrypted.length);
        return result;
    }
    private static byte[] encrypt(byte[] data,
                           SecretKeySpec secretKeySpec,
                           byte[] iv)
        throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
        return cipher.doFinal(data);
    }
    static void test1() throws Exception {
        Path dir = Paths.get("C:\\code\\tmp\\");
        var arr = new byte[]{10, 20, 30, 40};
        char[] pass = "password".toCharArray();
        byte[] enc = GCM.encrypt(arr, pass);
        System.out.println(Arrays.toString(GCM.decrypt(enc, pass)));

        KeyPair kp = KeyPairs.generate();
        KeyPairs.save(dir, kp, pass);
    }
    static void test2() throws Exception {
        Path dir = Paths.get("C:\\code\\tmp\\");
        char[] pass = "password".toCharArray();
        KeyPair kp = KeyPairs.load(dir, pass);
        var arr = new byte[]{10, 20, 30, 40};
        byte[] enc = GCM.encrypt(arr, kp.getPublic().getEncoded());
    }

    public static void main(String[] args) throws Exception {
        test1();
    }
}

class KeyPairs {
    private final static String PUBLIC_KEY = "public.key";
    private final static String PRIVATE_KEY = "private.key";

    public static KeyPair generate() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(4096, new SecureRandom());
        return kpg.genKeyPair();
    }

    public static KeyPair load(Path path, char[] password)
        throws IOException, GeneralSecurityException {
        Path publicKeyPath = path.resolve(PUBLIC_KEY);
        byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);
        Path privateKeyPath = path.resolve(PRIVATE_KEY);
        byte[] privateKeyBytes = GCM.decrypt(
            Files.readAllBytes(privateKeyPath),
            password);
        X509EncodedKeySpec x509EncodedKeySpec =
            new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec =
            new PKCS8EncodedKeySpec(privateKeyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        return new KeyPair(publicKey, privateKey);
    }
    public static void save(Path path, KeyPair keyPair, char[] password)
        throws IOException, GeneralSecurityException {
        byte[] encodedPrivateKey = keyPair.getPrivate().getEncoded();
        byte[] privateKeyBytes = GCM.encrypt(encodedPrivateKey, password);
        Files.write(path.resolve(PRIVATE_KEY), privateKeyBytes);
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        Files.write(path.resolve(PUBLIC_KEY), publicKeyBytes);
    }
}
