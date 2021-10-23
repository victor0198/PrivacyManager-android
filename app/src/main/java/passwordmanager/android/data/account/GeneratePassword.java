package passwordmanager.android.data.account;

import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.passay.CharacterData;


public class GeneratePassword {

    private CharacterRule getLowerCharRules(int quantityRule) {
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(quantityRule);

        return lowerCaseRule;
    }

    private CharacterRule getUpperCharRules(int quantityRule) {
        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(quantityRule);

        return upperCaseRule;
    }

    private CharacterRule getDigitCharRules(int quantityRule) {
        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(quantityRule);

        return digitRule;
    }

    private CharacterRule getSpecialCharRules(int quantityRule) {
        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return "ERROR_CODE";
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule specialCharRule = new CharacterRule(specialChars);
        specialCharRule.setNumberOfCharacters(quantityRule);

        return specialCharRule;
    }

    /**
     * Gets the employee’s last name.
     *
     * @param passwordLength      length of the password
     * @param lowerCharQuantity   the number of lower-case characters in the password
     * @param upperCharQuantity   the number of upper-case characters in the password
     * @param digitQuantity       the number of digits in the password
     * @param specialCharQuantity the number of special characters in the password
     * @return Generates a password string
     */
    public String generateSecurePassword(int passwordLength, int lowerCharQuantity,
                                         int upperCharQuantity, int digitQuantity,
                                         int specialCharQuantity) {

        if (passwordLength != lowerCharQuantity + upperCharQuantity + digitQuantity + specialCharQuantity) {
            throw new IllegalArgumentException("The overall quantity of characters must be equal to the password length!");
        }

        PasswordGenerator passGen = new PasswordGenerator();
        CharacterRule lowerCaseRule = this.getLowerCharRules(lowerCharQuantity);
        CharacterRule upperCaseRule = this.getUpperCharRules(upperCharQuantity);
        CharacterRule digitRule = this.getDigitCharRules(digitQuantity);
        CharacterRule specialCharRule = this.getSpecialCharRules(specialCharQuantity);

        return passGen.generatePassword(passwordLength, specialCharRule, lowerCaseRule,
                upperCaseRule, digitRule);
    }
}
