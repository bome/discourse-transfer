package net.jthink.discoursetransfer.apimodel;


public class CreateTopicSend
{
    private String title;
    private String raw;
    private int category;
    private String created_at;
    private boolean skip_validations;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getRaw()
    {
        return raw;
    }

    public void setRaw(String raw)
    {
        this.raw = raw;
    }

    public int getCategory()
    {
        return category;
    }

    public void setCategory(int category)
    {
        this.category = category;
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
}
