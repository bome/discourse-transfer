package net.jthink.discoursetransfer.tasks;

import com.google.gson.Gson;
import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.apimodel.CreatePostResult;
import net.jthink.discoursetransfer.apimodel.CreateTopicSend;
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

public class CreateTopic
{
    public static final String CREATE_POST_ENDPOINT = "posts/";

    /**
     * TODO need to create for the correct user
     *
     * @param topic
     * @return id if successful, if failed return 0
     * @throws Exception
     */
    public int create(CreateTopicSend topic,String userName) throws Exception
    {
        Gson gson = new Gson();
        String json = gson.toJson(topic);
        DiscourseTransfer.log.severe(userName + ":REQUEST-JSON:" + json);

        while(true)
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
	                DiscourseTransfer.log.config("TOPICID:" + result.getTopic_id());
	                return result.getTopic_id();
	            }
	        }
	        else
	        {
	            System.out.println("*****Unable to create topic:"+userName+":"+code);
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


    public int loadandCreate(HashMap<String, String> fieldNameToFieldMap, Path mapFile, Path redirectMapFile) throws Exception
    {
        if(!fieldNameToFieldMap.containsKey(PostCsv.TOPIC_TITLE.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Topics:"+PostCsv.TOPIC_TITLE.getFieldName());
            return -1;
        }

        if(!fieldNameToFieldMap.containsKey(PostCsv.POST_TEXT.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Topics:"+PostCsv.POST_TEXT.getFieldName());
            return -1;
        }

        if(!fieldNameToFieldMap.containsKey(PostCsv.FORUMID.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Topics:"+PostCsv.FORUMID.getFieldName());
            return -1;
        }

        //We dont send this to discourse but we need so we can store in mapping
        if(!fieldNameToFieldMap.containsKey(PostCsv.TOPICID.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Topics:"+PostCsv.TOPICID.getFieldName());
            return -1;
        }

        if(!fieldNameToFieldMap.containsKey(PostCsv.USERNAME.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Topics:"+PostCsv.USERNAME.getFieldName());
            return -1;
        }

        CreateTopicSend topicSend = new CreateTopicSend();
        if(fieldNameToFieldMap.containsKey(PostCsv.TOPIC_TITLE.getFieldName()))
        {
            topicSend.setTitle(TextReplacement.replaceText(fieldNameToFieldMap.get(PostCsv.TOPIC_TITLE.getFieldName())));
        }

        if(fieldNameToFieldMap.containsKey(PostCsv.POST_TEXT.getFieldName()))
        {
            String bodyText = fieldNameToFieldMap.get(PostCsv.POST_TEXT.getFieldName());
            topicSend.setRaw(TextReplacement.replaceText(bodyText));
        }

        //Looking in mapping to see what the original forumid was mapped to by Discourse when categries created
        if(fieldNameToFieldMap.containsKey(PostCsv.FORUMID.getFieldName()))
        {
            String categoryId = fieldNameToFieldMap.get(PostCsv.FORUMID.getFieldName());
            topicSend.setCategory(DiscourseTransfer.categoryMap.get(Integer.valueOf(categoryId)));
        }

        if(fieldNameToFieldMap.containsKey(PostCsv.POST_TIME.getFieldName()))
        {
            topicSend.setCreated_at(fieldNameToFieldMap.get(PostCsv.POST_TIME.getFieldName()));
        }

        topicSend.setSkip_validations(true);

        //Create the Topic as username
        int discourseTopicId    = create(topicSend, fieldNameToFieldMap.get(PostCsv.USERNAME.getFieldName()));

        //Map old/new topicId
        int originalTopicId     = Integer.parseInt(fieldNameToFieldMap.get(PostCsv.TOPICID.getFieldName()));
        if(discourseTopicId> 0 && originalTopicId> 0)
        {
            DiscourseTransfer.topicMap.put(originalTopicId, discourseTopicId);

            if(mapFile!=null)
            {
                Files.write(
                        mapFile,
                        new String(originalTopicId + ";" +  discourseTopicId + "\n")
                                .getBytes(Charset.forName("UTF8")),
                        StandardOpenOption.APPEND);
            }
            
            if (redirectMapFile != null)
            {
                if (fieldNameToFieldMap.containsKey(PostCsv.URL.getFieldName()))
                {
                	String originalUrl = fieldNameToFieldMap.get(PostCsv.URL.getFieldName());
                	if (!originalUrl.isEmpty())
                	{
						String discourseURL = HttpPostHelper.getWebsite() + "t/" + discourseTopicId;
						Files.write(redirectMapFile,
								new String(originalUrl + ";" + discourseURL + "\n").getBytes(Charset.forName("UTF8")),
								StandardOpenOption.APPEND);
                	}
                }

            	            	
            }
        }
        return discourseTopicId;
    }
}