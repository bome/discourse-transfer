package net.jthink.discoursetransfer.tasks;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.apimodel.Post;
import net.jthink.discoursetransfer.helpers.HttpGetHelper;

public class GetPost
{
    public static final String GET_POSTBYID_ENDPOINT = "posts/";

    /**
     * Get a single post, or null.
     */
	public Post getPostByID(int ID) throws Exception
	{
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet = HttpGetHelper.createHttpGet(GET_POSTBYID_ENDPOINT + ID + ".json");

		DiscourseTransfer.log.severe("REQUEST:"+httpGet.toString());
		HttpResponse response = httpClient.execute(httpGet);

		int code = response.getStatusLine().getStatusCode();
		HttpEntity resEntity = response.getEntity();

		Gson gson = new Gson();
		BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
		while (reader.ready())
		{
			String resultString = reader.readLine();
			if (code == 200)
			{
				Post post = gson.fromJson(resultString, Post.class);
				if (post != null && post.getId() == ID)
				{
					return post;
				}
				else
				{
	                DiscourseTransfer.log.severe("RESPONSE_CODE:"+code+":RESULTS:"+resultString);
				}
			}
			else
			{
                DiscourseTransfer.log.severe("RESPONSE_CODE:"+code+":RESULTS:"+resultString);
			}
		}
		return null;
	}

}