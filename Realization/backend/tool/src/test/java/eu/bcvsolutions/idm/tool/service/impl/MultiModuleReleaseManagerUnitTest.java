package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

/**
 * Test release on mock repository.
 * 
 * Multi module - module + module-impl + module-api.
 * 
 * @author Radek Tomiška
 *
 */
public class MultiModuleReleaseManagerUnitTest extends AbstractReleaseManagerUnitTest {
	
	protected final static String ROOT_MODULE_ID = "module";

	private ModuleReleaseManager releaseManager = new ModuleReleaseManager(ROOT_MODULE_ID);
	 
    @Override
	protected AbstractReleaseManager getReleaseManager() {
		return releaseManager;
	}
	
	@Override
	protected String prepareRepository() {
		String version = "1.0.0-SNAPSHOT";
		//
		try {
			releaseManager.setQuiet(true); // don't mess test logs
			releaseManager.setLocal(true); // local test repository only
			//
			// prepare mock repository
			File targetFolder = new File("target");
			Assert.assertTrue(targetFolder.exists());
			File repositoryRootFolder = new File(targetFolder.getPath() + "/mockMultiModuleRoot");
			if (repositoryRootFolder.exists()) {
				FileUtils.deleteDirectory(repositoryRootFolder);
			}
			Assert.assertTrue(repositoryRootFolder.mkdir());
			File realizationFolder = new File(repositoryRootFolder.getPath() + "/Realization");
			Assert.assertTrue(realizationFolder.mkdir());
			File backendFolder = new File(realizationFolder.getPath() + "/backend");
			Assert.assertTrue(backendFolder.mkdir());
			File frontendFolder = new File(realizationFolder.getPath() + "/frontend");
			Assert.assertTrue(frontendFolder.mkdir());
			//
			// create all BE and FE modules
			FileUtils.writeStringToFile(new File(frontendFolder.getPath() +"/czechidm-" + ROOT_MODULE_ID + "/package.json"),
					"{ \"version\" : \"1.0.0-snapshot\" }");
			// root
			FileUtils.writeStringToFile(new File(backendFolder.getPath() +"/" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "/pom.xml"),
					"<project>"
					+ "<modelVersion>4.0.0</modelVersion>"
					+ "<parent>"
					  + "<groupId>eu.bcvsolutions.idm</groupId>"
					  + "<artifactId>idm-parent</artifactId>"
					  + "<version>10.0.0</version>"
					+ "</parent>"
					+ "<artifactId>" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "</artifactId>"
					+ "<packaging>pom</packaging>"
					+ "<version>" + version + "</version>"
					+ "<modules>"
						+ "<module>" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "-api</module>"
						+ "<module>" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "-impl</module>"
					+ "</modules>" +
					"</project>");
			// api
			FileUtils.writeStringToFile(new File(backendFolder.getPath() +"/" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID
					+ "/" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "-api/pom.xml"),
					"<project>"
					+ "<modelVersion>4.0.0</modelVersion>"
					+ "<parent>"
					  + "<relativePath>../pom.xml</relativePath>"
					  + "<groupId>eu.bcvsolutions.idm</groupId>"
					  + "<artifactId>" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "</artifactId>"
					  + "<version>" + version + "</version>"
					+ "</parent>"
					+ "<artifactId>" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "-api</artifactId>"
					+ "<packaging>jar</packaging>" +
					"</project>");
			// impl
			FileUtils.writeStringToFile(new File(backendFolder.getPath() +"/" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID 
					+ "/" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "-impl/pom.xml"),
					"<project>"
					+ "<modelVersion>4.0.0</modelVersion>"
					+ "<parent>"
					  + "<relativePath>../pom.xml</relativePath>"
					  + "<groupId>eu.bcvsolutions.idm</groupId>"
					  + "<artifactId>" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "</artifactId>"
					  + "<version>" + version + "</version>"
					+ "</parent>"
					+ "<artifactId>" + ModuleReleaseManager.IDM_PREFIX + ROOT_MODULE_ID + "-impl</artifactId>"
					+ "<packaging>jar</packaging>" +
					"</project>");
			
			//
			releaseManager.setRepositoryRoot(repositoryRootFolder.getPath());
			releaseManager.gitInitRepository();
			releaseManager.gitCreateBranch("develop");
			releaseManager.init();
			releaseManager.gitSwitchBranch("develop");
			//
			return version;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}