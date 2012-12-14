package vivi.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownServiceException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

import com.wumii.model.service.CharsetDetector;

import utils.chars.StringUtil;


public class WebClient {
    private static final CharsetDetector CHARSET_DETECTOR = new CharsetDetector();
    private static final Charset DEFAULT_CHARSET = Charset.forName("GBK");
    
    class Proxy {
        private String proxyHost;
        private String proxyPort;
        private String proxyUser;
        private String proxyPassword;
        
        
        public Proxy(String proxyHost, String proxyPort) {
            this(proxyHost, proxyPort, null, null);
        }
        
        public Proxy(String sproxyHost, String sproxyPort,
                String sproxyUser, String sproxyPassword) {
            this.proxyHost = sproxyHost;
            this.proxyPort = sproxyPort;
            if (sproxyPassword != null && sproxyPassword.length() > 0) {
                this.proxyUser = sproxyUser;
                this.proxyPassword = sproxyPassword;
            }
        }
        
        public Properties getProxy() {
            Properties propRet = null;
            if (proxyHost != null && proxyHost.length() > 0) {
                setProxy(propRet);
            }

            return propRet;
        }
        
        private void setProxy(Properties propRet) {
            setHostAndPort(propRet);
            if (proxyUser != null && proxyUser.length() > 0) {
                setUsernameAndPsw(propRet);
            }
        }
        
        private void setHostAndPort(Properties propRet) {
            propRet = System.getProperties();
            propRet.setProperty("http.proxyHost", proxyHost);
            propRet.setProperty("http.proxyPort", proxyPort);
        }
        
        private void setUsernameAndPsw(Properties propRet) {
            propRet.setProperty("http.proxyUser", proxyUser);
            propRet.setProperty("http.proxyPassword", proxyPassword);
        }
    }
    
    private URL getUrl(String urlString) throws MalformedURLException {
        if (StringUtil.isBlank(urlString)) {
            return null;
        }
        urlString = (urlString.startsWith("http://") || urlString
                .startsWith("https://")) ? urlString : ("http://" + urlString)
                .intern();
        return new URL(urlString);
    }
    
    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        setHttpURLConnectionProperties(conn);
        return conn;
    }
    
    private void setHttpURLConnectionProperties(HttpURLConnection conn) {
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)");
        conn.setRequestProperty("Accept", "text/html");

        conn.setConnectTimeout((int) TimeUnit.MINUTES.toMillis(1));
    }
    
    private HttpURLConnection getConnectionWithCheck(String urlString) throws IOException {
        HttpURLConnection conn = getHttpURLConnection(getUrl(urlString));
        if (!(conn.getResponseCode() > 199 && conn.getResponseCode() < 300)) {
            conn = null;
        }
        return conn;
    }
    
    private String getContentFromConnection(HttpURLConnection conn) throws IOException, UnknownServiceException {
        
        InputStream inputstream = conn.getInputStream();
        byte[] contentBytes = IOUtils.toByteArray(inputstream);
        String contentTypeString = conn.getContentType();
        Charset charset = detectCharset(contentTypeString, contentBytes);
        return new String(contentBytes, charset);
    }
    
    private Charset detectCharset(String contentType, byte[] contentBytes) throws IOException {
        Charset charset = CHARSET_DETECTOR.detect(contentType, contentBytes);
        if (charset != null) {
            return charset;
        }
        return DEFAULT_CHARSET;
    }
    
    public String getContent(String urlString) {
        String content = "";
        try {
            content = getContentFromConnection(getConnectionWithCheck(urlString));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return content;
    }
    
    public void convertCharset(String htmlString, String oldCharset, String newCharset) {
        try {
            htmlString = new String(htmlString.getBytes(oldCharset), newCharset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        // SetProxy("10.10.10.10", "8080");//代理服务器设定
        String s = (new WebClient()).getContent("www.xiami.com/song/1770760764");
        System.out.println(s);
    }
}
