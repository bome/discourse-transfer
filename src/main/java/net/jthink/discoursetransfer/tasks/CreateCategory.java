package net.jthink.discoursetransfer.tasks;

import com.google.gson.Gson;
import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.apimodel.CreateCategoryResult;
import net.jthink.discoursetransfer.csv.CategoryCsv;
import net.jthink.discoursetransfer.helpers.HttpPostHelper;
import net.jthink.discoursetransfer.apimodel.CreateCategorySend;
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

public class CreateCategory
{
    public static final String CREATE_CATEGORY_ENDPOINT = "categories/";

    /**
     *
     * @param category
     * @return id if successful, if failed return 0
     * @throws Exception
     */
    public int create(CreateCategorySend category) throws Exception
    {
        Gson    gson    = new Gson();
        String  json    = gson.toJson(category);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = HttpPostHelper.createHttpPost(CREATE_CATEGORY_ENDPOINT);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        DiscourseTransfer.log.config("REQUEST:"+httpPost.toString());
        HttpResponse response = httpClient.execute(httpPost);
        int code = response.getStatusLine().getStatusCode();
        DiscourseTransfer.log.config("RESPONSE_CODE:"+code);

        HttpEntity resEntity = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
        while (reader.ready())
        {
            String resultString = reader.readLine();
            DiscourseTransfer.log.config("REQUEST-JSON:"+json+":RESPONSE_CODE:"+code+":RESULTS:"+resultString);
            if(code==200)
            {
                CreateCategoryResult result = gson.fromJson(resultString, CreateCategoryResult.class);
                DiscourseTransfer.log.config("CATEGORYID:"+result.getCategory().getId());
                return result.getCategory().getId();
            }
            else
            {
                DiscourseTransfer.log.severe("REQUEST-JSON:"+json+":RESPONSE_CODE:"+code+":RESULTS:"+resultString);
            }
        }
        return 0;
    }

    public int loadandCreate(HashMap<String, String> fieldNameToFieldMap, Path mapFile) throws Exception
    {
        CreateCategorySend category = new CreateCategorySend();

        if(fieldNameToFieldMap.containsKey(CategoryCsv.NAME.getFieldName()))
        {
            category.setName(fieldNameToFieldMap.get(CategoryCsv.NAME.getFieldName()));
        }

        if(fieldNameToFieldMap.containsKey(CategoryCsv.COLOR.getFieldName()))
        {
            category.setColor(fieldNameToFieldMap.get(CategoryCsv.COLOR.getFieldName()));
        }

        if(fieldNameToFieldMap.containsKey(CategoryCsv.TEXT_COLOR.getFieldName()))
        {
            category.setText_color(fieldNameToFieldMap.get(CategoryCsv.TEXT_COLOR.getFieldName()));
        }

        int discourseCategoryId = create(category);
        int originalForumId     = Integer.parseInt(fieldNameToFieldMap.get(CategoryCsv.ID.getFieldName()));
        if(discourseCategoryId> 0 && originalForumId> 0)
        {
            DiscourseTransfer.categoryMap.put(originalForumId, discourseCategoryId);

            if(mapFile!=null)
            {
                Files.write(
                        mapFile,
                        new String(originalForumId + ";" +  discourseCategoryId +"\n")
                                .getBytes(Charset.forName("UTF8")),
                        StandardOpenOption.APPEND);
            }
        }
        return discourseCategoryId;
    }
}