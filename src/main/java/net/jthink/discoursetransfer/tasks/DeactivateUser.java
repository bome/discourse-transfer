package net.jthink.discoursetransfer.tasks;

import net.jthink.discoursetransfer.DiscourseTransfer;
import net.jthink.discoursetransfer.helpers.HttpPutHelper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Activate these transferred user to avoid annoying extra step for customer
 *
 * put /admin/users/{id}/activate
 * */
public class DeactivateUser
{
    public static final String DEACTIVATE_USER_ENDPOINT = "/admin/users/%s/deactivate.json";

    /**
     *
     * @param userId
     * @return user_id if successful, if failed return 0
     * @throws Exception
     */
    public int deactivate(int userId) throws Exception
    {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = HttpPutHelper.createHttpPut(String.format(DEACTIVATE_USER_ENDPOINT,userId));

        HttpResponse response = httpClient.execute(httpPut);
        int code = response.getStatusLine().getStatusCode();
        DiscourseTransfer.log.config("REQUEST:"+String.format(DEACTIVATE_USER_ENDPOINT,userId));
        DiscourseTransfer.log.config("RESPONSE_CODE:"+code);
        HttpEntity resEntity  = response.getEntity();
        BufferedReader reader = new BufferedReader(new InputStreamReader(resEntity.getContent()));
        while(reader.ready())
        {
            String resultString = reader.readLine();
            DiscourseTransfer.log.config(resultString);

        }
        return 0;
    }
}