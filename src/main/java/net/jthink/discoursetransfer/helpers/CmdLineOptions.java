package net.jthink.discoursetransfer.helpers;

import com.beust.jcommander.Parameter;

/**
 *
 */
public class CmdLineOptions
{

    @Parameter(names="-a", description="api key")
    public String apiKey;

    @Parameter(names="-u", description="api username")
    public String apiUsername;

    @Parameter(names="-w", description="website")
    public String website;

    @Parameter(names="-uf", description="user csv file")
    public String usersCsvFile;

    @Parameter(names="-um", description="user map file")
    public String usersMapFile;

    @Parameter(names="-p", description="import password file to create")
    public String importpassFile;

    @Parameter(names="-cf", description="category csv file")
    public String categoryCsvFile;

    @Parameter(names="-cm", description="category map file to output")
    public String categoryMapFile;

    @Parameter(names="-tf", description="topic csv file")
    public String topicCsvFile;

    @Parameter(names="-tm", description="topics map file to output")
    public String topicMapFile;

    @Parameter(names="-pf", description="post csv file")
    public String postCsvFile;

    @Parameter(names="-pm", description="post map file to output")
    public String postMapFile;

    @Parameter(names="-d", description="delay to keep within default api throttle limit")
    public boolean delay;
}
