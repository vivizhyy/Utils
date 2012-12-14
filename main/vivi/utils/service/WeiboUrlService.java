package vivi.utils.service;

import java.util.ArrayList;
import java.util.List;

import utils.Constants;
import vivi.utils.file.FileUtil;
import weibo4j.Weibo;

public class WeiboUrlService {

    public WeiboUrlService() {
        init();
    }
    
    private void init() {
        Weibo weibo = new Weibo();
        weibo.setToken(Constants.ACCESS_TOKEN);
    }
    
    private boolean contains0(String line) {
        String[] token = line.split(", ");
        if (token.length != 4) {
            return true;
        } else {
            for (int i = 1; i < token.length; i++) {
                if (token[i].equals("0")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void printNonZeroUrlInfos() {
        final String urlInfoPath = "short-urls-info-1355108585335";
        List<String> lines = FileUtil.getLines(urlInfoPath);
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (!contains0(line)) {
                result.add(line);
            }
        }
        
        FileUtil.writeContents(result, 
                FileUtil.getFileNameWithTimestampLong("non0-short-urls-info"));
    }
}
