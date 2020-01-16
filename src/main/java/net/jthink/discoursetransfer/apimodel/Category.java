package net.jthink.discoursetransfer.apimodel;

public class Category
{
    private int     id;
    private String  name;
    private String  color;
    private String  text_color;
    private String  slug;
    private int     topic_count;
    private int     post_count;
    private int     position;
    private String  description;
    private String  description_text;
    private String  description_excerpt;



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

    public String getColor()
    {
        return color;
    }

    public void setColor(String color)
    {
        this.color = color;
    }

    public String getText_color()
    {
        return text_color;
    }

    public void setText_color(String text_color)
    {
        this.text_color = text_color;
    }

    public String getSlug()
    {
        return slug;
    }

    public void setSlug(String slug)
    {
        this.slug = slug;
    }

    public int getTopic_count()
    {
        return topic_count;
    }

    public void setTopic_count(int topic_count)
    {
        this.topic_count = topic_count;
    }

    public int getPost_count()
    {
        return post_count;
    }

    public void setPost_count(int post_count)
    {
        this.post_count = post_count;
    }

    public int getPosition()
    {
        return position;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDescription_text()
    {
        return description_text;
    }

    public void setDescription_text(String description_text)
    {
        this.description_text = description_text;
    }

    public String getDescription_excerpt()
    {
        return description_excerpt;
    }

    public void setDescription_excerpt(String description_excerpt)
    {
        this.description_excerpt = description_excerpt;
    }
}
