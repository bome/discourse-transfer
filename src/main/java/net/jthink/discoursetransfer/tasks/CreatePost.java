package net.jthink.discoursetransfer.tasks;


import com.google.gson.Gson;
import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.apimodel.CreatePostResult;
import net.jthink.discoursetransfer.apimodel.CreatePostSend;
import net.jthink.discoursetransfer.apimodel.Post;
import net.jthink.discoursetransfer.csv.PostCsv;
import net.jthink.discoursetransfer.helpers.HttpPostHelper;
import net.jthink.discoursetransfer.helpers.TextReplacement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

public class CreatePost
{
    public static final String CREATE_POST_ENDPOINT = "posts/";

    /**
     * TODO need to create for the correct user
     *
     * @param post
     * @return id if successful, if failed return 0
     * @throws Exception
     */
    public int create(CreatePostSend post, String userName) throws Exception
    {
        Gson gson = new Gson();
        String json = gson.toJson(post);
        DiscourseTransfer.log.severe("REQUEST-JSON:" + json);

        while (true)
        {
	        CloseableHttpClient httpClient = HttpClients.createDefault();
	
	        HttpPost httpPost = HttpPostHelper.createHttpPost(CREATE_POST_ENDPOINT, userName);
	        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
	        HttpResponse response = httpClient.execute(httpPost);
	        int code = response.getStatusLine().getStatusCode();
	
			if (code == 429)
			{
				// too many requests
				System.out.println("Status 429: too many requests... waiting for 30 seconds.");
				DiscourseTransfer.log.severe("STATUS: 429 Too Many Requests");
				Thread.sleep(30000);
				continue;
			}
	
	        if (code==200)
	        {
	            HttpEntity resEntity = response.getEntity();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
	            while (reader.ready())
	            {
	                String resultString = reader.readLine();
	                CreatePostResult result = gson.fromJson(resultString, CreatePostResult.class);
	                DiscourseTransfer.log.config("TOPICID:" + result.getId());
	                return result.getId();
	            }
	        }
	        else
	        {
	            DiscourseTransfer.log.severe("RESPONSE_CODE:" + code + ":REQUEST_JSON:" + json);
	            HttpEntity resEntity = response.getEntity();
	            BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
	            while (reader.ready())
	            {
	                String resultString = reader.readLine();
	                DiscourseTransfer.log.severe("RESULTS:" + resultString);
	            }
	            return 0;
	        }
	        break;
        }
        return 0;
    }

    public int loadandCreate(HashMap<String, String> fieldNameToFieldMap, Path mapFile) throws Exception
    {
        CreatePostSend post = new CreatePostSend();

        if(
                (!fieldNameToFieldMap.containsKey(PostCsv.POST_TEXT.getFieldName())) ||
                (!fieldNameToFieldMap.containsKey(PostCsv.TOPICID.getFieldName())) ||
                (!fieldNameToFieldMap.containsKey(PostCsv.POSTID.getFieldName())) ||
                (!fieldNameToFieldMap.containsKey(PostCsv.USERNAME.getFieldName()))
           )
        {
            System.out.println("Missing mandatory field for creating Posts");
            return -1;
        }

        String postIdString          = fieldNameToFieldMap.get(PostCsv.POSTID.getFieldName());
        int originalPostId = Integer.valueOf(postIdString);

        //Get the discourseTopicId for this topic (as master topic should already have been created)
        String topicIdString          = fieldNameToFieldMap.get(PostCsv.TOPICID.getFieldName());

        if(!DiscourseTransfer.topicMap.containsKey(Integer.valueOf(topicIdString)))
        {
            DiscourseTransfer.log.severe("Unable to find Topic In Db:"+topicIdString);
            return 0;
        }
        int originalTopicId = Integer.valueOf(topicIdString);

        int discourseTopicId = DiscourseTransfer.topicMap.get(originalTopicId);

        if(discourseTopicId<=0)
        {
            System.out.println("cannot find discourse topic id for topic id:"+topicIdString);
            return -1;
        }

        post.setTopic_id(discourseTopicId);
        if(fieldNameToFieldMap.containsKey(PostCsv.POST_TEXT.getFieldName()))
        {
            post.setRaw(TextReplacement.replaceText(fieldNameToFieldMap.get(PostCsv.POST_TEXT.getFieldName())));
        }

        if(fieldNameToFieldMap.containsKey(PostCsv.POST_TIME.getFieldName()))
        {
            post.setCreated_at(fieldNameToFieldMap.get(PostCsv.POST_TIME.getFieldName()));
        }
        post.setSkip_validations(true);
        
        // is this a reply to the topic?
        if(fieldNameToFieldMap.containsKey(PostCsv.REPLY_TO_TOPIC_ID.getFieldName()))
        {
        	int reply_to_topic_id = Integer.valueOf(fieldNameToFieldMap.get(PostCsv.REPLY_TO_TOPIC_ID.getFieldName()));
        	if (reply_to_topic_id != 0)
        	{
        		// we can only comment on the topic post of this topic.
        		if (reply_to_topic_id == originalTopicId)
        		{
        			// post number 1 is the topic itself
        			post.setReplyToPostNumber(1);
        		}
        		else
        		{
        			System.out.println("  WARNING: post " + postIdString + ": cannot set reply_to_topic to topic " + reply_to_topic_id + ": is not the parent topic.");
        		}
        	}
        }

        // is this a reply to another post in the topic?
        if(fieldNameToFieldMap.containsKey(PostCsv.REPLY_TO_POST_ID.getFieldName()))
        {
        	int reply_to_post_id = Integer.valueOf(fieldNameToFieldMap.get(PostCsv.REPLY_TO_POST_ID.getFieldName()));
        	if (reply_to_post_id != 0)
        	{
        		// now get the discourse ID of the replied-to post
        		if (!DiscourseTransfer.postMap.containsKey(reply_to_post_id))
        		{
        			System.out.println("  WARNING: post " + postIdString + ": cannot set reply_to_post to post " + reply_to_post_id + ": this post does not exist (yet?) in the post id map."); 			
        		}
        		else
        		{
        			int discourseReplyToTopicId = DiscourseTransfer.postMap.get(reply_to_post_id);

	        		// we need to find the post number of that post
	        		Post replyToPost = new GetPost().getPostByID(discourseReplyToTopicId);
	        		if (replyToPost == null)
	        		{
	        			System.out.println("  WARNING: post " + postIdString + ": cannot set reply_to_post to post " + reply_to_post_id + ": error retrieving corresponding Discourse post " + discourseReplyToTopicId); 			
	        		}
	        		else
	        		{
	        			post.setReplyToPostNumber(replyToPost.getPostNumber());
	        			System.out.println("  post " + postIdString + ": setting reply_to_post_number to " + replyToPost.getPostNumber());  			
	        		}
	
        		}
        	}
        }

        //Create Post
        int discoursePostId = create(post,  fieldNameToFieldMap.get(PostCsv.USERNAME.getFieldName()));

        if(originalPostId > 0 && discoursePostId >0)
        {
            DiscourseTransfer.postMap.put(originalPostId, discoursePostId);

            if (mapFile != null)
            {
                Files.write(
                        mapFile,
                        new String(postIdString + ";" + discoursePostId + "\n")
                                .getBytes(Charset.forName("UTF8")),
                        StandardOpenOption.APPEND);
            }
        }
        return discoursePostId;
    }
}