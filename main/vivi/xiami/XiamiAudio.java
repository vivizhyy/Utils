package vivi.xiami;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.FieldDataInvalidException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;

import utils.chars.StringUtil;
import vivi.net.WebClient;

public class XiamiAudio {

    private static String audioUrl;
    private static String mp3NameFromFile;
    private MP3File mp3File;
    private static String mp3Id;
    private Tag tag;
    private String title;
    private String album;
    private String artist;
    private static String xiamiWikiUrl;
    
    private static final Pattern TITLE_PATTERN = Pattern.compile("<h1>([^<]+)</h1>");
    private static final Pattern ALBUM_PATTERN = Pattern.compile("<a href=\"/album/\\d+\" title=\"\">([^<]+)</a>");
    private static final Pattern ARTIST_PATTERN = Pattern.compile("<a href=\"/artist/\\d+\" title=\"\">([^<]+)</a>");
//    private static final Pattern WIKI_ARTIST_PATTERN = Pattern.compile("<span style=\"font-size:14px;line-height:1\\.8\">([^<]+)</span>");
//    private static final Pattern WIKI_LRC_PATTERN = Pattern.compile("<textarea name=\"lrc\" id=\"editLrcText\" style=\"width:560px;height:350px\" tabindex=\"4\">([^<]+)</textarea>");
//    private static final Pattern ALBUM_INFO_PATTERN = Pattern.compile("<td valign=\"top\">([^<]+)</td>");
    
    private XiamiAudio(String fileName) {
        try {
            this.mp3File = new MP3File(new File(fileName));
            this.tag = mp3File.getTagOrCreateAndSetDefault();

        } catch (TagException
                | ReadOnlyFileException
                | InvalidAudioFrameException
                | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static XiamiAudio build(String fileName) {
        if (isXiaminFile(fileName)) {
            return new XiamiAudio(fileName);
        }
        return null;
    }
    
    
    public static boolean isXiaminFile(String filename) {
        // Blue_Jeans_Album_Version_Remastered_虾小米打碟中_03_1770760764_2984092.mp3
        // 
        Pattern xiamiFilePattern = Pattern.compile("(\\p{InCJK_Unified_Ideographs}*[a-zA-Z]*_)+虾小米打碟中_[0-9][0-9]_\\d+_\\d+\\.mp3");
        Matcher matcher = xiamiFilePattern.matcher(filename);
        if (matcher.find()) {
            System.out.println(matcher.group());
            String[] tokens = matcher.group().split("_");
            mp3Id = tokens[tokens.length - 2];
            audioUrl = "http://www.xiami.com/song/" + mp3Id;
            xiamiWikiUrl = "http://www.xiami.com/wiki/addlrc/id/" + mp3Id;
            mp3NameFromFile = "";
            for (int i = 0; i < tokens.length - 4; i++) {
                mp3NameFromFile += tokens[i] + " ";
            }
            return true;
        } else {
            System.err.println("file " + filename + " is not xiami file");
            return false;
        }
    }
    
    public String printTagFields() {
        StringBuilder sb = new StringBuilder();
        if (tag == null) {
            return "";
        }
        Iterator<TagField> iter = tag.getFields();
        while (iter.hasNext()) {
            TagField tagField = iter.next();
            sb.append(tagField.toString()).append("\n");
        }
        return sb.toString();
    }
    
    public void extactFieldsInfo() throws Exception {
        String webContent = new WebClient().getContent(audioUrl);
        if (!StringUtil.isBlank(webContent)) {
            parseInfoFromAudioUrl(webContent);
            return;
        }
        System.err.println(audioUrl + "\tis null");
    }
    
    private void parseInfoFromAudioUrl(String webContent) {
        String titlePiece = webContent.substring(webContent.indexOf("<div id=\"title\">"));
        Matcher titleMatcher = TITLE_PATTERN.matcher(titlePiece);
        if (titleMatcher.find()) {
            title = titleMatcher.group(1);
        }
        Matcher albumMatcher = ALBUM_PATTERN.matcher(webContent);
        if (albumMatcher.find()) {
            album = albumMatcher.group(1);
        }
        
        Matcher artistMatcher = ARTIST_PATTERN.matcher(webContent);
        if (artistMatcher.find()) {
            artist = artistMatcher.group(1);
        }
    }
    
/*    private void parserInfoFromWikiUrl(String webContent) {
        Pattern titlePattern = Pattern.compile("<a href=\"/song/" + mp3Id + "\">([^<]+)</a>");
        Matcher titleMatcher = titlePattern.matcher(webContent);
        if (titleMatcher.find()) {
            System.out.println(titleMatcher.group(1));
        }
        
        Matcher aritistMatcher = WIKI_ARTIST_PATTERN.matcher(webContent);
        if (aritistMatcher.find()) {
            System.out.println(aritistMatcher.group(1));
        }
        
        Matcher lrcMatcher = WIKI_LRC_PATTERN.matcher(webContent);
        if (lrcMatcher.find()) {
            System.out.println(lrcMatcher.group(1));
        }
        
        Matcher albumInfoMatcher = ALBUM_INFO_PATTERN.matcher(webContent);
        while (albumInfoMatcher.find()) {
            System.out.println(albumInfoMatcher.group(1));
        }
    }*/
    
    public void updateAudioInfo() {
        try {
            if (album != null) {
                tag.setField(FieldKey.ALBUM, album);
            }
            if (artist != null) {
                tag.setField(FieldKey.ARTIST, artist);
            }
            if (title == null) {
                title = mp3NameFromFile;
            }
            tag.setField(FieldKey.TITLE, title);
            mp3File.commit();
        } catch (KeyNotFoundException | FieldDataInvalidException e) {
            e.printStackTrace();
        } catch (CannotWriteException e) {
            e.printStackTrace();
        }
    }

    public String getMp3NameFromFile() {
        return mp3NameFromFile;
    }


    public String getAudioUrl() {
        return audioUrl;
    }

    
}
