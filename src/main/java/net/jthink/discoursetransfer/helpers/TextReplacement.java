package net.jthink.discoursetransfer.helpers;

/**
 * Text Replacement
 *
 * When we create csv files we convert some special chars such as \n, " and ; in the message text because causes issue
 * and then we have to convert tokens back into the original text/bootstrap euivalent
 *
 * Also some conversions are a bit forum specific , i.e in jforum we use [code][/code] but bootstrap uses the ``` ``` markdown format
 */
public class TextReplacement
{
    public static String replaceText(String text)
    {
        text = text.replaceAll("\\^M", "");
        text = text.replaceAll("\\[SEMICOLON\\]", ";");
        text = text.replaceAll("\\[NEWLINE\\]", "\r\n");
        text = text.replaceAll("\\[DOUBLEQUOTE\\]", "\"");
        text = text.replaceAll("\\[code\\]", "```");
        text = text.replaceAll("\\[/code\\]", "\r\n```\r\n");
        return text.trim();
    }
}
