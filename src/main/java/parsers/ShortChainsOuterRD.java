package parsers;

import java.util.Arrays;
import java.util.regex.Pattern;

public class ShortChainsOuterRD {
    /* Grammar:
        S := SExp <EOF>
        SExp = Atom | Pair
        Atom := Symbol | Number
        Pair := '(' SExp '.' SExp ')'
        Symbol := [a-zA-Z]+
        Number := [0-9]+
     */

    char[] string;
    int pos;

    public ShortChainsOuterRD(String string) {
        this.string = string.toCharArray();
        this.pos = 0;
    }

    boolean eof() {
        return pos == string.length;
    }

    boolean character(char c) {
        if (string[pos] == c) {
            pos++;
            return true;
        }
        return false;
    }

    boolean regex(String regex) {
        Pattern p = Pattern.compile(regex);
        String s = new String(Arrays.copyOfRange(string, pos, string.length));
        String[] r = p.split(s, 2);
        // pattern doesn't match
        if (r.length == 1) return false;
        String before = r[0];
        // pattern doesn't match at the beginning
        // TODO maybe put beginning of string-thingy in front of the pattern string?
        if (before.length() != 0) return false;
        String after = r[1];
        int matchedLength = s.length() - after.length();
        pos += matchedLength;
        return true;
    }

    public boolean s() {
        return eof() || (c0() && s());
    }

    boolean c0() { return e(); }

    boolean e() { return character('a'); }

    public static void main(String[] args) {
        ChainsOuterRD parser = new ChainsOuterRD("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        System.out.println(parser.s());
    }

    public void reset() {
        this.pos = 0;
    }
}


