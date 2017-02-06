package eu.bcvsolutions.idm.core.scheduler.exception;

import java.util.Map;

import eu.bcvsolutions.idm.core.api.domain.ResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;

/**
 * Scheduler exception
 * 
 * @author Radek Tomiška
 *
 */
public class SchedulerException extends ResultCodeException {

	private static final long serialVersionUID = -9114230584353922445L;

	public SchedulerException(ResultCode resultCode, Map<String, Object> parameters, Throwable throwable) {
		super(resultCode, parameters, throwable);
	}
	
	public SchedulerException(ResultCode resultCode, Map<String, Object> parameters) {
		this(resultCode, parameters, null);
	}
	
	public SchedulerException(ResultCode resultCode, Throwable throwable) {
		this(resultCode, null, throwable);
	}
}
