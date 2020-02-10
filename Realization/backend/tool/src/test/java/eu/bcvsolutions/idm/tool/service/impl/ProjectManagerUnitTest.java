package eu.bcvsolutions.idm.tool.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.bcvsolutions.idm.core.api.utils.ZipUtils;
import eu.bcvsolutions.idm.core.ecm.api.entity.AttachableEntity;
import eu.bcvsolutions.idm.test.api.AbstractUnitTest;

/**
 * Project build test:
 * - creates project mock structure in target.
 * 
 * @author Radek Tomiška
 *
 */
public class ProjectManagerUnitTest extends AbstractUnitTest {

	@Test
	public void testBuild() {
		createMockProjectStructure();
	}
	
	@BeforeClass
	public static void disableTestsOnDocumentation() {
		// generalize unit test, but it's integration test (MAVEN_HOME) is needed 
	    Boolean documentationOnly = Boolean.valueOf(System.getProperty("documentationOnly", "false"));
	    Assume.assumeFalse(documentationOnly);
	}
	
	private void createMockProjectStructure() {
		File targetFolder = new File("target");
		Assert.assertTrue(targetFolder.exists());
		//
		try {
			File projectRootFolder = new File(targetFolder, "mockProjectRoot");
			//
			if (projectRootFolder.exists()) {
				// prevent to delete whole target ... node_modules has to be presertved
				deleteFile(projectRootFolder, "dist");
				deleteFile(projectRootFolder, "frontend");
				deleteFile(projectRootFolder, "modules");
				deleteFile(projectRootFolder, "product");
				File internalTarget = new File(projectRootFolder, "target");
				if (internalTarget.exists()) {
					for (File child : internalTarget.listFiles()) {
						if (child.getName().equals("frontend")) {
							File feFiles = new File(String.format("%s/fe-sources", child.getPath()));
							if (feFiles.exists()) {
								for (File feChild : feFiles.listFiles()) {
									if (!feChild.getName().equals("node_modules")) {
										FileUtils.forceDelete(feChild);
									}
								}
							}
						} else { 
							FileUtils.forceDelete(child);
						}
					}
				}
			}
			//
			File productFolder = new File(projectRootFolder, "product");
			File modulesFolder = new File(projectRootFolder, "modules");
			File frontendFolder = new File(projectRootFolder, "frontend");
			//
			// mock product - copy necessary files for frontend build
			File warFolder = new File(productFolder, "war");
			File feSourcesFolder = new File(warFolder, "fe-sources");
			feSourcesFolder.mkdirs();
			File appPackage = new File(feSourcesFolder, "package.json");
			FileUtils.copyFile(new File("../../frontend/czechidm-app/package.json"), appPackage);
			FileUtils.copyFile(new File("../../frontend/czechidm-app/gulpfile.babel.js"), new File(feSourcesFolder, "gulpfile.babel.js"));
			FileUtils.copyFile(new File("../../frontend/czechidm-app/index.html"), new File(feSourcesFolder, "index.html"));
			FileUtils.copyDirectory(new File("../../frontend/czechidm-app/src"), new File(feSourcesFolder, "src"));
			FileUtils.copyDirectory(new File("../../frontend/czechidm-app/config"), new File(feSourcesFolder, "config"));
			FileUtils.copyDirectory(new File("../../frontend/czechidm-app/test"), new File(feSourcesFolder, "test"));
			File frontendModulesFolder = new File(feSourcesFolder, "czechidm-modules");
			File coreFrontendModuleFolder = new File(frontendModulesFolder, "czechidm-core");
			coreFrontendModuleFolder.mkdirs();
			File coreFrontendModulePackage = new File(coreFrontendModuleFolder, "package.json");
			FileUtils.copyFile(new File("../../frontend/czechidm-core/package.json"), coreFrontendModulePackage);
			FileUtils.copyFile(new File("../../frontend/czechidm-core/component-descriptor.js"), new File(coreFrontendModuleFolder, "component-descriptor.js"));
			FileUtils.copyFile(new File("../../frontend/czechidm-core/index.js"), new File(coreFrontendModuleFolder, "index.js"));
			FileUtils.copyFile(new File("../../frontend/czechidm-core/module-descriptor.js"), new File(coreFrontendModuleFolder, "module-descriptor.js"));
			FileUtils.copyFile(new File("../../frontend/czechidm-core/routes.js"), new File(coreFrontendModuleFolder, "routes.js"));
			FileUtils.copyDirectory(new File("../../frontend/czechidm-core/src"), new File(coreFrontendModuleFolder, "src"));
			FileUtils.copyDirectory(new File("../../frontend/czechidm-core/themes"), new File(coreFrontendModuleFolder, "themes"));
			File manifestFolder = new File(warFolder, "META-INF");
			manifestFolder.mkdirs();
			File productManifest = new File(manifestFolder, "MANIFEST.MF");
			createManifest(productManifest);
			ZipUtils.compress(warFolder, new File(productFolder, "idm-1.0.0-SNAPSHOT.war").getPath());
			FileUtils.forceDelete(warFolder);
			//
			// two mock modules - one with FE, two without
			// one
			File moduleOneFolder = new File(modulesFolder, "module-one");
			File moduleOneManifestFolder = new File(moduleOneFolder, "META-INF");
			moduleOneManifestFolder.mkdirs();
			File moduleOneManifest = new File(moduleOneManifestFolder, "MANIFEST.MF");
			createManifest(moduleOneManifest);
			File moduleOneFeSourcesFolder = new File(moduleOneFolder, "fe-sources");
			File moduleOneFrontendModulesFolder = new File(moduleOneFeSourcesFolder, "czechidm-modules");
			File moduleOneFrontendModuleFolder = new File(moduleOneFrontendModulesFolder, "czechidm-one");
			moduleOneFrontendModuleFolder.mkdirs();
			File moduleOnePackage = new File(moduleOneFrontendModuleFolder, "package.json");
			FileUtils.writeStringToFile(
					moduleOnePackage, 
					"{ \"version\" : \"1.0.0-snapshot\" }",
					AttachableEntity.DEFAULT_CHARSET
			);			
			ZipUtils.compress(moduleOneFolder, new File(modulesFolder, "module-one-1.0.0-SNAPSHOT.jar").getPath());
			FileUtils.forceDelete(moduleOneFolder);
			// two
			File moduleTwoFolder = new File(modulesFolder, "module-two");
			File moduleTwoManifestFolder = new File(moduleTwoFolder, "META-INF");
			moduleTwoManifestFolder.mkdirs();
			File moduleTwoManifest = new File(moduleTwoManifestFolder, "MANIFEST.MF");
			createManifest(moduleTwoManifest);
			ZipUtils.compress(moduleTwoFolder, new File(modulesFolder, "module-two-1.0.0-SNAPSHOT.jar").getPath());
			FileUtils.forceDelete(moduleTwoFolder);
			//
			// one third party library (~ mock .jar)
			FileUtils.writeStringToFile(
					new File(modulesFolder, "mock-lib.jar"), 
					"mock.jar content",
					AttachableEntity.DEFAULT_CHARSET
			);
			//
			// mock frontend files
			FileUtils.writeStringToFile(
					new File(frontendFolder, "mock.html"), 
					"<html>mock</html>",
					AttachableEntity.DEFAULT_CHARSET
			);
			//
			// build
			ProjectManager projectManager = new ProjectManager(); // MAVEN_HOME, ./npm installed from plugin
			projectManager.init();
			projectManager.build(projectRootFolder.getPath(), false); // prevent to clean all node_modules
			//
			// check distribution war
			ZipUtils.extract(new File(String.format("%s/dist/idm.war", projectRootFolder.getPath())), warFolder.getPath());
			// two FE modules in sources => installed
			Assert.assertTrue(new File(String.format("%s/fe-sources/czechidm-modules/czechidm-core", warFolder.getPath())).exists());
			Assert.assertTrue(new File(String.format("%s/fe-sources/czechidm-modules/czechidm-one", warFolder.getPath())).exists());
			Assert.assertTrue(new File(String.format("%s/fe-sources/czechidm-modules/czechidm-core/package.json", warFolder.getPath())).exists());
			Assert.assertTrue(new File(String.format("%s/fe-sources/czechidm-modules/czechidm-one/package.json", warFolder.getPath())).exists());
			// css
			Assert.assertTrue(new File(String.format("%s/css/main.css", warFolder.getPath())).exists());
			// three backend modules
			Assert.assertTrue(new File(String.format("%s/WEB-INF/lib/mock-lib.jar", warFolder.getPath())).exists());
			Assert.assertTrue(new File(String.format("%s/WEB-INF/lib/module-one-1.0.0-SNAPSHOT.jar", warFolder.getPath())).exists());
			Assert.assertTrue(new File(String.format("%s/WEB-INF/lib/module-two-1.0.0-SNAPSHOT.jar", warFolder.getPath())).exists());
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void createManifest(File manifestDestination) throws IOException {
		Manifest manifest = new Manifest();
        Attributes global = manifest.getMainAttributes();
        global.put(Attributes.Name.MANIFEST_VERSION, "1.0.0");
        global.put(Attributes.Name.IMPLEMENTATION_VERSION, "1.0.0-SNAPSHOT");
        try (OutputStream os = new FileOutputStream(manifestDestination)) {
        	manifest.write(os);
        }
	}
	
	private void deleteFile(File projectRootFolder, String filePath) throws IOException {
		File file = new File(String.format("%s/%s", projectRootFolder.getPath(), filePath));
		if (file.exists()) {
			FileUtils.forceDelete(file);
		}
	}
}
