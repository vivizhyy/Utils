package vivi.utils.domain.weibo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class WeiboUtil {
    private static final Logger logger = Logger.getLogger(WeiboUtil.class);
    private static Map<String,SimpleDateFormat> formatMap = new HashMap<String,SimpleDateFormat>();
    
    public static Date parseDate(String str, String format) {
        if(str==null||"".equals(str)){
                return null;
        }
        SimpleDateFormat sdf = formatMap.get(format);
        if (null == sdf) {
            sdf = new SimpleDateFormat(format, Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            formatMap.put(format, sdf);
        }
        try {
            synchronized(sdf){
                // SimpleDateFormat is not thread safe
                return sdf.parse(str);
            }
        } catch (ParseException e) {
            logger.error("Unexpected format(" + str + ") returned from sina.com.cn", e);
        }
        
        return null;
    }
    
}
