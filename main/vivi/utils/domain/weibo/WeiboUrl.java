package vivi.utils.domain.weibo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import utils.Constants;
import vivi.net.WebClient;
import weibo4j.ShortUrl;
import weibo4j.model.WeiboException;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

public class WeiboUrl {
    private static final Logger logger = Logger.getLogger(WeiboUrl.class);
    
    private String shortUrl;
    private String longUrl;
    private int clicks;
    private int commentCount;
    private int shareCount;
    private ShortUrl su;
    private long requestTime;
    private long publishedTime;
    private String title;
    
    public WeiboUrl(String shortUrl) {
        this.su = new ShortUrl();
        this.su.client.setToken(Constants.ACCESS_TOKEN);
        
        this.shortUrl = shortUrl;
        this.longUrl = initLongUrls();
        this.clicks = initClicks();
        this.commentCount = initCommentCount();
        this.shareCount = initShareCount();
        this.requestTime = new Date().getTime();
    }
    
    public WeiboUrl(String shortUrl, long publishedTime) {
        this(shortUrl);
        this.publishedTime = publishedTime;
        this.title = getWebPageTitle(new WebClient().getContent(longUrl));
        logger.info(shortUrl + ": " + title);
    }
    
    public WeiboUrl(String shortUrl, String longUrl, int clicks, int commentCount, int shareCount, long requestTime, long publishedTime) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.clicks = clicks;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.requestTime = requestTime;
        this.publishedTime = publishedTime;
        this.title = getWebPageTitle(new WebClient().getContent(longUrl));
    }
    
    public WeiboUrl(String shortUrl, String longUrl, 
            int clicks, int commentCount, int shareCount, 
            long requestTime, long publishedTime, 
            Map<String, String> urlToTitle) {
        this.shortUrl = shortUrl;
        this.longUrl = longUrl;
        this.clicks = clicks;
        this.commentCount = commentCount;
        this.shareCount = shareCount;
        this.requestTime = requestTime;
        this.publishedTime = publishedTime;
        this.title = urlToTitle.get(longUrl);
    }
    
    private int initClicks() {
        try {
            return ((JSONObject) su.clicksOfUrl(shortUrl)
                    .getJSONArray("urls").get(0)).getInt("clicks");
        } catch (JSONException | WeiboException e) {
            logger.error("error when get clicks json: ", e);
        }
        
        return 0;
    }
    
    private String initLongUrls() {
        try {
            return ((JSONObject) su.shortToLongUrl(shortUrl)
                    .getJSONArray("urls").get(0)).getString("url_long");
        } catch (JSONException | WeiboException e) {
            logger.error("error when get clicks json: ", e);
        }
        
        return "";
    }
    
    private int initCommentCount() {
        try {
            return Integer.valueOf(((JSONObject) su.commentCountOfUrl(shortUrl)
                    .getJSONArray("urls").get(0)).getInt("comment_counts"));
        } catch (JSONException | WeiboException e) {
            logger.error("error when get clicks json: ", e);
        }
        
        return 0;
    }
    
    private int initShareCount() {
        try {
            return Integer.valueOf((((JSONObject) su.shareCountsOfUrl(shortUrl)
                    .getJSONArray("urls").get(0)).getInt("share_counts")));
        } catch (JSONException | WeiboException e) {
            logger.error("error when get clicks json: ", e);
        }
        
        return 0;
    }

    public String getTitle() {
        return title;
    }
    private String getWebPageTitle(final String s)  
    {  
        String regex;  
        String title = "";  
        final List<String> list = new ArrayList<String>();  
        regex = "<title>.*?</title>";  
        final Pattern pa = Pattern.compile(regex, Pattern.CANON_EQ);  
        final java.util.regex.Matcher ma = pa.matcher(s);  
        while (ma.find())  
        {  
            list.add(ma.group());  
        }  
        for (int i = 0; i < list.size(); i++)  
        {  
            title = title + list.get(i);  
        }  
        return title.replaceAll("<.*?>", "");
    }

    public String getShortUrl() {
        return shortUrl;
    }

    public int getClicks() {
        return clicks;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public long getPublishedTime() {
        return publishedTime;
    }

    @Override
    public String toString() {
        return "shortUrl=" + shortUrl + ", longUrl=" + longUrl + ", title=" + title
                + ", clicks=" + clicks + ", commentCount=" + commentCount
                + ", shareCount=" + shareCount + ", su=" + su
                + ", requestTime=" + requestTime + ", publishedTime="
                + publishedTime;
    }

    public static void main(String[] args) {
        System.out.println(new WeiboUrl("http://t.cn/zjZ2l8g"));
    }

}
