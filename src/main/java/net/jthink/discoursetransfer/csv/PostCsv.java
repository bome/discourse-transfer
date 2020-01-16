package net.jthink.discoursetransfer.csv;

public enum PostCsv
{
    POSTID("postid"),
    TOPICID("topicid"),
    USERNAME("username"),
    FORUMID("forumid"),
    POST_TIME("post_time"),
    POST_TEXT("post_text"),
    TOPIC_TITLE("topic_title"),
    ;

    private String fieldName;

    PostCsv(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
