package eu.bcvsolutions.idm.core.workflow.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourceWrapper;
import eu.bcvsolutions.idm.core.api.rest.domain.ResourcesWrapper;
import eu.bcvsolutions.idm.core.workflow.model.dto.FormDataWrapperDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowFilterDto;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import eu.bcvsolutions.idm.core.workflow.service.WorkflowTaskInstanceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Rest controller for workflow instance tasks
 * 
 * TODO: secure endpoints
 * 
 * @author svandav
 *
 */
@RestController
@RequestMapping(value = BaseDtoController.BASE_PATH + "/workflow-tasks")
@Api(
		value = WorkflowTaskInstanceController.TAG,  
		tags = { WorkflowTaskInstanceController.TAG }, 
		description = "Running WF tasks",
		produces = BaseController.APPLICATION_HAL_JSON_VALUE,
		consumes = MediaType.APPLICATION_JSON_VALUE)
public class WorkflowTaskInstanceController {

	protected static final String TAG = "Workflow - task instances";
	//
	@Autowired
	private WorkflowTaskInstanceService workflowTaskInstanceService;
	@Value("${spring.data.rest.defaultPageSize}")
	private int defaultPageSize;

	/**
	 * Search instances of tasks with same variables and for logged user
	 * 
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/search")
	@ApiOperation(
			value = "Search task instances (/search/quick alias)", 
			nickname = "searchTaskInstances", 
			tags = { WorkflowTaskInstanceController.TAG })
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> search(
			@RequestBody WorkflowFilterDto filter) {

		ResourcesWrapper<WorkflowTaskInstanceDto> result = workflowTaskInstanceService.search(filter);
		List<WorkflowTaskInstanceDto> tasks = (List<WorkflowTaskInstanceDto>) result.getResources();
		List<ResourceWrapper<WorkflowTaskInstanceDto>> wrappers = new ArrayList<>();

		for (WorkflowTaskInstanceDto task : tasks) {
			wrappers.add(new ResourceWrapper<WorkflowTaskInstanceDto>(task));
		}
		ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>> resources = new ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>(
				wrappers);
		resources.setPage(result.getPage());
		return new ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>>(resources, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/search/quick")
	@ApiOperation(
			value = "Search task instances", 
			nickname = "searchQuickTaskInstances", 
			tags = { WorkflowTaskInstanceController.TAG })
	public ResponseEntity<ResourcesWrapper<ResourceWrapper<WorkflowTaskInstanceDto>>> searchQuick(
			@RequestParam(required = false) Integer size, @RequestParam(required = false) Integer page, @RequestParam(required = false) String sort, @RequestParam(required = false) String processInstanceId) {
		
		WorkflowFilterDto filter = new WorkflowFilterDto(size != null ? size : defaultPageSize);
		if (page != null) {
			filter.setPageNumber(page);
		}
		filter.initSort(sort);
		filter.setProcessInstanceId(processInstanceId);
		return this.search(filter);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{backendId}")
	@ApiOperation(
			value = "Historic task instance detail", 
			nickname = "getHistoricTaskInstance", 
			response = WorkflowTaskInstanceDto.class, 
			tags = { WorkflowTaskInstanceController.TAG })
	public ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>> get(
			@ApiParam(value = "Task instance id.", required = true)
			@PathVariable String backendId) {
		WorkflowTaskInstanceDto taskInstanceDto = workflowTaskInstanceService.get(backendId);
		//
		if (taskInstanceDto == null) {
			// TODO: NOT_FOUND instead?
			throw new ResultCodeException(CoreResultCode.FORBIDDEN, ImmutableMap.of("taskId", backendId));
		}
		//
		ResourceWrapper<WorkflowTaskInstanceDto> resource = new ResourceWrapper<WorkflowTaskInstanceDto>(
				taskInstanceDto);
		return new ResponseEntity<ResourceWrapper<WorkflowTaskInstanceDto>>(resource, HttpStatus.OK);
	}

	@RequestMapping(method = RequestMethod.PUT, value = "/{backendId}/complete")
	@ApiOperation(
			value = "Complete task instance", 
			nickname = "completeTaskInstance",
			tags = { WorkflowTaskInstanceController.TAG },
			notes = "Complete task with given decision.")
	public void completeTask(
			@ApiParam(value = "Task instance id.", required = true)
			@PathVariable String backendId, 
			@ApiParam(value = "Complete decision, variables etc.", required = true)
			@RequestBody FormDataWrapperDto formData) {
		workflowTaskInstanceService.completeTask(backendId, formData.getDecision(), formData.getFormData(), formData.getVariables());
		// 
		// TODO: no content should be returned
		// return new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
	}

}
