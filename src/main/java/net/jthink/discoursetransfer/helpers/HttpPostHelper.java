package net.jthink.discoursetransfer.helpers;

import org.apache.http.client.methods.HttpPost;

import static net.jthink.discoursetransfer.helpers.DiscourseParams.API_KEY_KEY;
import static net.jthink.discoursetransfer.helpers.DiscourseParams.API_USERNAME_KEY;
import static net.jthink.discoursetransfer.helpers.DiscourseParams.CONTENT_TYPE_KEY;

public class HttpPostHelper
{
    private static String apiUsername;
    private static String apiKey;
    private static String website;


    public static HttpPost createHttpPost(String endPoint)
    {
        HttpPost httpPost = new HttpPost(website + endPoint);
        httpPost.addHeader(API_KEY_KEY, apiKey);
        httpPost.addHeader(API_USERNAME_KEY, apiUsername);
        httpPost.addHeader(CONTENT_TYPE_KEY, "application/json");
        return httpPost;
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
    
    public static String getWebsite()
    {
    	return website;
    }

    /**
     * For creating topics/post as user
     *
     * @param endPoint
     * @param username
     * @return
     */
    public static HttpPost createHttpPost(String endPoint, String username)
    {
        HttpPost httpPost = new HttpPost(website + endPoint);
        httpPost.addHeader(API_KEY_KEY, apiKey);
        httpPost.addHeader(API_USERNAME_KEY, username);
        httpPost.addHeader(CONTENT_TYPE_KEY, "application/json");
        return httpPost;
    }

}
