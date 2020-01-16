package net.jthink.discoursetransfer.helpers;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import static net.jthink.discoursetransfer.helpers.DiscourseParams.*;

public class HttpPutHelper
{
    private static String apiUsername;
    private static String apiKey;
    private static String website;



    public static HttpPut createHttpPut(String endPoint)
    {
        HttpPut httpPut = new HttpPut(website + endPoint);
        httpPut.addHeader(API_KEY_KEY, apiKey);
        httpPut.addHeader(API_USERNAME_KEY, apiUsername);
        httpPut.addHeader(CONTENT_TYPE_KEY, "application/json");
        return httpPut;
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
