package net.jthink.discoursetransfer;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO should be incoroprated into main process
public class UpdateViewCount
{
    public static void main(final String[] args) throws Exception
    {
        List<String> topicsMap = Files.readAllLines(Paths.get("src/main/resources/topics_map.csv"), Charset.forName("utf-8"));
        Map<String, String> forumIdDiscourseId= new HashMap<>();
        for (int i = 1; i < topicsMap.size(); i++)
        {
            String next   = topicsMap.get(i);
            String[] pair = next.split(";");
            forumIdDiscourseId.put(pair[0], pair[1]);
        }

        List<String> viewCounts = Files.readAllLines(Paths.get("src/main/resources/topicviews.csv"), Charset.forName("utf-8"));
        Map<String, String> forumIdViewCount= new HashMap<>();
        for (int i = 1; i < viewCounts.size(); i++)
        {
            String next   = viewCounts.get(i);
            String[] pair = next.split(";");
            forumIdViewCount.put(pair[0], pair[1]);
        }

        Path viewImportFile = Files.createFile(Paths.get("src/main/resources/viewimport.csv"));

        //Now create map from discourse topic id to view count
        for(Map.Entry<String, String> next :forumIdDiscourseId.entrySet())
        {
            String viewCount = forumIdViewCount.get(next.getKey());

            Files.write(
                    viewImportFile,
                    new String("update topics set views=" + viewCount + " where id=" + next.getValue()
                            + ";\n")
                            .getBytes(Charset.forName("UTF8")),
                    StandardOpenOption.APPEND);
        }
        //Create .csv file to import into discourse database
    }
}
