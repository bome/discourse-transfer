package net.jthink.discoursetransfer.tasks;

import com.google.gson.Gson;
import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.apimodel.Category;
import net.jthink.discoursetransfer.apimodel.CreateCategoryResult;
import net.jthink.discoursetransfer.csv.CategoryCsv;
import net.jthink.discoursetransfer.csv.UserCsv;
import net.jthink.discoursetransfer.helpers.HttpGetHelper;
import net.jthink.discoursetransfer.helpers.HttpPostHelper;
import net.jthink.discoursetransfer.apimodel.CreateCategorySend;
import net.jthink.discoursetransfer.apimodel.GetCategoriesResult;

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

public class CreateCategory
{
    public static final String CREATE_CATEGORY_ENDPOINT = "categories/";

    public static final String GET_CATEGORIES_ENDPOINT = "categories.json";

    private static Category[] cachedCategories = null;
    
    /**
     * Getting a single category is not supported (yet) by the API,
     * so we get all categories (cache them) and then manually match by name.
     * @return the category ID or 0 if not found
     */
	public int getCategoryIdByName(String name) throws Exception
	{
		if (cachedCategories == null)
		{
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = HttpGetHelper.createHttpGet(GET_CATEGORIES_ENDPOINT);
	
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
					GetCategoriesResult result = gson.fromJson(resultString, GetCategoriesResult.class);
					if (result != null && result.getCategoryList() != null && result.getCategoryList().getCategories() != null)
					{
						cachedCategories = result.getCategoryList().getCategories();
						DiscourseTransfer.log.config("Received " + cachedCategories.length + " categories.");
						break;
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
		}
		
		if (cachedCategories != null)
		{
			for (Category cat : cachedCategories)
			{
				//System.out.println("Category " + cat.getId() + ": " + cat.getName());
				if (name.equalsIgnoreCase(cat.getName()))
				{
					DiscourseTransfer.log.config("Existing category '" + name + "' has ID=" + cat.getId());
					return cat.getId();
				}
			}
		}
		
		return 0;
	}

	
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
        int originalForumId     = Integer.parseInt(fieldNameToFieldMap.get(CategoryCsv.ID.getFieldName()));

        if(!fieldNameToFieldMap.containsKey(CategoryCsv.NAME.getFieldName()))
        {
            System.out.println("Missing mandatory field for creating category " + originalForumId + ":"+UserCsv.NAME.getFieldName());
            return -1;
        }

        int discourseCategoryId = getCategoryIdByName(fieldNameToFieldMap.get(CategoryCsv.NAME.getFieldName()));

        if (discourseCategoryId <= 0)
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
	        
	        discourseCategoryId = create(category);
        }
        
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