package eu.bcvsolutions.idm.core.scheduler.api.service;

import java.util.UUID;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Schedulable task services (this services will be automatically available as scheduled tasks)
 * 
 * Scheduler persists LRT only. 
 * LRT is processed asynchronously by different internal scheduled task {@link LongRunningTaskManager#processCreated()}.
 * 
 * @author Radek Tomiška
 * @author Jan Helbich
 * @since 7.6.0
 */
public abstract class AbstractSchedulableTaskExecutor<V> 
		extends AbstractLongRunningTaskExecutor<V> 
		implements SchedulableTaskExecutor<V> {

	@Autowired protected SecurityService securityService;
	@Autowired protected IdmLongRunningTaskService longRunningTaskService;
	@Autowired protected LongRunningTaskManager longRunningTaskManager;
	@Autowired protected IdmScheduledTaskService scheduledTaskService;
	//
	private UUID scheduledTaskId;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		// run as system - called from scheduler internally
		securityService.setSystemAuthentication();
		//
		// scheduled task is quartz reference to IdM entity
		IdmScheduledTaskDto taskDto = getScheduledTask(context);
		//
		// add task to queue only - quartz will start take care of the rest
		createIdmLongRunningTask(context, taskDto);
	}
	
	@Override
	public UUID getScheduledTaskId() {
		if (this.scheduledTaskId == null) {
			IdmScheduledTaskDto scheduledTask = scheduledTaskService
					.findByLongRunningTaskId(this.getLongRunningTaskId());
			this.scheduledTaskId = scheduledTask == null ? null : scheduledTask.getId(); 
		}
		return this.scheduledTaskId;
	}

	protected IdmScheduledTaskDto createIdmScheduledTask(String taskName) {
		IdmScheduledTaskDto t = new IdmScheduledTaskDto();
		t.setQuartzTaskName(taskName);
		return scheduledTaskService.save(t);
	}

	private IdmLongRunningTaskDto createIdmLongRunningTask(
			JobExecutionContext context, IdmScheduledTaskDto taskDto) {
		IdmLongRunningTaskDto longRunningTask = new IdmLongRunningTaskDto();
		longRunningTask.setTaskType(getClass().getCanonicalName());
		longRunningTask.setTaskDescription(context.getJobDetail().getDescription());
		longRunningTask.setTaskProperties(context.getMergedJobDataMap());
		longRunningTask.setResult(new OperationResult.Builder(OperationState.CREATED).build());
		longRunningTask.setInstanceId(context.getMergedJobDataMap().getString(SchedulableTaskExecutor.PARAMETER_INSTANCE_ID));
		longRunningTask.setScheduledTask(taskDto.getId());
		longRunningTask.setStateful(isStateful());
		longRunningTask.setDryRun(context.getMergedJobDataMap().getBoolean(PARAMETER_DRY_RUN));
		// each LRT executed from the queue will have new transaction context
		longRunningTask.getTaskProperties().put(
				LongRunningTaskExecutor.PARAMETER_TRANSACTION_CONTEXT, 
				TransactionContextHolder.createEmptyContext()
				);
		//
		longRunningTask = longRunningTaskService.save(longRunningTask);
		//
		if (!longRunningTaskManager.isAsynchronous()) {
			longRunningTaskManager.processCreated(longRunningTask.getId());
			longRunningTask = longRunningTaskService.get(longRunningTask.getId());
		}
		//
		return longRunningTask;
	}
	
	private IdmScheduledTaskDto getScheduledTask(JobExecutionContext context) {
		String taskName = context.getJobDetail().getKey().getName();
		IdmScheduledTaskDto dto = scheduledTaskService.findByQuartzTaskName(taskName);
		return dto == null ? createIdmScheduledTask(taskName) : dto;
	}

}
