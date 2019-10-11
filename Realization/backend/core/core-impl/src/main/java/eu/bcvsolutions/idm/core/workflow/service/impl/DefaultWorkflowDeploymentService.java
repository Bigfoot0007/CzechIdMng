package eu.bcvsolutions.idm.core.workflow.service.impl;

import java.io.InputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.workflow.api.dto.WorkflowDeploymentDto;
import eu.bcvsolutions.idm.core.workflow.api.service.WorkflowDeploymentService;

/**
 * Default implementation of workflow service for deployment
 * 
 * @author svandav
 *
 */

@Service
public class DefaultWorkflowDeploymentService implements WorkflowDeploymentService {

	@Autowired
	private RepositoryService repositoryService;

	/**
	 * Upload new deployment to Activiti
	 */
	@Override
	public WorkflowDeploymentDto create(String deploymentName, String fileName, InputStream inputStream) {
		Deployment deployment = repositoryService.createDeployment().addInputStream(fileName, inputStream)
				.name(deploymentName).deploy();

		return new WorkflowDeploymentDto(deployment);
	}

}
