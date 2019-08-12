package eu.bcvsolutions.idm.core.workflow.service;

import java.io.InputStream;
import java.util.List;

import org.activiti.bpmn.model.ValuedDataObject;
import org.activiti.engine.repository.ProcessDefinition;

import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowProcessDefinitionDto;
/**
 * Service for control workflow definitions.
 * @author svandav
 *
 */
public interface WorkflowProcessDefinitionService extends ReadDtoService<WorkflowProcessDefinitionDto, WorkflowFilterDto> {
	
	String SORT_BY_KEY = "key";
	String SORT_BY_NAME = "name";

	/**
	 * Find all last version and active process definitions
	 */
	List<WorkflowProcessDefinitionDto> findAllProcessDefinitions();

	/**
	 * Search process definition by key. Return only last active version of definition.
	 * @param processDefinitionKey
	 * @return
	 */
	WorkflowProcessDefinitionDto getByName(String processDefinitionKey);

	/**
	 * Find last version of process definition by key and return his ID
	 * 
	 * @param processDefinitionKey
	 * @return
	 */
	String getProcessDefinitionId(String processDefinitionKey);

	/**
	 * Find process definition by its key.
	 * 
	 * @param processDefinitionKey
	 * @return
	 * @since 9.7.2
	 */
	ProcessDefinition getProcessDefinition(String processDefinitionKey);

	/**
	 * Generate diagram for process definition ID
	 */
	InputStream getDiagram(String definitionId);
	
	/**
	 * Generate diagram for process definition key
	 */
	InputStream getDiagramByKey(String definitionKey);

	/**
	 * Returns data object defined in this process
	 * @param definitionId
	 * @return
	 */
	List<ValuedDataObject> getDataObjects(String definitionId);

	/**
	 * Get process definition by key as InputStream.
	 *
	 * @param processDefinitionKey
	 * @return InputStream
	 * @since 9.7.2
	 */
	InputStream getBpmnDefinition(String processDefinitionKey);
}
