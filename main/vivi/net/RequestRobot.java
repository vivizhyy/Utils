package vivi.net;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.params.CookieSpecPNames;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import org.json.JSONObject;

import utils.Constants;
import vivi.utils.domain.weibo.SinaWeiboMediaType;
import vivi.utils.file.FileUtil;
import vivi.utils.parser.SinaWeiboParser;

public class RequestRobot {
    private static final Logger logger = Logger.getLogger(RequestRobot.class);
    
    protected String encoding;
    protected DefaultHttpClient client;
    protected CookieStore cookieStore;
    
    public RequestRobot() {
        this("UTF-8");
    }
    
    public RequestRobot(String encoding) {
        this.encoding = encoding;

        client = new DefaultHttpClient();
        client.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
        client.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, encoding);
        client.getParams().setBooleanParameter(CookieSpecPNames.SINGLE_COOKIE_HEADER, true);
        client.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
        client.getParams().setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);

        cookieStore = new BasicCookieStore();
        setCookieStore(cookieStore);
    }
    
    private void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        client.setCookieStore(cookieStore);
        
        addCookie();
    }

    private Cookie createCookie(String name, String value) {
        BasicClientCookie cookie = new BasicClientCookie(name, value);
        cookie.setDomain(".open.weibo.com");
        cookie.setPath("/");

        Calendar calendar = Calendar.getInstance();
        calendar.set(2099, 11, 31);
        cookie.setExpiryDate(calendar.getTime());

        cookie.setSecure(false);
        return cookie;
    }
    

    private void setHeaders(HttpRequest method) {
        method.setHeader("Accept",
                "text/html,application/xhtml+xml,application/xml;");
        method.setHeader("Accept-Language", "zh-cn");
        method.setHeader(
                        "User-Agent",
                        "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
        method.setHeader("Accept-Charset", encoding);
        method.setHeader("Keep-Alive", "300");
        method.setHeader("Connection", "Keep-Alive");
        method.setHeader("Cache-Control", "no-cache");
    }
    
    private void addCookie() {
        final String COOKIE_PATH = "resources/cookie";
        String[] cookieTokens = FileUtil.getContents(COOKIE_PATH).replace("\r\n", "").split("; ");
        for (String cookieToken : cookieTokens) {
            String[] tokens = cookieToken.split("=");
            if (tokens.length == 2) {
                cookieStore.addCookie(createCookie(tokens[0], tokens[1]));
            }
        }
    }
    
    private ContentResponse doPost(String referer, List<NameValuePair> params) throws Exception {
        final String actionUrl = "http://open.weibo.com/tools/aj_interface.php";
        final HttpPost method = new HttpPost(actionUrl);
        setHeaders(method);
        method.setHeader("Referer", referer);
        method.setHeader("Content-Type", "application/x-www-form-urlencoded");
        method.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        logPostRequest(method);

        final BasicHttpContext localContext = new BasicHttpContext();
        
        ResponseHandler<ContentResponse> handler = new ResponseHandler<ContentResponse>() {
            public ContentResponse handleResponse(HttpResponse httpResponse)
                    throws ClientProtocolException, IOException {
                ContentResponse response = new ContentResponse(httpResponse, localContext);
                logPostResponse(method, response);
                return response;
            }
        };

        ContentResponse response = client.execute(method, handler, localContext);
        
        Header location = response.getHttpResponse().getFirstHeader(HttpHeaders.LOCATION);
        if (location != null) {
            // XXX: what about "HTTP" or "https"? is it possible that the location contains
            // the host name but not the protocol?
            String url = location.getValue();
            if (!location.getValue().startsWith("http")) {
                url = "http://" + method.getURI().getAuthority() + url;
            }
            return doGet(url, "");
        }
        
        response.rejectNonOKResponse();
        return response;
    }
    
    protected ContentResponse doGet(String url, String referer)
            throws ClientProtocolException, IOException, Exception {
        final HttpGet method = new HttpGet(url);
        setHeaders(method);
        method.setHeader("Referer", referer);
        logGetRequest(method);

        final BasicHttpContext localContext = new BasicHttpContext();

        ResponseHandler<ContentResponse> handler = new ResponseHandler<ContentResponse>() {
            public ContentResponse handleResponse(HttpResponse httpResponse)
                    throws ClientProtocolException, IOException {
                ContentResponse response = new ContentResponse(httpResponse, localContext);
                logGetResponse(method, response);
                return response;
            }
        };

        ContentResponse response = client.execute(method, handler, localContext);
        response.rejectNonOKResponse();
        return response;
    }
    
    private void logPostRequest(HttpPost method) throws ParseException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("do post request: " + method.getURI().toString());
            logger.debug("header:\n" + getHeadersStr(method.getAllHeaders()));
            logger.debug("body:\n" + EntityUtils.toString(method.getEntity()));
            logger.debug("cookie:\n" + getCookieStr());
        }
    }
    
    private void logPostResponse(HttpPost method, ContentResponse response)
            throws ParseException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("do post response:" + method.getURI().toString());
            logger.debug("header:\n" + getHeadersStr(response.getHttpResponse().getAllHeaders()));
            logger.debug("body:\n" + response.getContent());
        }
    }
    
    private void logGetRequest(HttpGet method) {
        if (logger.isDebugEnabled()) {
            logger.debug("do get request: " + method.getURI().toString());
            logger.debug("header:\n" + getHeadersStr(method.getAllHeaders()));
            logger.debug("cookie:\n" + getCookieStr());
        }
    }

    private void logGetResponse(HttpGet method, Response response)
            throws ParseException, IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("do get response: " + method.getURI().toString());
            logger.debug("header: \n" + getHeadersStr(response.getHttpResponse().getAllHeaders()));
            logger.debug("body: \n" + response.getBody());
        }
    }
    
    private String getHeadersStr(Header[] headers) {
        StringBuilder builder = new StringBuilder();
        for (Header header : headers) {
            builder.append(header.getName()).append(": ").append(
                    header.getValue()).append("\n");
        }
        return builder.toString();
    }
    
    private String getCookieStr() {
        StringBuilder builder = new StringBuilder();
        for (Cookie cookie : cookieStore.getCookies()) {
            builder.append(cookie.getDomain()).append(":").append(
                    cookie.getName()).append("=").append(cookie.getValue())
                    .append(";").append(cookie.getPath()).append(";").append(
                            cookie.getExpiryDate()).append(";").append(
                            cookie.isSecure()).append(";\n");
        }
        return builder.toString();
    }
    
    protected abstract class Response {
        protected String url;
        protected HttpResponse httpResponse;
        
        public Response(HttpResponse httpResponse, HttpContext httpContext) throws ParseException, IOException {
            HttpHost target = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
            HttpUriRequest req = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
            url = target.toString() + req.getURI();

            this.httpResponse = httpResponse;
        }
        
        public String getUrl() {
            return url;
        }

        public HttpResponse getHttpResponse() {
            return httpResponse;
        }
        
        public abstract String getBody();
        
        public void rejectNonOKResponse() throws Exception {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                String mesg = "Non-OK HTTP response: " + httpResponse.getStatusLine()+ ". URL: " + url;
                if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                        statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
                    mesg += "redirect:" + httpResponse.getFirstHeader("Location");
                }
                
                logger.error(mesg);
                throw new Exception(mesg);
            }
        }
    }
    
    protected class ContentResponse extends Response {
        private String content;
        
        public ContentResponse(HttpResponse httpResponse, HttpContext httpContext) throws ParseException, IOException {
            super(httpResponse, httpContext);
            content = EntityUtils.toString(httpResponse.getEntity(), encoding);
        }
        
        public String getContent() {
            return content;
        }
        
        public JSONObject asJSONObject() throws JSONException {
            return new JSONObject(content).getJSONObject("retjson");
        }

        @Override
        public String getBody() {
            return content;
        }
        
    }
    
    private List<NameValuePair> getParams(String apiUrl, String requestData) {
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("_t", "0"));
        params.add(new BasicNameValuePair("api_url", apiUrl)); 
        params.add(new BasicNameValuePair("request_data", requestData + "&access_token=2.00RdP6jBhXEBTE9eab66cd94FVJo4E"));
        params.add(new BasicNameValuePair("request_type", "get"));
        return params;
    }
    
    private List<JSONObject> getStatusList(String apiUrl, String referer, String uid) {
        List<NameValuePair> params = getParams(apiUrl, "uid=" + uid + "&count=100&feature=1");
        try {
            JSONObject jsonStatus = doPost(referer, params).asJSONObject(); //asJSONArray();
            org.json.JSONArray jsonStatuses = null;
            if(!jsonStatus.isNull("statuses")){                             
                jsonStatuses = jsonStatus.getJSONArray("statuses");
            }
            if(!jsonStatus.isNull("reposts")){
                jsonStatuses = jsonStatus.getJSONArray("reposts");
            }
            int size = jsonStatuses.length();
            List<JSONObject> statuses = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                JSONObject status = jsonStatuses.getJSONObject(i);
                statuses.add(status);
            }
            return statuses;
        } catch (JSONException e) {
            logger.error("construct Status JSON exception: ", e);
        } catch (Exception e) {
            logger.error("construct Status exception: ", e);
        }
        
        return Collections.emptyList();
    }
    
    public void writeTimeline(SinaWeiboMediaType type) {
        List<String> uids = type.getUids();
        final String apiUrl = "https://api.weibo.com/2/statuses/user_timeline.json";
        String referer = "http://open.weibo.com/tools/console?uri=statuses/user_timeline&httpmethod=GET&";
        
        try {
            for (String uid : uids) {
                List<JSONObject> statuses = getStatusList(apiUrl, referer, uid);
                List<String> contents = new ArrayList<>();
                for (JSONObject status : statuses) {
                    contents.add(status.toString());
                }
                String dir = Constants.TIMELINE_DIR + 
                        type.toString();
                if (isWriteDir(dir)) {
                    FileUtil.writeContents(contents, 
                            dir + "/"+ FileUtil.getFileNameWithTimestampLong(uid));
                }
                
                Thread.sleep(TimeUnit.SECONDS.toMillis(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private boolean isWriteDir(String dir) {
        File fileDir = new File(dir);
        if (!fileDir.isDirectory()) {
            return fileDir.mkdir();
        }
        return true;
    }
    
    private void writeUserInfo(List<String> uids) throws InterruptedException {
        final String apiUrl = "https://api.weibo.com/2/users/show.json";
        
        List<String> userInfo = new ArrayList<>();
        for (String uid : uids) {
            String referer = "http://open.weibo.com/tools/console?uri=users/show&httpmethod=GET&key1=uid&value1=" + uid;
            String requestData = "uid=" + uid;
            try {
                JSONObject jsonUser = doPost(referer, getParams(apiUrl, requestData)).asJSONObject();
                userInfo.add(jsonUser.toString());
            } catch (Exception e) {
                logger.error(e);
            } 
            Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        }
        FileUtil.writeContents(userInfo, FileUtil.getFileNameWithTimestampLong("sina-weibo-user-info-json"));
    }
    
    private void writeShortUrlClicks(List<String> encodedShortUrls) {
        final String apiUrl = "https://api.weibo.com/2/short_url/clicks.json";
        String referer = "http://open.weibo.com/tools/console?uri=short_url/clicks&httpmethod=GET&{{{apiToolPara}}}";
        List<String> clicks = new ArrayList<>();
        try {
            for (String shortUrl : encodedShortUrls) {
                String requestData = "url_short=http%3A%2F%2Ft.cn%2F" + shortUrl;
                clicks.add(doPost(referer, getParams(apiUrl, requestData)).getContent());
                
                Thread.sleep(TimeUnit.SECONDS.toMillis(5));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileUtil.writeContents(clicks, FileUtil.getFileNameWithTimestampLong("clicks-json"));
    }

    
    public static void main(String[] args) throws InterruptedException {
        PropertyConfigurator.configure("log4j.properties");
        RequestRobot robot = new RequestRobot();
        robot.writeTimeline(SinaWeiboMediaType.TECH);
    }
}
