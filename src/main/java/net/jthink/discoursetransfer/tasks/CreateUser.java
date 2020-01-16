package net.jthink.discoursetransfer.tasks;

import com.google.gson.Gson;
import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.csv.UserCsv;
import net.jthink.discoursetransfer.helpers.HttpPostHelper;
import net.jthink.discoursetransfer.apimodel.CreateUserResult;
import net.jthink.discoursetransfer.apimodel.CreateUserSend;
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

public class CreateUser
{
    public static final String CREATE_USERS_ENDPOINT = "users/";

    /**
     *
     * @param user
     * @return user_id if successful, if failed return 0
     * @throws Exception
     */
    public int create(CreateUserSend user) throws Exception
    {
        Gson gson       = new Gson();
        String json = gson.toJson(user);
        DiscourseTransfer.log.severe("REQUEST-JSON:"+json);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = HttpPostHelper.createHttpPost(CREATE_USERS_ENDPOINT);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        DiscourseTransfer.log.severe("REQUEST:"+httpPost.toString());
        HttpResponse response = httpClient.execute(httpPost);

        int code = response.getStatusLine().getStatusCode();
        HttpEntity resEntity  = response.getEntity();

        if(code==200)
        {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
            while (reader.ready())
            {
                String resultString = reader.readLine();
                CreateUserResult result = gson.fromJson(resultString, CreateUserResult.class);
                DiscourseTransfer.log.config("UserID:" + result.getUser_id());
                return result.getUser_id();
            }
        }
        else
        {
            DiscourseTransfer.log.severe("UserCreatedFailed:" + code);
            DiscourseTransfer.log.severe("REQUEST:"+httpPost.toString());

            BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
            while (reader.ready())
            {
                String resultString = reader.readLine();
                DiscourseTransfer.log.severe(resultString);
            }
        }
        return 0;
    }

    public int loadandCreate(HashMap<String, String> fieldNameToFieldMap, Path mapFile) throws Exception
    {
        if(!fieldNameToFieldMap.containsKey(UserCsv.USERID.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Users:"+UserCsv.USERID.getFieldName());
            return -1;
        }

        if(!fieldNameToFieldMap.containsKey(UserCsv.USERNAME.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Users:"+UserCsv.USERNAME.getFieldName());
            return -1;
        }

        if(!fieldNameToFieldMap.containsKey(UserCsv.EMAIL.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Users:"+UserCsv.EMAIL.getFieldName());
            return -1;
        }

        CreateUserSend user = new CreateUserSend();

        if (fieldNameToFieldMap.containsKey(UserCsv.USERNAME.getFieldName()))
        {
            user.setUsername(fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()));
        }

        if (fieldNameToFieldMap.containsKey(UserCsv.EMAIL.getFieldName()))
        {
            user.setEmail(fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()));
        }

        if (fieldNameToFieldMap.containsKey(UserCsv.NAME.getFieldName()))
        {
            user.setName(fieldNameToFieldMap.get(UserCsv.NAME.getFieldName()));
        }

        if (fieldNameToFieldMap.containsKey(UserCsv.PASSWORD.getFieldName()))
        {
            user.setPassword(fieldNameToFieldMap.get(UserCsv.PASSWORD.getFieldName()));
        }

        user.setActive(true);
        user.setApproved(true);

        int discourseUserId = create(user);
        Integer originalUserId = Integer.parseInt(fieldNameToFieldMap.get(UserCsv.USERID.getFieldName()));
        if (discourseUserId > 0 && originalUserId > 0)
        {
            DiscourseTransfer.userMap.put(originalUserId, discourseUserId);
            if(mapFile!=null)
            {
                Files.write(
                        mapFile,
                        new String(originalUserId + ";" +  discourseUserId + "\n")
                                .getBytes(Charset.forName("UTF8")),
                        StandardOpenOption.APPEND);
            }
        }
        return discourseUserId;
    }
}