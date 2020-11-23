package net.jthink.discoursetransfer;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Strings;
import net.jthink.discoursetransfer.csv.UserCsv;
import net.jthink.discoursetransfer.csv.CategoryCsv;
import net.jthink.discoursetransfer.helpers.CmdLineOptions;
import net.jthink.discoursetransfer.helpers.HttpGetHelper;
import net.jthink.discoursetransfer.helpers.HttpPostHelper;
import net.jthink.discoursetransfer.helpers.HttpPutHelper;
import net.jthink.discoursetransfer.tasks.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.logging.Logger;

/**
 * Transfer data from another forum into discourse
 */
public class DiscourseTransfer
{
    public static Logger log = Logger.getLogger("net.jthink.discoursetransfer");

    //Maps from original userId to discourse userId once users have been imported
    public static Map<Integer, Integer> userMap = new LinkedHashMap<Integer, Integer>();

    //Maps from original forumId to discourse categoryId once categories have been imported
    public static Map<Integer, Integer> categoryMap = new LinkedHashMap<Integer, Integer>();

    //Maps from original topicId to discourse topicId have been imported
    public static Map<Integer, Integer> topicMap    = new LinkedHashMap<Integer, Integer>();

    //Maps from original postId to discourse postId have been imported
    public static Map<Integer, Integer> postMap    = new LinkedHashMap<Integer, Integer>();

    public static void main(final String[] args) throws Exception
    {
        CmdLineOptions options = verifyOptions(args);
        if(options==null)
        {
            System.exit(1);
        }

        HttpPostHelper.setApiKey(options.apiKey);
        HttpPostHelper.setApiUsername(options.apiUsername);
        HttpPostHelper.setWebsite(options.website);

        HttpPutHelper.setApiKey(options.apiKey);
        HttpPutHelper.setApiUsername(options.apiUsername);
        HttpPutHelper.setWebsite(options.website);

        HttpGetHelper.setApiKey(options.apiKey);
        HttpGetHelper.setApiUsername(options.apiUsername);
        HttpGetHelper.setWebsite(options.website);

        //Add Users
        if(options.usersCsvFile!=null)
        {
        	Path importPassFile = null;
        	if (options.importpassFile != null)
        	{
        		importPassFile = Paths.get(options.importpassFile);
        	}
            addUsers(Paths.get(options.usersCsvFile), options.usersMapFile, importPassFile, options.delay);
        }

        //Add Categories (all created by admin)
        if(options.categoryCsvFile!=null)
        {
            addCategories(Paths.get(options.categoryCsvFile),  options.categoryMapFile,options.delay);
        }

        //Add Topics
        if(options.topicCsvFile!=null)
        {
            //if categories were already created form an earlier run we creating mapping based on the
            //mapping output file
            if(options.categoryCsvFile==null)
            {
                Path categoryMapFile = Paths.get(options.categoryMapFile);
                List<String> categorys = Files.readAllLines(categoryMapFile, Charset.forName("iso-8859-1"));
                for(int i=1; i< categorys.size(); i++)
                {
                    String[] categoryPair =  categorys.get(i).split(";");
                    if(categoryPair.length==2)
                    {
                        categoryMap.put(Integer.parseInt(categoryPair[0]), Integer.parseInt(categoryPair[1]));
                    }
                }
            	System.out.println("Loaded " + categoryMap.size() + " previous category ID mappings.");
            }
            addTopics(Paths.get(options.topicCsvFile), options.topicMapFile, options.topicRedirectMapFile, options.delay);
        }

        //Add Posts
        if(options.postCsvFile!=null)
        {
            //if topics were already created form an earlier run we creating mapping based on the
            //mapping output file
            if(options.topicCsvFile==null)
            {
                Path topicMapFile = Paths.get(options.topicMapFile);
                List<String> topics = Files.readAllLines(topicMapFile, Charset.forName("utf8"));
                for(int i=1; i< topics.size(); i++)
                {
                    String[] topicPair =  topics.get(i).split(";");
                    if(topicPair.length==2)
                    {
                        topicMap.put(Integer.parseInt(topicPair[0]), Integer.parseInt(topicPair[1]));
                    }
                }
            	System.out.println("Loaded " + topicMap.size() + " previous topic ID mappings.");
            }
            
            // also load previous post mapping
            if (options.postMapFile != null)
            {
                Path postMapFile = Paths.get(options.postMapFile);
                if (postMapFile.toFile().exists())
                {
	                List<String> posts = Files.readAllLines(postMapFile, Charset.forName("utf8"));
	                for(int i=1; i< posts.size(); i++)
	                {
	                    String[] postPair =  posts.get(i).split(";");
	                    if(postPair.length==2)
	                    {
	                        postMap.put(Integer.parseInt(postPair[0]), Integer.parseInt(postPair[1]));
	                    }
	                }
	            	System.out.println("Loaded " + postMap.size() + " previous post ID mappings.");
                }
            }

            addPosts(Paths.get(options.postCsvFile), options.postMapFile, options.delay);
        }
    }



    public static CmdLineOptions verifyOptions(final String[] args)
    {

        CmdLineOptions options = new CmdLineOptions();
        try
        {
            /*JCommander parser =*/ new JCommander(options, args);
        }
        catch(Exception cme)
        {
            cme.printStackTrace();
            printUsage();
            System.exit(1);
            return null;
        }

        if(
                Strings.isStringEmpty(options.apiKey) ||
                Strings.isStringEmpty(options.apiUsername) ||
                Strings.isStringEmpty(options.website)
            )
        {
            printUsage();
            System.exit(1);
            return null;
        }

        if(
                Strings.isStringEmpty(options.usersCsvFile)
                && Strings.isStringEmpty(options.categoryCsvFile)
                && Strings.isStringEmpty(options.topicCsvFile)
                && Strings.isStringEmpty(options.postCsvFile))
        {
            System.out.println("Must select at least users, categories, topics or posts file");
            printUsage();
            System.exit(1);
            return null;
        }

        System.out.println("apiKey:"        + options.apiKey);
        System.out.println("Username:"      + options.apiUsername);
        System.out.println("Website:"       + options.website);

        if(options.usersCsvFile!=null)
        {
            Path path = Paths.get(options.usersCsvFile);
            if (!Files.exists(path))
            {
                System.out.println("Users csvFile does not exist:" + path);
                printUsage();
                System.exit(1);
                return null;
            }
            System.out.println("UsersCsvFile:"  + options.usersCsvFile);
        }

        if(options.categoryCsvFile!=null)
        {
            Path path = Paths.get(options.categoryCsvFile);
            if (!Files.exists(path))
            {
                System.out.println("Category csvFile does not exist:" + path);
                printUsage();
                System.exit(1);
                return null;
            }
            System.out.println("CategoryCsvFile:"  + options.categoryCsvFile);
        }

        if(options.topicCsvFile!=null)
        {
            Path path = Paths.get(options.topicCsvFile);
            if (!Files.exists(path))
            {
                System.out.println("Topics csvFile does not exist:" + path);
                printUsage();
                System.exit(1);
                return null;
            }
            System.out.println("TopicCsvFile:"  + options.topicCsvFile);

            if(options.categoryCsvFile==null && options.categoryMapFile==null)
            {
                System.out.println("To import Topics must either also import Categories or specify a Category Map File");
                System.exit(1);
            }
        }

        if(options.postCsvFile!=null)
        {
            Path path = Paths.get(options.postCsvFile);
            if (!Files.exists(path))
            {
                System.out.println("Post csvFile does not exist:" + path);
                printUsage();
                System.exit(1);
                return null;
            }
            System.out.println("PostCsvFile:"  + options.postCsvFile);

            if(options.topicCsvFile==null && options.topicMapFile==null)
            {
                System.out.println("To import Posts must either also import Topics or specify a Topic Map File");
                System.exit(1);
            }
        }
        return options;
    }

    public static void printUsage()
    {
        System.out.println("DiscourseTransfer [-d] -u username -a apiKey -au allUsersApiKey -w website [-uf userCsvFile]  [-um userMapFile] [-p passwordoutputfile] [-cf categoryCsvFile] [-cm categoryMapFile][-tf topicCsvFile][-tm topicMapFile] [-pf postCsvFile] [-pm postMapFile]");
    }

    private static boolean prevalidateUsers(List<String> users)
    {
        //What Data do we have
        String[] fieldNames = users.get(0).split(";");

        HashMap<String, String> fieldNameToFieldMap = new HashMap<>();

        HashSet<String> dupUserNames = new HashSet<>();
        List<String>    dupUserNamesOutput = new ArrayList<>();
        List<String>    invalidUserNamesOutput = new ArrayList<>();
        List<String>    noEmailsOutput = new ArrayList<>();
        HashSet<String> dupEmails = new HashSet<>();
        List<String>    invalidEmailsOutput = new ArrayList<>();
        List<String>    spamEmailsOutput = new ArrayList<>();
        List<String>    dupEmailsOutput = new ArrayList<>();

        for(int i=1; i < users.size(); i++)
        {
            String user = users.get(i);
            String[] fields = user.split(";");
            for(int j=0;j<fieldNames.length; j++)
            {
                fieldNameToFieldMap.put(fieldNames[j], fields[j]);
            }

            if(Strings.isStringEmpty(fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName())))
            {
                noEmailsOutput.add(user);
            }

            if(
                    (!fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()).contains("@"))
                    ||
                    (!fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()).contains("."))
                    ||
                    (fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()).matches(".+@.+@.+")))

            {
                invalidEmailsOutput.add(user);
            }

            //TODO not sure where Discourse stores list
            if(fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()).contains("mailinator.com"))
            {
                spamEmailsOutput.add(user);
            }

            if(!dupEmails.add(fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()).toLowerCase()))
            {
                dupEmailsOutput.add(user);
            }

            if(
                    fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()).contains("_")||
                    fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()).contains(" ")||
                    fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()).contains("*")||
                    fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()).contains(".")||
                    fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()).contains("@")||
                    fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()).contains("!")
            )
            {
                invalidUserNamesOutput.add(user);
            }

            if(!dupUserNames.add(fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()).toLowerCase()))
            {
                dupUserNamesOutput.add(user);
            }

        }

        if(noEmailsOutput.size()>0)
        {
            System.out.println("\n----Users with no email address, Need to add email address:");
            for(String next:noEmailsOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                System.out.println(fieldNameToFieldMap.get(UserCsv.USERID.getFieldName()) +";" + fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()));
            }
        }

        if(invalidEmailsOutput.size()>0)
        {
            System.out.println("\n----Users with invalid email address, Need to fix email address:");
            for(String next:invalidEmailsOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                System.out.println(fieldNameToFieldMap.get(UserCsv.USERID.getFieldName()) +";" + fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()));
            }
        }

        if(spamEmailsOutput.size()>0)
        {
            System.out.println("\n----Users with email domain that Discourse considers spam, Need to fix email address:");
            for(String next:spamEmailsOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                System.out.println(fieldNameToFieldMap.get(UserCsv.USERID.getFieldName()) +";" + fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()));
            }
        }

        if(dupEmailsOutput.size()>0)
        {
            System.out.println("\n----Multiple Users with same email address, need to merge users into one (and edit topics.csv/posts.csv accordingly):");

            for(String next:dupEmailsOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                String dupEmail = fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName());

                for(int i=1; i < users.size(); i++)
                {
                    String user = users.get(i);
                    fieldNameToFieldMap.clear();
                    fields = user.split(";");
                    for(int j=0;j<fieldNames.length; j++)
                    {
                        fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                    }

                    if(fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()).equals(dupEmail))
                    {
                        System.out.println(dupEmail + ";"
                                + fieldNameToFieldMap.get(UserCsv.USERID.getFieldName()) +";"
                                + fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()));
                    }


                }
            }
        }

        if(dupUserNamesOutput.size()>0)
        {
            System.out.println("\n----Multiple Users with same username, need to remove one line from users.csv");
            for(String next:dupUserNamesOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                System.out.println(fieldNameToFieldMap.get(UserCsv.USERID.getFieldName())
                        +";" + fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName())
                        +";" + fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()));
            }
        }

        if(invalidUserNamesOutput.size()>0)
        {
            System.out.println("\n----Invalid Usernames, need to modify username and edit topics.csv/posts.csv to match");
            for(String next:invalidUserNamesOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                System.out.println(fieldNameToFieldMap.get(UserCsv.USERID.getFieldName())
                        +";" + fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()));
            }



            System.out.println("\n----Possible sed to replace all three files");
            for(String next:invalidUserNamesOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }

                String oldUserName = fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName());
                String newUserName = oldUserName
                        .replace("*","")
                        .replaceAll("@.*$","")
                        .replace(" ","")
                        .replace("_","")
                        .replace(".","")
                        .replace("!","")
                        ;
                System.out.println("sed -i 's/;"+oldUserName + ";/;"+newUserName+";/g' users.csv;");
                System.out.println("sed -i 's/;"+oldUserName + ";/;"+newUserName+";/g' topics.csv;");
                System.out.println("sed -i 's/;"+oldUserName + ";/;"+newUserName+";/g' posts.csv;");
            }
        }

        if(dupEmailsOutput.size()>0)
        {
            System.out.println("\n----Possible sed to merge multiple users with same email");
            for(String next:dupEmailsOutput)
            {
                fieldNameToFieldMap.clear();
                String[] fields = next.split(";");
                for(int j=0;j<fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                String dupEmail = fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName());

                List<String> usersWithSameEmail = new ArrayList<>();
                for(int i=1; i < users.size(); i++)
                {
                    String user = users.get(i);
                    fieldNameToFieldMap.clear();
                    fields = user.split(";");
                    for (int j = 0; j < fieldNames.length; j++)
                    {
                        fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                    }

                    if (fieldNameToFieldMap.get(UserCsv.EMAIL.getFieldName()).equals(dupEmail))
                    {
                        usersWithSameEmail.add(fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()));
                    }
                }

                if(usersWithSameEmail.size()==2)
                {
                    System.out.println("sed -i 's/;"+usersWithSameEmail.get(1) + ";/;"+usersWithSameEmail.get(0)+";/g' topics.csv;");
                    System.out.println("sed -i 's/;"+usersWithSameEmail.get(1) + ";/;"+usersWithSameEmail.get(0)+";/g' posts.csv;");
                }
            }
        }

        if(dupEmailsOutput.size()>0 ||
                dupUserNamesOutput.size()>0 ||
                invalidUserNamesOutput.size() >0 ||
                noEmailsOutput.size()>0 ||
                spamEmailsOutput.size()>0 ||
                invalidEmailsOutput.size()>0)
        {
            return false;
        }
        return true;
    }

    /** Create user from file, activate them and optionally create import file for database for importpass so will
     *  recognise users existing password
     *
     * @param usersCsvFile
     * @param importPassFile
     * @throws Exception
     */
    public static void addUsers(Path usersCsvFile, String map, Path importPassFile, boolean isDelay) throws Exception
    {
        System.out.println("Creating Users...");

        List<String> users = Files.readAllLines(usersCsvFile, Charset.forName("iso-8859-1"));

        if(!prevalidateUsers(users))
        {
            System.out.println("Exiting because of issues with users import file");
            System.exit(1);
        }

        if(importPassFile!=null)
        {
            if(Files.exists(importPassFile))
            {
                Files.delete(importPassFile);
            }
            importPassFile = Files.createFile(importPassFile);
        }
        else
        {
        	System.out.println("- WARNING: no password file given. Will create users with random password.");
        }

        //What Data do we have
        String[] fieldNames = users.get(0).split(";");

        HashMap<String, String> fieldNameToFieldMap = new HashMap<>();
        Path mapFile = initMap(map,"originalUserId;discourseUserId", true/*deleteFirst*/);


        //For each user
        for(int i=1; i < users.size(); i++)
        {
            String user = users.get(i);
            String[] fields = user.split(";");
            if(fields.length!=fieldNames.length)
            {
                System.out.println("Csv Field Count Incorrect:"+user);
                System.exit(1);
            }

            //Seems okay lets create
            for(int j=0;j<fieldNames.length; j++)
            {
                fieldNameToFieldMap.put(fieldNames[j], fields[j]);
            }
            CreateUser createUser = new CreateUser(); 
            int userId = createUser.loadandCreate(fieldNameToFieldMap, mapFile);
            if(userId>0)
            {
                //If have have hash add to the sql file
                if(importPassFile!=null && fieldNameToFieldMap.containsKey(UserCsv.HASH.getFieldName()))
                {
                    Files.write(
                            importPassFile,
                            new String(userId + ";"
                                    +  "import_pass;"
                                    +  fieldNameToFieldMap.get(UserCsv.HASH.getFieldName()) +";"
                                    +  "now();"
                                    +  "now()"
                                    + "\n")
                                    .getBytes(Charset.forName("UTF8")),
                            StandardOpenOption.APPEND);
                }

                if (!createUser.didUserExist())
                {
	                //https://meta.discourse.org/t/can-you-bypass-the-activation-email/136830
	                new DeactivateUser().deactivate(userId);
	                new ActivateUser().activate(userId);
                }
            }
            else
            {
                System.out.println("Unable to Create User:"+fieldNameToFieldMap.get(UserCsv.USERNAME.getFieldName()));
                System.exit(1);
            }

            if(isDelay)
            {
                Thread.sleep(2000);
            }
        }
    }

    /**
     * Create categories from file
     *
     * @param categoryCsvFile
     * @throws Exception
     */
    public static void addCategories(Path categoryCsvFile, String categoryMapFile, boolean isDelay) throws Exception
    {
        System.out.println("Creating Categories...");

        List<String> categories = Files.readAllLines(categoryCsvFile,Charset.forName("iso-8859-1"));
        String[] fieldNames = categories.get(0).split(";");
        HashMap<String, String> fieldNameToFieldMap = new HashMap<>();
        Path mapFile = initMap(categoryMapFile,"originalCategoryId;discourseCategoryId", true/*deleteFirst*/);
        for (int i = 1; i < categories.size(); i++)
        {
            String category = categories.get(i);
            String[] fields = category.split(";");
            if (fields.length != fieldNames.length)
            {
                System.out.println("Csv Field Count Incorrect:" + category);
                System.exit(1);
            }

            for(int j=0;j<fieldNames.length; j++)
            {
                fieldNameToFieldMap.put(fieldNames[j], fields[j]);
            }

            int categoryId = new CreateCategory().loadandCreate(fieldNameToFieldMap, mapFile);
            if(categoryId<=0)
            {
                System.out.println("Unable to Create Category:" + fieldNameToFieldMap.get(CategoryCsv.NAME.getFieldName()));
                System.exit(1);
            }

            if(isDelay)
            {
                Thread.sleep(2000);
            }
        }
    }

    /**
     * Create topics from file
     *
     * @param topicCsvFile
     * @throws Exception
     */
    public static void addTopics(Path topicCsvFile, String topicsMappingOutput, String topicsRedirectMappingOutput, boolean isDelay) throws Exception
    {
        List<String> topics = Files.readAllLines(topicCsvFile, Charset.forName("utf-8"));
        String[] fieldNames = topics.get(0).split(";");
        HashMap<String, String> fieldNameToFieldMap = new HashMap<>();
        System.out.println("Creating Topics.....");
        Path mapFile = initMap(topicsMappingOutput,"originalTopicId;discourseTopicId", false/*deleteFirst*/);
        Path redirectMapFile = initMap(topicsRedirectMappingOutput,"originalURL;discourseURL", false/*deleteFirst*/);
        for (int i = 1; i < topics.size(); i++)
        {
            String topic = topics.get(i);
            String[] fields = topic.split(";");
            if (fields.length != fieldNames.length)
            {
                System.out.println("Csv Field Count Incorrect:" + topic);
                System.exit(1);
            }
            else
            {
                fieldNameToFieldMap = new HashMap<>();
                for (int j = 0; j < fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }

                try
                {
                    int result = new CreateTopic().loadandCreate(fieldNameToFieldMap, mapFile, redirectMapFile);
                    if (result <= 0)
                    {
                        System.out.println("*****Unable to create topic:" + topic);
                    }
                }
                catch(Exception ex)
                {
                    System.out.println("*****Unable to create topic:" + topic);
                    ex.printStackTrace(System.out);
                }

                if(isDelay)
                {
                    Thread.sleep(2000);
                }
            }
        }
    }


    /**
     * Create Posts from file
     *
     * @param postCsvFile
     * @param isDelay
     * @throws Exception
     */

    public static void addPosts(Path postCsvFile, String map, boolean isDelay) throws Exception
    {
        List<String> posts = Files.readAllLines(postCsvFile, Charset.forName("utf8"));
        String[] fieldNames = posts.get(0).split(";");
        HashMap<String, String> fieldNameToFieldMap = new HashMap<>();
        System.out.println("Creating Posts....");
        Path mapFile = initMap(map,"originalPostId;discoursePostId", false/*deleteFirst*/);
        for (int i = 1; i <posts.size(); i++)
        {
            String post = posts.get(i);
            String[] fields = post.split(";");
            if (fields.length != fieldNames.length)
            {
                System.out.println("Csv Field Count Incorrect:" + post);
                System.exit(1);
            }
            else
            {
                for (int j = 0; j < fieldNames.length; j++)
                {
                    fieldNameToFieldMap.put(fieldNames[j], fields[j]);
                }
                try
                {
                    int result = new CreatePost().loadandCreate(fieldNameToFieldMap, mapFile);
                    if(result<=0)
                    {
                        System.out.println("Unable To Create "+post);
                    }
                }
                catch(Exception ex)
                {
                    System.out.println("Unable To Create "+post);
                    ex.printStackTrace(System.out);
                }

                if(isDelay)
                {
                    Thread.sleep(2000);
                }
            }
        }
    }

    /**
     * Remove old map file and create new one with correct header
     * @param map
     * @param headerText
     * @return
     * @throws Exception
     */
    private static Path initMap(String map, String headerText, boolean deleteFirst) throws Exception
    {
        Path mapFile=null;
        if(map!=null)
        {
            mapFile = Paths.get(map);
            if(Files.exists(mapFile))
            {
            	if (!deleteFirst)
            	{
            		return mapFile;
            	}
                Files.delete(mapFile);
            }
            mapFile = Files.createFile(mapFile);

            Files.write(
                    mapFile,
                    new String(headerText + "\n")
                            .getBytes(Charset.forName("UTF8")),
                    StandardOpenOption.APPEND);
        }
        return mapFile;
    }
}