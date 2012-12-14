package vivi.utils.domain.weibo;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UrlInfo {

    private WeiboUrl weiboUrl;
    private double voteValue;
    private double t;
    private double score;
    private String text;
    private String originUrl;
    
    public UrlInfo(WeiboUrl url, String text, String originUrl) {
        this.weiboUrl = url;
        this.voteValue = url.getCommentCount() + url.getShareCount() + url.getClicks()*0.2;
        this.t = url.getRequestTime() - url.getPublishedTime();
        this.score = -1;
        this.text = text;
        this.originUrl = originUrl;
    }
    
    public double getScore() {
        if (score == -1) {
            calculateScore();
        }
        return score;
    }

    private void calculateScore() {
        score = voteValue / Math.pow(2 + t/TimeUnit.HOURS.toMillis(1), 1.8);
//        score = Math.log10(voteValue) + t/TimeUnit.HOURS.toMillis((long) 12.5);
    }
    
    public WeiboUrl getWeiboUrl() {
        return weiboUrl;
    }

    @Override
    public String toString() {
        return "【标题：" + weiboUrl.getTitle() + 
                "】 【统计数据：" + 
                new Date(weiboUrl.getPublishedTime()).toLocaleString() + 
                " " + weiboUrl.getShareCount() + " " + weiboUrl.getCommentCount() + 
                " " + weiboUrl.getClicks() + 
                "】 【文章链接：" +
                weiboUrl.getLongUrl() + 
                "】 【微博链接：" + originUrl +
                "】 【微博内容：" + text + "】"
                ;
    }
    
}
