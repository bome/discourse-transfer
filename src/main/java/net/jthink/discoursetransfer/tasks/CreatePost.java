package net.jthink.discoursetransfer.tasks;


import com.google.gson.Gson;
import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.apimodel.CreatePostResult;
import net.jthink.discoursetransfer.apimodel.CreatePostSend;
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

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = HttpPostHelper.createHttpPost(CREATE_POST_ENDPOINT, userName);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
        HttpResponse response = httpClient.execute(httpPost);
        int code = response.getStatusLine().getStatusCode();

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

        String postId          = fieldNameToFieldMap.get(PostCsv.POSTID.getFieldName());

        //Get the discourseTopicId for this topic (as master topic should already have been created)
        String topicId          = fieldNameToFieldMap.get(PostCsv.TOPICID.getFieldName());

        if(!DiscourseTransfer.topicMap.containsKey(Integer.valueOf(topicId)))
        {
            DiscourseTransfer.log.severe("Unable to find Topic In Db:"+topicId);
            return 0;
        }

        int    discourseTopicId = DiscourseTransfer.topicMap.get(Integer.valueOf(topicId));

        if(discourseTopicId<=0)
        {
            System.out.println("cannot find discourse topic id for topic id:"+topicId);
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

        //Create Post
        int discoursePostId = create(post,  fieldNameToFieldMap.get(PostCsv.USERNAME.getFieldName()));

        if(Integer.parseInt(postId) > 0 && discoursePostId >0)
        {
            if (mapFile != null)
            {
                Files.write(
                        mapFile,
                        new String(postId + ";" + discoursePostId + "\n")
                                .getBytes(Charset.forName("UTF8")),
                        StandardOpenOption.APPEND);
            }
        }
        return discoursePostId;
    }
}