import com.staticconstants.flowpad.backend.security.HashedPassword;
import com.staticconstants.flowpad.backend.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordHasherTest {

    @Test
    public void testHashAndVerifyPassword() throws Exception {
        char[] password = "password123!$".toCharArray();
        HashedPassword hashed = PasswordHasher.hashPassword(password.clone());

        assertNotNull(hashed.hashBase64, "Hash should not be null");
        assertNotNull(hashed.saltBase64, "Salt should not be null");

        boolean isValid = PasswordHasher.verifyPassword("password123!$".toCharArray(), hashed.hashBase64, hashed.saltBase64);
        assertTrue(isValid, "Password verification should succeed");
    }

    @Test
    public void testWrongPasswordFailsVerification() throws Exception {
        char[] correctPassword = "correctPassword".toCharArray();
        char[] wrongPassword = "wrongPassword".toCharArray();

        HashedPassword hashed = PasswordHasher.hashPassword(correctPassword.clone());
        boolean isValid = PasswordHasher.verifyPassword(wrongPassword, hashed.hashBase64, hashed.saltBase64);
        assertFalse(isValid, "Wrong password should not validate");
    }

    @Test
    public void testHashUniquenessWithDifferentSalts() throws Exception {
        char[] password = "password123".toCharArray();

        HashedPassword hashed1 = PasswordHasher.hashPassword(password.clone());
        HashedPassword hashed2 = PasswordHasher.hashPassword(password.clone());

        assertNotEquals(hashed1.hashBase64, hashed2.hashBase64, "Hashes should differ due to different salts");
        assertNotEquals(hashed1.saltBase64, hashed2.saltBase64, "Salts should be different");
    }

    //    TODO: Fix cannot be accessed from outside package
//    @Test
//    public void testSameSaltSameHash() throws Exception {
//        char[] password = "repeatable".toCharArray();
//        byte[] salt = Base64.getDecoder().decode(PasswordHasher.hashPassword("static".toCharArray()).saltBase64);
//
//        String hash1 = PasswordHasher.hashPassword(password.clone(), salt);
//        String hash2 = PasswordHasher.hashPassword(password.clone(), salt);
//
//        assertEquals(hash1, hash2, "Same password and salt should yield the same hash");
//    }

    @Test
    public void testEmptyPassword() throws Exception {
        char[] password = new char[0];
        HashedPassword hashed = PasswordHasher.hashPassword(password.clone());

        assertNotNull(hashed.hashBase64);
        assertNotNull(hashed.saltBase64);

        boolean isValid = PasswordHasher.verifyPassword(password.clone(), hashed.hashBase64, hashed.saltBase64);
        assertTrue(isValid, "Empty password should verify if hashed and verified correctly");
    }

    @Test
    public void testPasswordZeroing() throws Exception {
        char[] password = "toBeCleared".toCharArray();
        PasswordHasher.hashPassword(password);
        for (char c : password) {
            assertEquals('\0', c, "Password char array should be cleared");
        }
    }

//    TODO: Fix cannot be accessed from outside package
//    @Test
//    public void testSaltIsCorrectLength() throws Exception {
//        byte[] salt = PasswordHasher.generateSalt();
//        assertEquals(salt.length, PasswordHasher.SALT_LENGTH, "salt byte array length should equal PasswordHasher.SALT_LENGTH");
//    }
}
