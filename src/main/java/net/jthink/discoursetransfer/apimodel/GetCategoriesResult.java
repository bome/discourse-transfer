package net.jthink.discoursetransfer.apimodel;

public class GetCategoriesResult
{
	private List category_list;
	
	public static class List {
		private boolean can_create_category;
		private boolean can_create_topic;
		private boolean draft;
		private String draft_key;
		private int draft_sequence;
		private Category[] categories;
		
		/**
		 * @return the can_create_category
		 */
		public boolean isCan_create_category() {
			return can_create_category;
		}
		/**
		 * @return the can_create_topic
		 */
		public boolean isCan_create_topic() {
			return can_create_topic;
		}
		/**
		 * @return the draft
		 */
		public boolean isDraft() {
			return draft;
		}
		/**
		 * @return the draft_key
		 */
		public String getDraft_key() {
			return draft_key;
		}
		/**
		 * @return the draft_sequence
		 */
		public int getDraft_sequence() {
			return draft_sequence;
		}
		/**
		 * @return the categories
		 */
		public Category[] getCategories() {
			return categories;
		}
	}

	/**
	 * @return the category_list
	 */
	public List getCategoryList() {
		return category_list;
	}
	
}
