package org.arachb.api.provider;

public interface PageField {

	
	/**
	 * The key for this type as it appears in the tunneled json file
	 * Note: at some point MetaDataSummary might want to switch to enummaps,
	 * will need a string -> enum mapping here though
	 */
	String keyString();
	
	/**
	 * 
	 */
	PageField lookupKey(String key);
	
	/**
	 * hook to provide user friendly field name
	 * @return field's display name 
	 */
	String displayString();
	
	/**
	 * hook to provide formatting of field values
	 * for example, chopping up an author list into a ul display list
	 * @param fieldValue a string to reformat
	 * @return formatted
	 */
	String formatValue(String fieldValue);

}
