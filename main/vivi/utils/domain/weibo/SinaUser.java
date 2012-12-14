package vivi.utils.domain.weibo;

import java.util.Date;

import org.apache.log4j.Logger;

import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

public class SinaUser {
    private static final Logger logger = Logger.getLogger(SinaUser.class);
    
    private long id;
    private String screenName;
    private int followersCount;
    private int friendsCount;
    private Date createdAt;
    private String description;
    
    public SinaUser(JSONObject json) {
        constructJson(json);
    }
    
    private void constructJson(JSONObject json) {
        try {
            this.id = json.getLong("id");
            this.screenName = json.getString("screen_name");
            this.followersCount = json.getInt("followers_count");
            this.friendsCount = json.getInt("friends_count");
            this.createdAt = WeiboUtil.parseDate(json.getString("created_at"), "EEE MMM dd HH:mm:ss z yyyy");
            this.description = json.getString("description");
        } catch (JSONException e) {
            logger.error(e.getMessage() + ":" + json.toString(), e);
        }
    }

    public long getId() {
        return id;
    }

    public String getScreenName() {
        return screenName;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "SinaUser [id=" + id + ", screenName=" + screenName
                + ", followersCount=" + followersCount + ", friendsCount="
                + friendsCount + ", createdAt=" + createdAt + ", description="
                + description + "]";
    }
    
}
