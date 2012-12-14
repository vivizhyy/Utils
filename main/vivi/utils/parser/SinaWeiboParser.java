package vivi.utils.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import utils.Constants;
import utils.Util;
import utils.Util.Transformer;
import utils.chars.StringUtil;
import vivi.net.RequestRobot;
import vivi.utils.domain.weibo.SinaUser;
import vivi.utils.domain.weibo.SinaWeiboMediaType;
import vivi.utils.domain.weibo.UrlInfo;
import vivi.utils.domain.weibo.WeiboStatus;
import vivi.utils.domain.weibo.WeiboUrl;
import vivi.utils.file.FileUtil;
import weibo4j.ShortUrl;
import weibo4j.Timeline;
import weibo4j.model.Status;
import weibo4j.model.WeiboException;
import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

public class SinaWeiboParser {
    private static final Logger logger = Logger.getLogger(SinaWeiboParser.class);
    
    private static final String MEDIA_FILE_PATH = "sina-weibo-user-info-json 2012-12-05-21-12-45.txt";
    
    private List<SinaUser> medias;
    
    public SinaWeiboParser() {
//        try {
//            medias = getMedias(MEDIA_FILE_PATH);
//        } catch (JSONException e) {
//            logger.error("wrong with init medias", e);
//            medias = Collections.emptyList();
//        }
    }
    
    private List<WeiboStatus> getWeiboStatus(SinaWeiboMediaType type, String uid) {
        List<WeiboStatus> statuses = new ArrayList<>();
        for (WeiboStatus status : getStatusByMediaType(type)) {
            if (status.getWeiboUserId().equals(uid)) {
                statuses.add(status);
            }
        }
        
        return statuses;
    }
    
    private double getDecayedFactorByDate(long downloadTime, Date pulishDate) {
        final long HALF_LIFE_PERIOD = TimeUnit.HOURS.toMillis(24);
//        final long HALF_LIFE_PERIOD = TimeUnit.MINUTES.toMillis(30);
        double age = downloadTime - pulishDate.getTime();
        double t = age / HALF_LIFE_PERIOD;
        return Math.pow(2, -t);
    }
    
    private void decayedByFans(WeiboStatus status, SinaUser poster) {
        status.setBoost(getDecayedFactorByFans(poster.getFollowersCount()));
        status.clearScore();
    }
    
    private double getDecayedFactorByFans(int fansCount) {
        return Math.pow(2, -(Math.log10(fansCount) - 3));
    }
    
    private SinaUser getMedia(long uid) {
        for (SinaUser user : medias) {
            if (user.getId() == uid) {
                return user;
            }
        }
        return null;
    }
    
    private void writeTopAndTailStatuses(List<WeiboStatus> sortedStatuses) {
        int size = (int) (sortedStatuses.size() * 0.1);
        List<String> statuses = new ArrayList<>();
        statuses.add("created_at\tcomment_count\trepost_count\ttext");
        statuses.addAll(getSubWeiboStatus(sortedStatuses, 0, size - 1));
        FileUtil.writeContents(statuses, FileUtil.getTxtFileNameWithTimestamp("ends/top"));
        
        statuses.clear();
        statuses.add("created_at\tcomment_count\trepost_count\ttext");
        statuses.addAll(getSubWeiboStatus(sortedStatuses, sortedStatuses.size() - size, sortedStatuses.size() - 1));
        FileUtil.writeContents(statuses, FileUtil.getTxtFileNameWithTimestamp("ends/tail"));
    }
    
    private List<String> getSubWeiboStatus(List<WeiboStatus> sortedStatuses, int fromIdx, int toIds) {
        return Util.transformList(sortedStatuses.subList(fromIdx, toIds), new Transformer<WeiboStatus, String>(){

            @Override
            public String transform(WeiboStatus weiboStatus) {
                System.out.println(weiboStatus.getCommentsCount() + "\t" + weiboStatus.getRepostsCount() + "\t" + weiboStatus.getScore());
                return weiboStatus.getCreatedAt().toLocaleString() + "\t" + 
                        weiboStatus.getCommentsCount() + "\t" + 
                        weiboStatus.getRepostsCount() + "\t"+ 
                        weiboStatus.getText();
            }});
    }
    
    private List<SinaUser> getMedias(String filePath) throws JSONException {
        List<SinaUser> medias = new ArrayList<>();
        for (String line : FileUtil.getLines(filePath)) {
            medias.add(new SinaUser(new JSONObject(line)));
        }
        return medias;
    }
    
    private List<WeiboStatus> getStatusByMediaType(SinaWeiboMediaType type) {
        List<String> uids = type.getUids();
        //      System.out.println("id\tcomment_c\trepost_c\tscore\t");
        List<WeiboStatus> statuses = new ArrayList<>();
//        long downloadTime = 0;
        String userId="";
        String filePath = Constants.TIMELINE_DIR + type.toString() + "/";
        for (String uidFilename : new File(filePath).list()) {
            filePath += uidFilename;
            String[] tokens = uidFilename.split("-");
            userId = tokens[0];
//            downloadTime = Long.parseLong(tokens[1]);
            
            try {
                for (String line : FileUtil.getLines(filePath)) {
                    WeiboStatus status = new WeiboStatus(new JSONObject(line));
                    status.setWeiboUserId(userId);
                    if (status.containsLink()) {
                        statuses.add(status);
                    }
                }
            } catch (JSONException e) {
                logger.error("json exception: ", e);
            }
            filePath = Constants.TIMELINE_DIR + type.toString() + "/";
        } 

        logger.info("get all weibo status");
        return statuses;
    }
    
    public List<String> getStatusShortUrl(SinaWeiboMediaType type) {
        List<WeiboStatus> statuses = getStatusByMediaType(type);
        List<String> shortUrls = new ArrayList<>();
        for (WeiboStatus status : statuses) {
            Matcher matcher = Pattern.compile("http://t\\.cn/[a-zA-Z0-9]+").matcher(status.getText());
            if (matcher.find()) {
                shortUrls.add(matcher.group());
            }
        }
        return shortUrls;
    }
    
    private Map<String, Integer> getUrlToClicks() throws JSONException {
        String filePath = "clicks-json-1354856901794";
        List<String> lines = FileUtil.getLines(filePath);
        Map<String, Integer> urlToClicks = new HashMap<>();
/*        for (String line : lines) {
            CliksOfUrl clickOfUrl = new CliksOfUrl(new JSONObject(line));
            urlToClicks.put(clickOfUrl.getShortUrl(), clickOfUrl.getClicks());
        }
        */
        return urlToClicks;
    }
    
    private void writeNewStatus() {
        final String weiboIdPath = "tech-status-id";
        List<String> ids = FileUtil.getLines(weiboIdPath);
        Timeline tm = new Timeline();
        tm.client.setToken(Constants.ACCESS_TOKEN);
        ShortUrl su = new ShortUrl();
        su.client.setToken(Constants.ACCESS_TOKEN);
        List<String> statuses = new ArrayList<>();
        for (String id : ids) {
            try {
                Status status = tm.showStatus(id);
                String shortUrl = getShortUrl(status.getText());
                if (shortUrl != null) {
                    JSONObject jo = (JSONObject) su.clicksOfUrl(shortUrl).getJSONArray("urls").get(0);

                    statuses.add(status.getCreatedAt() + "\t" + 
                            status.getId() + "\t" + 
                            status.getText() + "\t" + 
                            status.getRepostsCount() + "\t" +
                            status.getCommentsCount() + "\t" +
                            jo.getInt("clicks"));
                }
                Thread.sleep(TimeUnit.SECONDS.toMillis(3));
            } catch (WeiboException e) {
                FileUtil.writeContents(statuses, FileUtil.getFileNameWithTimestampLong("tech-status-with-click"));
                e.printStackTrace();
            } catch (JSONException e) {
                FileUtil.writeContents(statuses, FileUtil.getFileNameWithTimestampLong("tech-status-with-click"));
                e.printStackTrace();
            } catch (InterruptedException e) {
                FileUtil.writeContents(statuses, FileUtil.getFileNameWithTimestampLong("tech-status-with-click"));
                e.printStackTrace();
            }
        }
        FileUtil.writeContents(statuses, FileUtil.getFileNameWithTimestampLong("tech-status-with-click"));
    }
    
    private String getShortUrl(String text) {
        Matcher matcher = Pattern.compile("http://t\\.cn/[a-zA-Z0-9]+").matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
    
    private List<WeiboStatus> getStatusWithClick() {
        final String statusesWithClickPath = "tech-status-with-click-1354865964022";
        List<WeiboStatus> statuses = new ArrayList<>();
        for (String line : FileUtil.getLines(statusesWithClickPath)) {
            statuses.add(new WeiboStatus(line));
        }
        return statuses;
    }
    
    private Map<String, WeiboUrl> getShortUrlToWeiboUrls(List<WeiboStatus> weiboStatuses) {
        Map<String, WeiboUrl> shortUrlToweiboUrls = new HashMap<>();
        for (WeiboStatus weiboStatus : weiboStatuses) {
            if (!StringUtil.isBlank(weiboStatus.getShortLink())) {
                logger.info("get " + weiboStatus.getShortLink());
                WeiboUrl url = new WeiboUrl(weiboStatus.getShortLink(), 
                        weiboStatus.getCreatedAt().getTime());
                shortUrlToweiboUrls.put(weiboStatus.getShortLink(), url);
            }
        }
        FileUtil.writeContents(shortUrlToweiboUrls.values(), 
                FileUtil.getFileNameWithTimestampLong("short-urls"));
        return shortUrlToweiboUrls;
    }
    
    private WeiboUrl getWeiboUrlByLine(String line) {
        Map<String, String> urlToTitle = getUrlToTitle();
        String[] tokens = line.split(", ");
        return new WeiboUrl(getValue(tokens[0]), getValue(tokens[1]), 
                getIntValue(tokens[2]),
                getIntValue(tokens[3]),
                getIntValue(tokens[4]),
                getLongValue(tokens[5]),
                getLongValue(tokens[6]), 
                urlToTitle);
    }
    
    private String getValue(String token) {
        return token.split("=")[1];
    }
    
    private int getIntValue(String token) {
        return Integer.valueOf(getValue(token));
    }
    
    private long getLongValue(String token) {
        return Long.valueOf(getValue(token));
    }
    
    private void writeSortedUrlInfos(List<WeiboStatus> weiboStatuses, SinaWeiboMediaType type) {
        List<UrlInfo> urlInfos = new ArrayList<>();
        List<WeiboUrl> weiboUrls = new ArrayList<>();
        Set<String> shortLinks = new HashSet<>();
        for (WeiboStatus status : weiboStatuses) {
            String mid = status.getMid();
            String weiboUrl = "weibo.com/" + status.getWeiboUserId() + "/" + mid;
            String shortLink = status.getShortLink();
            if (!StringUtil.isBlank(shortLink) && shortLinks.add(shortLink)) {
                logger.info("status:" + mid + "\nget " + shortLink);
                WeiboUrl url = new WeiboUrl(shortLink, 
                        status.getCreatedAt().getTime());
                weiboUrls.add(url);
                urlInfos.add(new UrlInfo(url, status.getText(), weiboUrl));
            }
        }

        FileUtil.writeContents(weiboUrls, 
                FileUtil.getFileNameWithTimestampLong(type.toString() + "-weibo-urls"));
        logger.info("start sort.");
        Collections.sort(urlInfos, new Comparator<UrlInfo>(){

            @Override
            public int compare(UrlInfo o1, UrlInfo o2) {
                double result = o1.getScore() - o2.getScore();
                if (result < 0) {
                    return 1;
                } else if (result == 0) {
                    return 0;
                }
                return -1;
            }});
        
        FileUtil.writeContents(urlInfos, 
                FileUtil.getFileNameWithTimestampLong(type.toString() + "-url-infos"));
    }
    
    
    /*private String writeSortedUrlInfos(SinaWeiboMediaType type) {
        List<UrlInfo> urlInfos = new ArrayList<>();
        List<String> urlLines = FileUtil.getLines("short-urls-info-1355118399796");
        for (WeiboStatus status : getStatusByMediaType(type)) {
            String line = "";
            for (String urlLine : urlLines) {
                if (urlLine.contains(status.getShortLink())) {
                    line = urlLine;
                    break;
                }
            }
            if (!StringUtil.isBlank(line)) {
                String weiboUrl = "weibo.com/" + status.getWeiboUserId() + "/" + status.getMid();
                urlInfos.add(new UrlInfo(getWeiboUrlByLine(line), status.getText(), weiboUrl));
            } else {
                logger.error(status.getShortLink());
            }
        }
        Collections.sort(urlInfos, new Comparator<UrlInfo>(){

            @Override
            public int compare(UrlInfo o1, UrlInfo o2) {
                double result = o1.getScore() - o2.getScore();
                if (result < 0) {
                    return 1;
                } else if (result == 0) {
                    return 0;
                }
                return -1;
            }});
        String filePath = FileUtil.getFileNameWithTimestampLong("sorted-url-info");
        FileUtil.writeContents(urlInfos, filePath);
        return filePath;
    }*/
    
    private Map<String, String> getUrlToTitle() {
        final String filePath = "title-url.txt";
        List<String> lines = FileUtil.getLines(filePath);
        Map<String, String> urlToTitle = new HashMap<>();
        for (String line : lines) {
            String token[] = line.split("  ");
            if (token.length == 2) {
                urlToTitle.put(token[1].trim(), token[0].trim());
            }
        }
        return urlToTitle;
    }
    
    public static void main(String[] args) throws InterruptedException {
        PropertyConfigurator.configure("log4j.properties");
        SinaWeiboMediaType type = SinaWeiboMediaType.CURRENT_EVENTS;
        
        logger.info("start request timeline: " + new Date().toLocaleString());
        RequestRobot robot = new RequestRobot();
//        robot.writeTimeline(type);
        
        SinaWeiboParser parser = new SinaWeiboParser();
        
        logger.info("start request urlInfo: " + new Date().toLocaleString());
        parser.writeSortedUrlInfos(parser.getStatusByMediaType(type), type);
        
    }
}
