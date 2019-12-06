package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;

import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowHistoricProcessInstanceDto;

/**
 * Service for control workflow historic process instances.
 * 
 * @author svandav
 *
 */
public interface WorkflowHistoricProcessInstanceService extends ReadDtoService<WorkflowHistoricProcessInstanceDto, WorkflowFilterDto> {
	
	String SORT_BY_START_TIME = "startTime";
	String SORT_BY_END_TIME = "endTime";
	String PROCESS_INSTANCE_NAME = "processInstanceName";

	/**
	 * Search historic process instance by ID. Historic process have same ID as process.
	 * @param historicProcessInstanceId
	 * @return
	 */
	WorkflowHistoricProcessInstanceDto get(String historicProcessInstanceId);

	/**
	 * Generate diagram for historic process. In diagram are highlight used paths. If isn't process ended, then only current activity is highlight.
	 * @param processInstanceId
	 * @return
	 */
	InputStream getDiagram(String processInstanceId);

}
