package net.jthink.discoursetransfer.csv;

public enum CategoryCsv
{
    ID("id"),
    NAME("name"),
    COLOR("color"),
    TEXT_COLOR("text_color"),
    ;

    private String fieldName;

    CategoryCsv(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
