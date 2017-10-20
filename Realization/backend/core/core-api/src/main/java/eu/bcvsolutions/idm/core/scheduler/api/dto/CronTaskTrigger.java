package eu.bcvsolutions.idm.core.scheduler.api.dto;

import javax.validation.constraints.NotNull;

import org.quartz.CronTrigger;

/**
 * Cron task trigger
 * 
 * @author Radek Tomiška
 */
public class CronTaskTrigger extends AbstractTaskTrigger {

	private static final long serialVersionUID = 1L;
	
	@NotNull
	private String cron;
	
	public CronTaskTrigger() {
	}
	
	/**
	 * Creates a new instance using trigger and state
	 * 
	 * @param trigger trigger
	 * @param state state
	 */
	public CronTaskTrigger(String taskId, CronTrigger trigger, TaskTriggerState state) {
		super(taskId, trigger, state);
		
		cron = trigger.getCronExpression();
	}
	
	public String getCron() {
		return cron;
	}
	
	public void setCron(String cron) {
		this.cron = cron;
	}
}
