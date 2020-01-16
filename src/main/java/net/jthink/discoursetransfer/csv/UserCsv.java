package net.jthink.discoursetransfer.csv;

public enum UserCsv
{
    USERID("userid"),
    USERNAME("username"),
    NAME("name"),
    EMAIL("email"),
    PASSWORD("password"),
    HASH("hash"),
    ;

    private String fieldName;

    UserCsv(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
