package se;

/**
 * @author frank
 *
 */
public class NLPUtils {
	
	public static Boolean isNoun(String token) {
		if (Character.isUpperCase(token.charAt(0))) {
			return true;
		} else {
			return false;
		}
	}

}
