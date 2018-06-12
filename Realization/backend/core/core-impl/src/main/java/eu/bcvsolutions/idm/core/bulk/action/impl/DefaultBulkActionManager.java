package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.BulkActionManager;
import eu.bcvsolutions.idm.core.api.bulk.action.IdmBulkAction;
import eu.bcvsolutions.idm.core.api.bulk.action.dto.IdmBulkActionDto;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.entity.BaseEntity;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ModuleService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.dto.LongRunningFutureTask;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskManager;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.utils.PermissionUtils;

/**
 * Implementation of manager for bulk action
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service("bulkActionManager")
public class DefaultBulkActionManager implements BulkActionManager {

	private final PluginRegistry<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>, Class<? extends BaseEntity>> pluginExecutors;
	private final LongRunningTaskManager taskManager;
	private final ModuleService moduleService;
	
	@Autowired
	public DefaultBulkActionManager(
			List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> actions,
			LongRunningTaskManager taskManager,
			ModuleService moduleService) {
		pluginExecutors = OrderAwarePluginRegistry.create(actions);
		//
		this.taskManager = taskManager;
		this.moduleService = moduleService;
	}
	
	@Override
	public IdmBulkActionDto processAction(IdmBulkActionDto action) {
		AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> executor = getOperationForDto(action);
		// check if action is available
		if (!moduleService.isEnabled(executor.getModule())) {
			throw new ResultCodeException(CoreResultCode.BULK_ACTION_MODULE_DISABLED, ImmutableMap.of("action", action.getName(), "module", executor.getModule()));
		}
		//
		executor = (AbstractBulkAction<?, ?>) AutowireHelper.createBean(executor.getClass());
		//
		executor.setAction(action);
		//
		// validate before execute
		executor.validate();
		//
		LongRunningFutureTask<OperationResult> execute = taskManager.execute(executor);
		action.setLongRunningTaskId(execute.getExecutor().getLongRunningTaskId());
		action.setEntityClass(executor.getService().getEntityClass().getName());
		action.setFilterClass(executor.getService().getFilterClass().getName());
		action.setModule(executor.getModule());
		action.setFormAttributes(executor.getFormAttributes());
		//
		action.setPermissions(toString(executor.getPermissions()));
		return action;
	}
	
	@Override
	public List<IdmBulkActionDto> getAvailableActions(Class<? extends BaseEntity> entity) {
		List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> actions = pluginExecutors.getPluginsFor(entity);
		//
		List<IdmBulkActionDto> result = new ArrayList<>();
		for (IdmBulkAction<? extends BaseDto, ? extends BaseFilter> action : actions) {
			// skip disabled modules 
			if (!moduleService.isEnabled(action.getModule())) {
				continue;
			}
			IdmBulkActionDto actionDto = new IdmBulkActionDto();
			actionDto.setEntityClass(action.getService().getEntityClass().getName());
			actionDto.setFilterClass(action.getService().getFilterClass().getName());
			actionDto.setModule(action.getModule());
			actionDto.setName(action.getName());
			actionDto.setFormAttributes(action.getFormAttributes());
			actionDto.setPermissions(toString(action.getPermissions()));
			result.add(actionDto);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> getOperationForDto(IdmBulkActionDto actionDto) {
		Assert.notNull(actionDto);
		Assert.notNull(actionDto.getEntityClass());
		try {
			Class<?> forName = Class.forName(actionDto.getEntityClass());
			if (AbstractEntity.class.isAssignableFrom(forName)) {
				List<AbstractBulkAction<? extends BaseDto, ? extends BaseFilter>> actions = pluginExecutors.getPluginsFor((Class<? extends BaseEntity>) forName);
				//
				for (AbstractBulkAction<? extends BaseDto, ? extends BaseFilter> action : actions) {
					if (action.getName().equals(actionDto.getName())) {
						return action;
					}
				}
			}
		} catch (ClassNotFoundException e) {
			throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionClass", actionDto.getEntityClass()), e);
		}
		throw new ResultCodeException(CoreResultCode.NOT_FOUND, ImmutableMap.of("bulkActionName", actionDto.getName()));
	}

	private Map<String, String[]> toString(Map<String, BasePermission[]> permissions) {
		Map<String, String[]> results = new HashMap<>();
		permissions.entrySet().forEach(entry -> {
			results.put(entry.getKey(), PermissionUtils.toString(Arrays.asList(entry.getValue())).toArray(new String[]{}));
		});
		return results;
	}
}
