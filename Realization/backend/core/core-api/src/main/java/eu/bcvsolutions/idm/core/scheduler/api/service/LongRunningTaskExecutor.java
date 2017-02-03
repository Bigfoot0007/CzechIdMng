package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.Map;
import java.util.UUID;

/**
 * Long running task executor
 * 
 * @author Radek Tomiška
 *
 */
public interface LongRunningTaskExecutor extends Runnable {

	/**
	 * Module identifier
	 * 
	 * @return
	 */
	String getModule();
	
	/**
	 * Initialize task executor before task is processed
	 * 
	 * @param context
	 */
	void init(Map<String, Object> properties);
	
	/**
	 * Main execution method
	 */
	void process();
	
	/**
	 * Executors description
	 * 
	 * @return
	 */
	String getDescription();
	
	/**
	 * Returns total item count
	 * 
	 * @return
	 */
	Long getCount();
	
	/**
	 * Returns processed items count
	 * 
	 * @return
	 */
	Long getCounter();
	
	/**
	 * Updates persisted task state (count, counter, etc.)
	 * 
	 * @param context
	 * @return Returns false, when long running task is canceled.
	 */
	boolean updateState();
	
	/**
	 * Gets long running task log id
	 * 
	 * @return
	 */
	UUID getLongRunningTaskId();
	
	/**
	 * Sets long running task log id
	 * 
	 * @param longRunningTask
	 */
	void setLongRunningTaskId(UUID taskId);
}
