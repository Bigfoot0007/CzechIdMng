package eu.bcvsolutions.idm.core.api.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import org.identityconnectors.common.StringUtil;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.PasswordGenerate;

/**
 * Password generator
 * 
 * @author Ondrej Kopr
 */

public class PasswordGenerator {
	
	public static final String LOWER_CHARACTERS = "abcdefghijklmnopqrstuvwxyz";
	
	public static final String UPPER_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static final String SPECIAL_CHARACTERS = "!@#$%&*";
	
	public static final String NUMBERS = "0123456789";
	
	private static final int ROLLS = 6;
	
	private static final String PATH = "eu/bcvsolutions/idm/core/diceware";
	
	private static final String DEFAULT_DICEWARE = "cs";
	
	private static final String DEFAULT_FILE_TYPE = "csv";
	
	private static final String DEFAULT_DELIMITER = " ";
	
	private static final int DEFAULT_PASSWORD_LENGTH = 12;

	private Map<Integer, Long> filePosition;
	
	public String generatePassphrase(PasswordGenerate policy) {
		Assert.notNull(policy, "Password policy can't be null.");
		
		int passphraseLength = policy.getPassphraseWords() == null ? 0 : policy.getPassphraseWords().intValue();
		List<String> password = new ArrayList<>(passphraseLength);
		
		for (int index = 0; index < passphraseLength; index++) {
			
			StringBuilder number = new StringBuilder();
			for (int roll = 1; roll < ROLLS; roll++) {
				number.append(getRandomNumber(1, 6));
			}
			password.add(getWordFromDictonary(Integer.parseInt(number.toString()), DEFAULT_DICEWARE));
		}
		
		return String.join(DEFAULT_DELIMITER, password);
	}
	
	/**
	 * Method create password by given password policy that implements interface PasswordGenerate
	 * 
	 * @param policy
	 * @return
	 */
	public String generateRandom(PasswordGenerate policy) {
		Assert.notNull(policy, "Password policy can't be null.");		
		// policy may contains null values
		int minimumLength = policy.getMinPasswordLength() == null ? 0 : policy.getMinPasswordLength().intValue();

		int minimumLengthAll = policy.getMinLowerChar() == null ? 0 : policy.getMinLowerChar().intValue(); 
		minimumLengthAll += policy.getMinUpperChar() == null ? 0 : policy.getMinUpperChar().intValue();
		minimumLengthAll += policy.getMinSpecialChar() == null ? 0 : policy.getMinSpecialChar().intValue();
		minimumLengthAll += policy.getMinNumber() == null ? 0 : policy.getMinNumber().intValue();

		// defensive set of max length
		int maximumLength = 0;
		if (policy.getMaxPasswordLength() != null) {
			// if is defined maximum password length in policy use it
			maximumLength = policy.getMaxPasswordLength().intValue();
		} else {
			// set higher value of minimal lengths
			maximumLength = minimumLength > minimumLengthAll ? minimumLength : minimumLengthAll;
		}
		
		// if maximum and minimum length is both zero use default password length (defensive behavior)
		if (maximumLength == 0 && minimumLength == 0) {
			maximumLength = DEFAULT_PASSWORD_LENGTH;
			minimumLength = DEFAULT_PASSWORD_LENGTH;
		}
		
		if (maximumLength < minimumLength) {
			throw new IllegalArgumentException("Parameter: maxLength can't be lower than parameter: minLength");
		}
		
		
		if (maximumLength < minimumLengthAll) {
			throw new IllegalArgumentException("Parameter: maxLength must be higer or same as sum minimal length of all base.");
		}

		StringBuilder password = new StringBuilder();
		StringBuilder base = new StringBuilder();
		String prohibited = policy.getProhibitedCharacters();
		
		// set bases for generating
		String lowerBase = policy.getLowerCharBase();
		String upperBase = policy.getUpperCharBase();
		String specialBase = policy.getSpecialCharBase();
		String numberBase = policy.getNumberBase();
		
		// generate minimal requirements
		if (policy.getMinLowerChar() != null && policy.getMinLowerChar() != 0) {
			String lower = removeProhibited(lowerBase, prohibited);
			password.append(getRandomChars(lower, policy.getMinLowerChar(), null));
			base.append(lower);
		}
		if (policy.getMinUpperChar() != null && policy.getMinUpperChar() != 0) {
			String upper = removeProhibited(upperBase, prohibited);
			password.append(getRandomChars(upper, policy.getMinUpperChar(), null));
			base.append(upper);
		}
		if (policy.getMinSpecialChar() != null && policy.getMinSpecialChar() != 0) {
			String special = removeProhibited(specialBase, prohibited);
			password.append(getRandomChars(special, policy.getMinSpecialChar(), prohibited));
			base.append(special);
		}
		if (policy.getMinNumber() != null && policy.getMinNumber() != 0) {
			String number = removeProhibited(numberBase, prohibited);
			password.append(getRandomChars(number, policy.getMinNumber(), prohibited));
			base.append(number);
		}
		
		// add final string to password 
		int missingLength = getRandomNumber(minimumLength - password.length(), maximumLength - password.length());
		if (missingLength > 0) {
			if (base.length() == 0) {
				base.append(policy.getLowerCharBase());
				base.append(policy.getUpperCharBase());
				base.append(policy.getSpecialCharBase());
				base.append(policy.getNumberBase());
				base = new StringBuilder(removeProhibited(base.toString(), policy.getProhibitedCharacters()));
			}
			password.append(getRandomChars(shuffle(base).toString(), missingLength, policy.getProhibitedCharacters()));
		}
		
		return shuffle(password).toString();
	}
	
	/**
	 * Generate default password:
	 * 5 characters from lower,
	 * 1 characters from upper,
	 * 1 characters from special,
	 * 1 characters from number
	 * For generating will be use character base defined by this class.
     *
	 * @return
	 */
	public String generateRandom() {
		StringBuilder password = new StringBuilder();
		password.append(getRandomNumbers(1));
		password.append(getRandomSpecialCharacters(1));
		password.append(getRandomLowerCharacters(5));
		password.append(getRandomUpperCharacters(1));
		return shuffle(password).toString();
	}
	
	/**
	 * Generate password by given parameters.
	 * If you want to skip some category set parameters for category to null.
	 * For generating will be use user defined character base.
	 * 
	 * @param minLength minimal length for password
	 * @param maxLength maximal length for password
	 * @param lowerMin minimal characters from lower category
	 * @param lowerBase lower character that will be use for generating
	 * @param upperMin minimal characters from upper category
	 * @param upperBase upper character that will be use for generating
	 * @param specialMin minimal characters from special category
	 * @param specialBase special character that will be use for generating
	 * @param numberMin minimal characters from number category
	 * @param numberBase numbers that will be use for generating
	 * @param prohibited
	 * @return new password
	 */
	public String generateRandom(
			Integer minLength,Integer maxLength,
			Integer lowerMin, String lowerBase,
			Integer upperMin, String upperBase,
			Integer specialMin, String specialBase,
			Integer numberMin, String numberBase, String prohibited) {
		
		return this.generateRandom(new PasswordGenerate() {
			
			@Override
			public String getUpperCharBase() {
				return upperBase;
			}
			
			@Override
			public String getSpecialCharBase() {
				return specialBase;
			}
			
			@Override
			public String getProhibitedCharacters() {
				return prohibited;
			}
			
			@Override
			public Integer getPassphraseWords() {
				return 0;
			}
			
			@Override
			public String getNumberBase() {
				return numberBase;
			}
			
			@Override
			public Integer getMinUpperChar() {
				return upperMin;
			}
			
			@Override
			public Integer getMinSpecialChar() {
				return specialMin;
			}
			
			@Override
			public Integer getMinPasswordLength() {
				return minLength;
			}
			
			@Override
			public Integer getMinNumber() {
				return numberMin;
			}
			
			@Override
			public Integer getMinLowerChar() {
				return lowerMin;
			}
			
			@Override
			public Integer getMaxPasswordLength() {
				return maxLength;
			}
			
			@Override
			public String getLowerCharBase() {
				return lowerBase;
			}
		});
	}
	
	/**
	 * Generate password by given parameters.
	 * If you want to skip some category set parameters for category to null.
	 * For generating will be use default characters defined by this class.
	 * 
	 * @param minLength minimal length for password
	 * @param maxLength maximal length for password
	 * @param lowerMin minimal characters from lower category
	 * @param upperMin minimal characters from upper category
	 * @param specialMin minimal characters from special category
	 * @param numberMin minimal characters from number category
	 * @param prohibited string with character/s which will be skipped
	 * @return new password
	 */
	public String generateRandom(
			Integer minLength,Integer maxLength,
			Integer lowerMin, Integer upperMin,
			Integer specialMin,	Integer numberMin,
			String prohibited) {
		return generateRandom(
				minLength, maxLength, lowerMin,
				LOWER_CHARACTERS, upperMin,
				UPPER_CHARACTERS, specialMin,
				SPECIAL_CHARACTERS, numberMin,
				NUMBERS, prohibited);
	}
	
	/**
	 * Generate password by given parameters.
	 * If you want to skip some category set parameters for category to null.
	 * For generating will be use default characters defined by this class.
	 * 
	 * @param minLength minimal length for password
	 * @param maxLength maximal length for password
	 * @param lowerMin minimal characters from lower category
	 * @param upperMin minimal characters from upper category
	 * @param specialMin minimal characters from special category
	 * @param numberMin minimal characters from number category
	 * @return new password
	 */
	public String generateRandom(
			Integer minLength, Integer maxLength,
			Integer lowerMin, Integer upperMin,
			Integer specialMin, Integer numberMin) {
		return generateRandom(
				minLength, maxLength, lowerMin,
				upperMin, specialMin, numberMin, null);
	}
	
	private StringBuilder shuffle(StringBuilder string) {
		char[] charArray = string.toString().toCharArray();
		List<Character> list = new ArrayList<>(charArray.length);
        for (char character : charArray){
        	list.add(character);
        }
        
        StringBuilder result = new StringBuilder(string.length());
        
        while(!list.isEmpty()){
            result.append(list.remove(getRandomNumber(0, list.size() - 1)));
        }
        
        return result;
	}
	
	private StringBuilder getRandomNumbers(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomNumber(0, 9));
		}
		return password;
	}
	
	private StringBuilder getRandomSpecialCharacters(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomSpecialCharacter());
		}
		return password;
	}
	
	private StringBuilder getRandomUpperCharacters(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomUpperCharacter());
		}
		return password;
	}
	
	
	private StringBuilder getRandomLowerCharacters(int max) {
		StringBuilder password = new StringBuilder();
		for (int index = 0; index < max; index++) {
			password.append(getRandomLowerCharacter());
		}
		return password;
	}
	
	private char getRandomSpecialCharacter() {
		return getRandomChar(SPECIAL_CHARACTERS);
	}
	
	private char getRandomUpperCharacter() {
		return getRandomChar(UPPER_CHARACTERS);
	}
	
	private char getRandomLowerCharacter() {
		return getRandomChar(LOWER_CHARACTERS);
	}
	
	private char getRandomChar(String string) {
		return string.charAt(getRandomNumber(0, string.length() - 1));
	}
	
	private StringBuilder getRandomChars(String base, int length, String prohibited) {
		StringBuilder result = new StringBuilder();
		// if base is empty return empty result 
		if (StringUtil.isEmpty(base)) {
			return result;
		}
		base = removeProhibited(base, prohibited);
		
		for (int index = 0; index < length; index++) {
			result.append(getRandomChar(base));
		}
		return result;
	}
	
	private String removeProhibited(String string, String prohibited) {
		if (prohibited != null) {
			 for (Character character : prohibited.toCharArray()) {
				 string = string.replaceAll(Pattern.quote(character.toString()), "");
			 }
			 string = string.trim();
		}
		return string;
	}
	
	private int getRandomNumber(int min, int max) {
		// Better random generator?
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}
	
	private void loadDicewareDictonary(String localization) {
		if (this.filePosition == null) {
			this.filePosition = new HashMap<>();
		}
		
		String fileName = exportResource("diceware_" + localization + "." + DEFAULT_FILE_TYPE, PATH);
		
		String line = null;
		try {
			
			
	        File file = new File(fileName);
			RandomAccessFile randFile = new RandomAccessFile(file, "r");
			
			long offset = randFile.getFilePointer();
			while((line = randFile.readLine()) != null) {
				String[] splitLine = line.split(" ", 2);
				filePosition.put(Integer.valueOf(splitLine[0]), offset);
				offset = randFile.getFilePointer();
			}
			
			randFile.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Diceware IO exception, see log.", e);
		}
	}
	
	private String exportResource(String resourceFileName, String path) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(path + "/" + resourceFileName);
        String newFile = System.getProperty("java.io.tmpdir") + "/tmp_idm_" + resourceFileName;
        try {
        	byte[] buffer = new byte[4096];
        	int readBytes;
        	
			OutputStream outputStream = new FileOutputStream(newFile);
			while ((readBytes = is.read(buffer)) > 0) {
				outputStream.write(buffer, 0, readBytes);
            }
			outputStream.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Diceware IO exception, see log.", e);
		}
        return newFile;
	}
	
	private String getWordFromDictonary(int rolls, String localization) {
		if (this.filePosition == null || this.filePosition.isEmpty()) {
			this.loadDicewareDictonary(DEFAULT_DICEWARE);
		}
		
		String word;
		
		try {
			String fileName = System.getProperty("java.io.tmpdir") + "/tmp_idm_" + "diceware_" + localization + "." + DEFAULT_FILE_TYPE;
			RandomAccessFile randFile = new RandomAccessFile(new File(fileName), "r");

			randFile.seek(this.filePosition.get(rolls));
			
            String[] line = randFile.readLine().split(" ", 2);
            ;
			word = line[1];
			
			randFile.close();
		} catch (IOException e) {
			throw new IllegalArgumentException("Diceware IO exception, see log.", e);
		}

		return word;
	}
}
