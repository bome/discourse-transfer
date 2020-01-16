package net.jthink.discoursetransfer.apimodel;

public class CreatePostResult
{
    private int     id;
    private String  name;
    private int     topic_id;


    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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
