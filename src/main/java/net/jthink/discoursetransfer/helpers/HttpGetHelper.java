package net.jthink.discoursetransfer.helpers;

import org.apache.http.client.methods.HttpGet;

import static net.jthink.discoursetransfer.helpers.DiscourseParams.*;

public class HttpGetHelper
{
    private static String apiUsername;
    private static String apiKey;
    private static String website;

    
    public static HttpGet createHttpGet(String endPoint)
    {
        HttpGet httpGet = new HttpGet(website + endPoint);
        httpGet.addHeader(API_KEY_KEY, apiKey);
        httpGet.addHeader(API_USERNAME_KEY, apiUsername);
        //httpGet.addHeader(CONTENT_TYPE_KEY, "application/json");
        return httpGet;
    }

    public static void setApiUsername(String username)
    {
        apiUsername =username;
    }

    public static void setApiKey(String key)
    {
        apiKey = key;
    }

    public static void setWebsite(String websiteUrl)
    {
        if(!websiteUrl.endsWith("/"))
        {
            websiteUrl = websiteUrl + "/";
        }
        website = websiteUrl;
    }
}
