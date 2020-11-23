package net.jthink.discoursetransfer.tasks;

import com.google.gson.Gson;
import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.csv.UserCsv;
import net.jthink.discoursetransfer.helpers.HttpGetHelper;
import net.jthink.discoursetransfer.helpers.HttpPostHelper;
import net.jthink.discoursetransfer.apimodel.CreateUserResult;
import net.jthink.discoursetransfer.apimodel.CreateUserSend;
import net.jthink.discoursetransfer.apimodel.User;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
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
import java.util.Random;

public class CreateUser
{
    public static final String CREATE_USERS_ENDPOINT = "users/";

    public static final String GET_USERBYEMAIL_ENDPOINT = "admin/users/list/all.json?email=";

    private boolean userExistedBefore = false;
    

    public User getUserByEmail(String email) throws Exception
	{
    	while (true)
    	{
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = HttpGetHelper.createHttpGet(GET_USERBYEMAIL_ENDPOINT + email);
	
			// DiscourseTransfer.log.severe("REQUEST:"+httpGet.toString());
			HttpResponse response = httpClient.execute(httpGet);
	
			int code = response.getStatusLine().getStatusCode();
			HttpEntity resEntity = response.getEntity();
			if (code == 429)
			{
				// too many requests
				System.out.println("Status 429: too many requests... waiting for 15 seconds.");
				DiscourseTransfer.log.severe("STATUS: 429 Too Many Requests");
				Thread.sleep(15000);
				continue;
			}
	
			if (code == 200)
			{
				Gson gson = new Gson();
				BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
				while (reader.ready())
				{
					String resultString = reader.readLine();
					User[] users = gson.fromJson(resultString, User[].class);
					if (users.length > 0)
					{
						DiscourseTransfer.log.config("UserID:" + users[0].getId());
						return users[0];
					}
				}
			}
			break;
    	}
		return null;
	}
    
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

    	while (true)
    	{	
	        CloseableHttpClient httpClient = HttpClients.createDefault();
	        HttpPost httpPost = HttpPostHelper.createHttpPost(CREATE_USERS_ENDPOINT);
	        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));
	
	        DiscourseTransfer.log.severe("REQUEST:"+httpPost.toString());
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
	
	        HttpEntity resEntity  = response.getEntity();
	       
	        if(code==200)
	        {
	            BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
	            while (reader.ready())
	            {
	                String resultString = reader.readLine();
	                CreateUserResult result = gson.fromJson(resultString, CreateUserResult.class);
	                if (!result.isSuccess())
	                {
	                	DiscourseTransfer.log.severe("Create User message failed: " + result.getMessage());
	                	return 0;
	                }
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
	        break;
    	}
        return 0;
    }

    
    private static Random random = new Random();
    
    private static String getRandomPassword()
    {
    	return Long.toHexString(random.nextLong())
    			+ Long.toHexString(random.nextLong())
    			+ Long.toHexString(random.nextLong());
    }

    public int loadandCreate(HashMap<String, String> fieldNameToFieldMap, Path mapFile) throws Exception
    {
    	userExistedBefore = false;
    	
        int originalUserId = Integer.parseInt(fieldNameToFieldMap.get(UserCsv.USERID.getFieldName()));
    	int discourseUserId = 0;

        if(!fieldNameToFieldMap.containsKey(UserCsv.EMAIL.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating Users:"+UserCsv.EMAIL.getFieldName());
            return -1;
        }

        User retrievedUser = getUserByEmail(fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()));
        if (retrievedUser != null)
        {
        	discourseUserId = retrievedUser.getId();
        	userExistedBefore = true;
        }
        
    	if (discourseUserId == 0)
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

	        String password = "";
	        if (fieldNameToFieldMap.containsKey(UserCsv.PASSWORD.getFieldName()))
	        {
	            password = fieldNameToFieldMap.get(UserCsv.PASSWORD.getFieldName());
		        if (password.isEmpty())
		        {
		        	System.out.println("WARNING: user #" + originalUserId + " (" + user.getUsername() + ") has an empty password. Will use a randomized password.");
		        }
	        }
	        if (password.isEmpty())
	        {
	        	// passwords must not be blank
	            password = getRandomPassword();
	        }	        
	        user.setPassword(password);
	
	        user.setActive(true);
	        user.setApproved(true);
	
	        discourseUserId = create(user);
    	}
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
    
    /**
     * For the last call to loadandCreate(), return if the user existed already or was newly created.
     * @return true if user existed before, false otherwise.
     */
    public boolean didUserExist()
    {
    	return userExistedBefore;
    }
    
}