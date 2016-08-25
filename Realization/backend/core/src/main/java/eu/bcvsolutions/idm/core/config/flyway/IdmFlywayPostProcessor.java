package eu.bcvsolutions.idm.core.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.stereotype.Component;

/**
 * {@link FlywayMigrationStrategy} only can't be used for modular flyway configuration. 
 * We need to use {@link FlywayMigrationStrategy} directly after module dependent {@link Flyway} is created.
 * 
 * @author Radek Tomiška
 *
 */
@Component(IdmFlywayPostProcessor.NAME)
public class IdmFlywayPostProcessor implements BeanPostProcessor {
	
	public static final String NAME = "flywayPostProcessor";
	
	@Autowired
	private FlywayMigrationStrategy flywayMigrationStrategy;

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof Flyway) {
			flywayMigrationStrategy.migrate((Flyway) bean);
		}	
		return bean;
	}

}
