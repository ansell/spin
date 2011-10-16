package org.topbraid.spin.statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * A singleton managing statistics for SPIN execution.
 * In TopBraid, this singleton is used as a single entry point for various
 * statistics producing engines such as TopSPIN.
 * The results are displayed in the SPIN Statistics view of TBC.
 * 
 * The SPINStatisticsManager is off by default, and needs to be activated
 * with <code>setRecording(true);</code>.
 * 
 * @author Holger Knublauch
 */
public class SPINStatisticsManager {

	private static SPINStatisticsManager singleton = new SPINStatisticsManager();
	
	/**
	 * Gets the singleton instance of this class.
	 * @return the SPINStatisticsManager (never null)
	 */
	public static SPINStatisticsManager get() {
		return singleton;
	}
	
	
	private Set<SPINStatisticsListener> listeners = new HashSet<SPINStatisticsListener>();
	
	private boolean recording;
	
	private List<SPINStatistics> stats = new LinkedList<SPINStatistics>();
	
	
	public void addListener(SPINStatisticsListener listener) {
		listeners.add(listener);
	}
	

	/**
	 * Adds new statistics and notifies any registered listeners.
	 * This should only be called if <code>isRecording()</code> is true
	 * to prevent the unnecessary creation of SPINStatistics objects.
	 * @param values  the statistics to add
	 */
	public void add(Iterable<SPINStatistics> values) {
		for(SPINStatistics s : values) {
			stats.add(s);
		}
		notifyUpdated();
	}
	
	
	/**
	 * Gets all previously added statistics.
	 * @return the statistics
	 */
	public List<SPINStatistics> getStatistics() {
		return stats;
	}
	
	
	public boolean isRecording() {
		return recording;
	}
	
	
	public void removeListener(SPINStatisticsListener listener) {
		listeners.remove(listener);
	}
	
	
	public void reset() {
		stats.clear();
		notifyUpdated();
	}
	
	
	private void notifyUpdated() {
		for(SPINStatisticsListener listener : new ArrayList<SPINStatisticsListener>(listeners)) {
			listener.statisticsUpdated();
		}
	}
	
	
	public void setRecording(boolean value) {
		this.recording = value;
	}
}
