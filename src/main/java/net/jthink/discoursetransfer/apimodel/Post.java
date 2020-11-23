package net.jthink.discoursetransfer.apimodel;

public class Post
{
    private int     id;
    private String  name;
    private String  username;
    private int     post_number;
	//... and many more fields
    
    
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the post_number
	 */
	public int getPostNumber() {
		return post_number;
	}

}
