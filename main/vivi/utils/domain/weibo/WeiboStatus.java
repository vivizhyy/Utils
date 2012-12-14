package vivi.utils.domain.weibo;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import utils.Constants;
import utils.chars.StringUtil;
import weibo4j.Timeline;
import weibo4j.examples.oauth2.Log;
import weibo4j.model.WeiboException;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

/**
 * @author yyzhang
 *
 */
public class WeiboStatus {
    private static final Logger logger = Logger.getLogger(WeiboStatus.class);
    
    private Date createdAt;
    private long id;
    private String text;
    private int repostsCount;
    private int commentsCount;
    private double boost;
    private double score;
    private int click;
    private Matcher matcher;
    private String weiboUrl;
    private String weiboUserId;
    
    
    public WeiboStatus(String line) {
        if (!StringUtil.isBlank(line)) {
            String[] tokens = line.split("\t");
            if (tokens.length == 6) {
                this.createdAt = WeiboUtil.parseDate(tokens[0], "EEE MMM dd HH:mm:ss zzz yyyy");
                this.id = Long.valueOf(tokens[1]);
                this.text = tokens[2];
                this.repostsCount = Integer.valueOf(tokens[3]);
                this.commentsCount = Integer.valueOf(tokens[4]);
                this.click = Integer.valueOf(tokens[5]);
            }
        }
        this.boost = 1;
        this.score = -1;
    }
    public WeiboStatus(JSONObject json) {
        constructJson(json);
        matcher = Pattern.compile("http://t\\.cn/[a-zA-Z0-9]+").matcher(text);
        this.boost = 1.0;
        this.score = -1;
    }
    
    public WeiboStatus(JSONObject json, int click) {
        this(json);
        this.click = click;
    }

    private void constructJson(JSONObject json) {
        try {
            this.createdAt = WeiboUtil.parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
            id = json.getLong("id");
            this.text = json.getString("text");
            this.repostsCount = json.getInt("reposts_count");
            this.commentsCount = json.getInt("comments_count");
            this.click = json.getInt("clicks");
        } catch (JSONException e) {
            logger.error(e.getMessage() + ":" + json.toString(), e);
        }
    }
    public String getShortLink() {
        return matcher.group();
    }
    
    public boolean containsLink() {
        return matcher.find();
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getText() {
        return text;
    }

    public int getRepostsCount() {
        return repostsCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public double getBoost() {
        return boost;
    }

    public void setBoost(double boost) {
        this.boost *= boost;
        clearScore();
    }

    public long getId() {
        return id;
    }
    
    public double getScore() {
        if (score == -1) {
            score = calculateScore();
        }
        return score;
    }

    public void clearScore() {
        score = -1;
    }

    private double calculateScore() {
        double score = 0;
//        score += repostsCount + commentsCount*2;
        score += repostsCount + commentsCount;
        return score *  boost;
    }

    public int getClick() {
        return click;
    }

    public void setClick(int click) {
        this.click = click;
    }

    public String getWeiboUrl() {
        return weiboUrl;
    }
    public void setWeiboUrl(String weiboUrl) {
        this.weiboUrl = weiboUrl;
    }
    public String getWeiboUserId() {
        return weiboUserId;
    }
    public void setWeiboUserId(String weiboUserId) {
        this.weiboUserId = weiboUserId;
    }
    
    public String getMid() {
        String access_token = Constants.ACCESS_TOKEN;
        Timeline tm = new Timeline();
        tm.client.setToken(access_token);
        try {
            return tm.QueryMid( 1, String.valueOf(id)).getString("mid");
        } catch (WeiboException | JSONException e) {
            logger.error(id, e);
        } 
        
        return "";
    }
    
    @Override
    public String toString() {
        return "WeiboStatus [createdAt=" + createdAt + ", id=" + id + ", text="
                + text + ", repostsCount=" + repostsCount + ", commentsCount="
                + commentsCount + ", boost=" + boost + ", score=" + score + "]";
    }
    
}
