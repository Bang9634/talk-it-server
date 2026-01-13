package com.bang9634.common.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt algorithm.
 * Provides methods to hash passwords, verify passwords, and assess password strength.
 * 
 * @author bang9634
 */
public class PasswordUtil {
    
    /**
     * Default number of hashing rounds for BCrypt.
     * 
     * <h4>Estimated processing time by number of rounds (typical server):</h4>
     * <ul>
     *   <li>10 rounds: ~65ms</li>
     *   <li>12 rounds: ~250ms (recommended)</li>
     *   <li>15 rounds: ~2sec</li>
     * </ul>
     * 
     * <h4>Recommend round selection:</h4>
     * <ul>
     *  <li>4~8: Testing environments</li>
     *  <li>10-12: General web applications</li>
     *  <li>13-15: High-security systems</li>
     * <li>16+: Extremely sensitive data (very slow)</li>
     * </ul>
     * 
     * @implNote Higher rounds increase security but also processing time.
     */
    private static final int DEFAULT_ROUNDS = 12;


    /**
     * Hash a plain text password using BCrypt with default rounds.
     * 
     * @param plainPassword The plain text password to hash
     * @return The hashed password
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(DEFAULT_ROUNDS));
    }

    /**
     * Hash a plain text password using BCrypt with specified rounds.
     * 
     * @param plainPassword The plain text password to hash
     * @param rounds Number of hashing rounds (4-31)
     * @return The hashed password
     */
    public static String hash(String plainPassword, int rounds) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty.");
        }
        if (rounds < 4 || rounds > 31) {
            throw new IllegalArgumentException("BCrypt rounds value must be between 4 and 31.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(rounds));
    }

    /**
     * Verify a plain text password against a stored BCrypt hash.
     * <p>
     * Uses BCrypt's built-in verification to ensure timing-attack safe comparison.
     * Automatically extracts the salt and rounds information embedded in the hash for verification.
     * </p>
     * 
     * <h4>Verification process:</h4>
     * <ol>
     *   <li>Check for null values</li>
     *   <li>Extract salt and rounds information from the hash</li>
     *   <li>Hash the plain password with the same salt and rounds</li>
     *   <li>Perform a timing-attack safe comparison of the two hashes</li>
     * </ol>
     * 
     * @param plainPassword The plain text password to verify
     * @param hashedPassword The stored hashed password
     * @return true if the password matches, false if it does not match or an error occurs
     * 
     * @apiNote Safely returns false for null values or invalid hash formats
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            // Invalid hash format or other BCrypt errors
            System.err.println("Error occurred during password verification: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate if a given hashed password is in valid BCrypt format.
     * <p>
     * Validates the standard format of a BCrypt hash to ensure database integrity.
     * Uses a regular expression to check the structural validity of the BCrypt hash.
     * </p>
     * 
     * <h4>BCrypt hash format:</h4>
     * <pre>{@code
     * $2a$12$abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345
     * ├─┘├┘├─────────────────────────────────────────────────────────┘
     * │  │  └─ Hash value (53 characters)
     * │  └─ Number of rounds (2 characters)
     * └─ BCrypt version ($2a$, $2b$, $2x$, $2y$)
     * }</pre>
     * 
     * @param hashedPassword The hashed password string to validate
     * @return true if the hashed password is a valid BCrypt hash, false otherwise
     * 
     * @implNote This method only validates the format and does not verify the actual hash computation accuracy
     */
    public static boolean isValidHash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        
        // BCrypt hashes start with '$2a$', '$2b$', '$2x$', or '$2y$' and are 60 characters long
        return hashedPassword.matches("^\\$2[abxy]\\$\\d{2}\\$.{53}$");
    }

    /**
     * Extract the number of rounds used in a BCrypt hash
     * <p>
     * Parses and returns the rounds information encoded in the hash string.
     * Used to check the rounds of existing hashes during system upgrades or security policy changes.
     * </p>
     * 
     * <h4>Extraction process:</h4>
     * <ol>
     *   <li>Validate hash format</li>
     *   <li>Split string by dollar ($) sign</li>
     *   <li>Parse rounds from the third part</li>
     *   <li>Convert to integer and return</li>
     * </ol>
     * 
     * @param hashedPassword BCrypt hash string
     * @return Number of rounds used in the hash, or -1 if invalid
     * 
     * @apiNote The returned number of rounds can be used to determine if rehashing is necessary
     */
    public static int getRounds(String hashedPassword) {
        if (!isValidHash(hashedPassword)) {
            return -1;
        }
        
        try {
            // Extract the rounds part from the $2a$12$... format
            String[] parts = hashedPassword.split("\\$");
            return Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Numerically evaluates the strength of a password
     * <p>
     * Evaluates the password strength as a score between 0 and 100 based on various criteria.
     * </p>
     * 
     * @param password The password to evaluate (null or empty string scores 0)
     * @return Strength score (integer between 0 and 100)
     * 
     * @see #getPasswordStrengthText(String) Converts score to text
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        
        // Length criteria
        if (password.length() >= 12) score += 25;
        else if (password.length() >= 8) score += 20;
        else if (password.length() >= 6) score += 15;
        else if (password.length() >= 4) score += 5;
        
        // Include lowercase letters
        if (password.matches(".*[a-z].*")) score += 15;
        
        // Include uppercase letters
        if (password.matches(".*[A-Z].*")) score += 15;
        
        // Include numbers
        if (password.matches(".*\\d.*")) score += 15;
        
        // Include special characters
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score += 15;
        
        // Diversity bonus
        int variety = 0;
        if (password.matches(".*[a-z].*")) variety++;
        if (password.matches(".*[A-Z].*")) variety++;
        if (password.matches(".*\\d.*")) variety++;
        if (password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) variety++;
        
        if (variety >= 3) score += 10;
        else if (variety >= 2) score += 5;
        
        return Math.min(score, 100);
    }

    /**
     * Convert numeric password strength score to descriptive text
     * <p>
     * Converts numeric strength scores to user-friendly 5-level descriptive text.
     * Used to display password strength in the UI.
     * </p>
     * 
     * @param password The password to evaluate
     * @return Descriptive text representing the strength
     * 
     * @see #getPasswordStrength(String) Calculates numeric strength score
     */
    public static String getPasswordStrengthText(String password) {
        int strength = getPasswordStrength(password);
        
        if (strength >= 80) return "Very Strong";
        else if (strength >= 60) return "Strong";
        else if (strength >= 40) return "Moderate";
        else if (strength >= 20) return "Weak";
        else return "Very Weak";
    }

    /**
     * Generate a secure temporary password
     * <p>
     * Generates a temporary password for situations such as account recovery or administrator reset.
     * Ensures high strength by including all character types (uppercase, lowercase, numbers, special characters).
     * </p>
     * 
     * <h4>Generation process:</h4>
     * <ol>
     *   <li>At least one character from each character type is selected (4 characters)</li>
     *   <li>The remaining characters are randomly selected from all character sets</li>
     *   <li>Shuffle the characters using the Fisher-Yates algorithm</li>
     *   <li>Return the final temporary password</li>
     * </ol>
     * 
     * <h4>Included characters:</h4>
     * <ul>
     *   <li><strong>Uppercase letters:</strong> A-Z (26 characters)</li>
     *   <li><strong>Lowercase letters:</strong> a-z (26 characters)</li>
     *   <li><strong>Numbers:</strong> 0-9 (10 characters)</li>
     *   <li><strong>Special characters:</strong> !@#$%^&* (8 characters)</li>
     * </ul>
     * 
     * @param length The length of the password to generate (minimum 8 characters)
     * @return A secure temporary password string
     * 
     * @throws IllegalArgumentException If the length is less than 8
     * 
     * @apiNote The generated temporary password must be delivered to the user through a secure method
     */
    public static String generateTemporaryPassword(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("Temporary password must be at least 8 characters long.");
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder password = new StringBuilder();
        
        // Ensure strength by including at least one character from each category
        password.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt((int) (Math.random() * 26))); // Uppercase
        password.append("abcdefghijklmnopqrstuvwxyz".charAt((int) (Math.random() * 26))); // Lowercase
        password.append("0123456789".charAt((int) (Math.random() * 10))); // Numbers
        password.append("!@#$%^&*".charAt((int) (Math.random() * 8))); // Special characters
        
        // Randomly select from the entire character set for the remaining length
        for (int i = 4; i < length; i++) {
            password.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        
        // Shuffle the string using the Fisher-Yates algorithm
        char[] array = password.toString().toCharArray();
        for (int i = array.length - 1; i > 0; i--) {
            int index = (int) (Math.random() * (i + 1));
            char temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
        
        return new String(array);
    }
}