package eu.bcvsolutions.idm.core.scheduler.service.impl;

/**
 * Test task for {@link DefaultSchedulerManagerIntegrationTest}.
 * 
 * @author Radek Tomiška
 *
 */
public class TestSchedulableDryRunTask extends TestSchedulableTask {
	
	@Override
	public boolean supportsDryRun() {
		return true;
	}
}
