package cn.yxffcode.datasource;

import java.io.IOException;
import java.io.StringReader;

/**
 * 字符串处理相关类。
 */
public class StringUtils {
  public static boolean equalsIgnoreCase(final CharSequence str1, final CharSequence str2) {
    if (str1 == null || str2 == null) {
      return str1 == str2;
    } else if (str1 == str2) {
      return true;
    } else if (str1.length() != str2.length()) {
      return false;
    } else {
      return regionMatches(str1, true, 0, str2, 0, str1.length());
    }
  }

  /**
   * Green implementation of regionMatches.
   *
   * @param cs         the {@code CharSequence} to be processed
   * @param ignoreCase whether or not to be case insensitive
   * @param thisStart  the index to start on the {@code cs} CharSequence
   * @param substring  the {@code CharSequence} to be looked for
   * @param start      the index to start on the {@code substring} CharSequence
   * @param length     character length of the region
   * @return whether the region matched
   */
  static boolean regionMatches(final CharSequence cs, final boolean ignoreCase, final int thisStart,
                               final CharSequence substring, final int start, final int length) {
    if (cs instanceof String && substring instanceof String) {
      return ((String) cs).regionMatches(ignoreCase, thisStart, (String) substring, start, length);
    } else {
      int index1 = thisStart;
      int index2 = start;
      int tmpLen = length;

      while (tmpLen-- > 0) {
        char c1 = cs.charAt(index1++);
        char c2 = substring.charAt(index2++);

        if (c1 == c2) {
          continue;
        }

        if (!ignoreCase) {
          return false;
        }

        // The same check as in String.regionMatches():
        if (Character.toUpperCase(c1) != Character.toUpperCase(c2)
            && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
          return false;
        }
      }

      return true;
    }
  }

  /**
   * Determines whether or not the sting 'searchIn' contains the string
   * 'searchFor', disregarding case and leading whitespace
   *
   * @param searchIn  the string to search in
   * @param searchFor the string to search for
   * @return true if the string starts with 'searchFor' ignoring whitespace
   */
  public static boolean startsWithIgnoreCaseAndWs(String searchIn,
                                                  String searchFor) {
    return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
  }

  /**
   * Determines whether or not the sting 'searchIn' contains the string
   * 'searchFor', disregarding case and leading whitespace
   *
   * @param searchIn  the string to search in
   * @param searchFor the string to search for
   * @param beginPos  where to start searching
   * @return true if the string starts with 'searchFor' ignoring whitespace
   */
  private static boolean startsWithIgnoreCaseAndWs(String searchIn,
                                                   String searchFor, int beginPos) {
    if (searchIn == null) {
      return searchFor == null;
    }

    int inLength = searchIn.length();

    for (; beginPos < inLength; beginPos++) {
      if (!Character.isWhitespace(searchIn.charAt(beginPos))) {
        break;
      }
    }

    return startsWithIgnoreCase(searchIn, beginPos, searchFor);
  }

  /**
   * Determines whether or not the string 'searchIn' contains the string
   * 'searchFor', dis-regarding case starting at 'startAt' Shorthand for a
   * String.regionMatch(...)
   *
   * @param searchIn  the string to search in
   * @param startAt   the position to start at
   * @param searchFor the string to search for
   * @return whether searchIn starts with searchFor, ignoring case
   */
  private static boolean startsWithIgnoreCase(String searchIn, int startAt,
                                              String searchFor) {
    return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor
        .length());
  }

  /**
   * Returns the given string, with comments removed
   *
   * @param src                the source string
   * @param stringOpens        characters which delimit the "open" of a string
   * @param stringCloses       characters which delimit the "close" of a string, in
   *                           counterpart order to <code>stringOpens</code>
   * @param slashStarComments  strip slash-star type "C" style comments
   * @param slashSlashComments strip slash-slash C++ style comments to end-of-line
   * @param hashComments       strip #-style comments to end-of-line
   * @param dashDashComments   strip "--" style comments to end-of-line
   * @return the input string with all comment-delimited data removed
   */
  public static String stripComments(String src, String stringOpens,
                                     String stringCloses, boolean slashStarComments,
                                     boolean slashSlashComments, boolean hashComments,
                                     boolean dashDashComments) {
    if (src == null) {
      return null;
    }

    StringBuilder builder = new StringBuilder(src.length());

    // It's just more natural to deal with this as a stream
    // when parsing..This code is currently only called when
    // parsing the kind of metadata that developers are strongly
    // recommended to cache anyways, so we're not worried
    // about the _1_ extra object allocation if it cleans
    // up the code

    StringReader sourceReader = new StringReader(src);

    int contextMarker = Character.MIN_VALUE;
    boolean escaped = false;
    int markerTypeFound = -1;

    int ind = 0;

    int currentChar = 0;

    try {
      while ((currentChar = sourceReader.read()) != -1) {

        if (false && currentChar == '\\') {
          escaped = !escaped;
        } else if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound)
            && !escaped) {
          contextMarker = Character.MIN_VALUE;
          markerTypeFound = -1;
        } else if ((ind = stringOpens.indexOf(currentChar)) != -1 && !escaped
            && contextMarker == Character.MIN_VALUE) {
          markerTypeFound = ind;
          contextMarker = currentChar;
        }

        if (contextMarker == Character.MIN_VALUE && currentChar == '/'
            && (slashSlashComments || slashStarComments)) {
          currentChar = sourceReader.read();
          if (currentChar == '*' && slashStarComments) {
            int prevChar = 0;
            while ((currentChar = sourceReader.read()) != '/'
                || prevChar != '*') {
              if (currentChar == '\r') {

                currentChar = sourceReader.read();
                if (currentChar == '\n') {
                  currentChar = sourceReader.read();
                }
              } else {
                if (currentChar == '\n') {

                  currentChar = sourceReader.read();
                }
              }
              if (currentChar < 0)
                break;
              prevChar = currentChar;
            }
            continue;
          } else if (currentChar == '/' && slashSlashComments) {
            while ((currentChar = sourceReader.read()) != '\n'
                && currentChar != '\r' && currentChar >= 0)
              ;
          }
        } else if (contextMarker == Character.MIN_VALUE
            && currentChar == '#' && hashComments) {
          // Slurp up everything until the newline
          while ((currentChar = sourceReader.read()) != '\n'
              && currentChar != '\r' && currentChar >= 0)
            ;
        } else if (contextMarker == Character.MIN_VALUE
            && currentChar == '-' && dashDashComments) {
          currentChar = sourceReader.read();

          if (currentChar == -1 || currentChar != '-') {
            builder.append('-');

            if (currentChar != -1) {
              builder.append(currentChar);
            }

            continue;
          }

          // Slurp up everything until the newline

          while ((currentChar = sourceReader.read()) != '\n'
              && currentChar != '\r' && currentChar >= 0)
            ;
        }

        if (currentChar != -1) {
          builder.append((char) currentChar);
        }
      }
    } catch (IOException ioEx) {
      // we'll never see this from a StringReader
    }

    return builder.toString();
  }

}
