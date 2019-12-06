package eu.bcvsolutions.idm;

import java.io.InputStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.scheduler.config.AbstractScheduledTaskInitializer;

/**
 * Implementation of {@link AbstractScheduledTaskInitializer} for initial core
 * long running task.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@DependsOn("initApplicationData")
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
public class InitCoreScheduledTask extends AbstractScheduledTaskInitializer {

	private static final String CORE_SCHEDULED_TASK_XML = "CoreScheduledTasks.xml";
	
	@Override
	protected InputStream getTasksInputStream() {
		return this.getClass().getClassLoader().getResourceAsStream(getTasksXmlPath());
	}

	@Override
	protected String getTasksXmlPath() {
		return DEFAULT_RESOURCE + CORE_SCHEDULED_TASK_XML;
	}

}
