package net.jthink.discoursetransfer.apimodel;


public class CreatePostSend
{
    private int topic_id;
    private String raw;
    private String created_at;
    private boolean skip_validations;

    public String getRaw()
    {
        return raw;
    }

    public void setRaw(String raw)
    {
        this.raw = raw;
    }


    public String getCreated_at()
    {
        return created_at;
    }

    public void setCreated_at(String created_at)
    {
        this.created_at = created_at;
    }


    public boolean isSkip_validations()
    {
        return skip_validations;
    }

    public void setSkip_validations(boolean skip_validations)
    {
        this.skip_validations = skip_validations;
    }


    public int getTopic_id()
    {
        return topic_id;
    }

    public void setTopic_id(int topic_id)
    {
        this.topic_id = topic_id;
    }

}
