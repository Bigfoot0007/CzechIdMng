package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmExportImportService;
import eu.bcvsolutions.idm.core.api.service.ImportManager;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractLongRunningTaskExecutor;

/**
 * Import task
 * 
 * @author Vít Švanda
 *
 */
@Component(ImportTaskExecutor.TASK_NAME)
@Description("Import task")
public class ImportTaskExecutor extends AbstractLongRunningTaskExecutor<OperationResult> {

	public static final String TASK_NAME = "import-long-running-task";

	@Autowired
	private IdmExportImportService service;
	@Autowired
	private ImportManager importManager;
	private UUID batchId;
	private IdmExportImportDto batch;
	private boolean dryRun = false;

	public ImportTaskExecutor() {
		super();
	}

	public ImportTaskExecutor(UUID batchId, boolean dryRun) {
		this.dryRun = dryRun;
		this.batchId = batchId;
	}
	
	@Override
	public String getName() {
		return TASK_NAME;
	}

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
	}

	@Override
	public OperationResult process() {
		batch = getBatch();
		// Start import
		importManager.internalExecuteImport(batch, dryRun, this);

		if (dryRun) {
			return new OperationResult(OperationState.NOT_EXECUTED);
		}
		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	protected OperationResult end(OperationResult result, Exception ex) {
		Assert.notNull(batch, "Batch must exists!");

		OperationResult operationResult = super.end(result, ex);
		
		// If result is 'NOT_EXECUTED' (dryRun), then we have to change result manually.
		if (ex == null && OperationState.NOT_EXECUTED == operationResult.getState()) {
			IdmLongRunningTaskDto task = getLongRunningTaskService().get(getLongRunningTaskId());
			
			ResultModel resultModel = new DefaultResultModel(CoreResultCode.IMPORT_EXECUTED_AS_DRYRUN);
			task.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(resultModel).build());
			
			getLongRunningTaskService().save(task);
		}
		return operationResult;
	}

	private IdmExportImportDto getBatch() {
		IdmExportImportDto batch = service.get(batchId);
		//
		if (batch == null) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND,
					ImmutableMap.of("id", batchId));
		}
		return batch;
	}

	@Override
	public String getDescription() {
		String deafultDescription =  "Import long running task";
		if (batchId == null) {
			return deafultDescription;
		}
		IdmExportImportDto importBatch = service.get(batchId);
		if (importBatch == null) {
			return deafultDescription;
		}
		return MessageFormat.format("Import batch with name: [{0}]", importBatch.getName());
	}

}
