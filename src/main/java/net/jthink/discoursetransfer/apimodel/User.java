package net.jthink.discoursetransfer.apimodel;

public class User
{
    private int     id;
    private String  username;
    private String  name;
    private String  email;

    //private String  avatar_template;
    //private String[]  secondary_emails;
    //private boolean active;
    //private boolean admin;
    //private boolean moderator;
	//... and many more fields
    
    /**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

    
}
