package org.topbraid.base.util;


/**
 * This allows clients to know some events that happen in the graph store.
 * 
 * @author Jeremy Carroll
 */
// TODO move to another package
public interface IDataUsageListener<T> {



	/**
	 * The classOfInterest was used for purpose for a file with extension.
	 * 
	 * @param classOfInterest item that will be reported as being used in the User Data Usage
	 * @param extension
	 * @param purpose
	 */
	void used(T classOfInterest, String extension, String purpose);

}
