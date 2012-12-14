package utils.chars;

import java.nio.charset.Charset;

public class CharUtil {

    public static final String UTF8 = "UTF-8";
    public static final Charset UTF8_CHARSET = Charset.forName(UTF8);
    
    public static byte[] toBytes(String s) {
        return s.getBytes(UTF8_CHARSET);
    }
}
