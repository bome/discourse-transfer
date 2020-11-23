package net.jthink.discoursetransfer.csv;

public enum PostCsv
{
    POSTID("postid"),
    TOPICID("topicid"),
    USERNAME("username"),
    /** equivalent to Discourse Category */
    FORUMID("forumid"),
    POST_TIME("post_time"),
    POST_TEXT("post_text"),
    TOPIC_TITLE("topic_title"),
    /** optional: set to 0 if not a reply (comment) to the given topic */
    REPLY_TO_TOPIC_ID("reply_to_topic_id"),
    /** optional: set to 0 if not a reply (comment) to the given post */
    REPLY_TO_POST_ID("reply_to_post_id"),
    /** optional: the original URL (path), for generating the redirect map */
    URL("url")
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
