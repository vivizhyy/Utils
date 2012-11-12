package vivi.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;


public class WebClient {

    /**
     * 代理服务器的地址
     */
    private static String proxyHost;
    /**
     * 代理服务器的端口
     */
    private static String proxyPort;
    /**
     * 代理服务器用户名
     */
    private static String proxyUser;
    /**
     * 代理服务器密码
     */
    private static String proxyPassword;

    /**
     * 网页抓取方法
     * 
     * @param urlString
     *            要抓取的url地址
     * @param charset
     *            网页编码方式
     * @param timeout
     *            超时时间
     * @return 抓取的网页内容
     * @throws IOException
     *             抓取异常
     */
    public static String getWebContent(String urlString, final String charset,
            int timeout) throws IOException {
        if (urlString == null || urlString.length() == 0) {
            return null;
        }
        urlString = (urlString.startsWith("http://") || urlString
                .startsWith("https://")) ? urlString : ("http://" + urlString)
                .intern();
        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        getProxy();
        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)");// 增加报头，模拟浏览器，防止屏蔽
        conn.setRequestProperty("Accept", "text/html");// 只接受text/html类型，当然也可以接受图片,pdf,*/*任意，就是tomcat/conf/web里面定义那些

        conn.setConnectTimeout(timeout);
        try {
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        InputStream input = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input,
                charset));
        String line = null;
        StringBuffer sb = new StringBuffer();
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\r\n");
        }
        if (reader != null) {
            reader.close();
        }
        if (conn != null) {
            conn.disconnect();
        }
        return sb.toString();

    }

    /**
     * 网页抓取方法
     * 
     * @param urlString
     *            要抓取的url地址
     * @return 抓取的网页内容
     * @throws IOException
     *             抓取异常
     */
    public static String GetWebContent(String urlString) throws IOException {
        return getWebContent(urlString, "iso-8859-1", 5000);
    }

    /**
     * 网页抓取方法
     * 
     * @param urlString
     *            要抓取的url地址
     * @param pageCharset
     *            目标网页编码方式
     * @return 抓取的网页内容
     * @throws IOException
     *             抓取异常
     */
    public static String getWebContent(String urlString, String pageCharset)
            throws IOException {
        String strHTML = getWebContent(urlString, "iso-8859-1", 5000);
        String StrEncode = new String(strHTML.getBytes("iso-8859-1"),
                pageCharset);
        return StrEncode;
    }

    /**
     * 设定代理服务器
     * 
     * @param proxyHost
     * @param proxyPort
     */
    public static void setProxy(String proxyHost, String proxyPort) {
        setProxy(proxyHost, proxyPort, null, null);
    }

    /**
     * 设定代理服务器
     * 
     * @param proxyHost
     *            代理服务器的地址
     * @param proxyPort
     *            代理服务器的端口
     * @param proxyUser
     *            代理服务器用户名
     * @param proxyPassword
     *            代理服务器密码
     */
    public static void setProxy(String sproxyHost, String sproxyPort,
            String sproxyUser, String sproxyPassword) {
        proxyHost = sproxyHost;
        proxyPort = sproxyPort;
        if (sproxyPassword != null && sproxyPassword.length() > 0) {
            proxyUser = sproxyUser;
            proxyPassword = sproxyPassword;
        }
    }

    /**
     * 取得代理设定
     * 
     * @return
     */
    private static Properties getProxy() {
        Properties propRet = null;
        if (proxyHost != null && proxyHost.length() > 0) {
            propRet = System.getProperties();
            // 设置http访问要使用的代理服务器的地址
            propRet.setProperty("http.proxyHost", proxyHost);
            // 设置http访问要使用的代理服务器的端口
            propRet.setProperty("http.proxyPort", proxyPort);
            if (proxyUser != null && proxyUser.length() > 0) {
                // 用户名密码
                propRet.setProperty("http.proxyUser", proxyUser);
                propRet.setProperty("http.proxyPassword", proxyPassword);
            }
        }

        return propRet;
    }

    /**
     * 类测试函数
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // SetProxy("10.10.10.10", "8080");//代理服务器设定
        String s = getWebContent("www.xiami.com/song/1770760764", "utf-8");
        System.out.println(s);
    }
}
