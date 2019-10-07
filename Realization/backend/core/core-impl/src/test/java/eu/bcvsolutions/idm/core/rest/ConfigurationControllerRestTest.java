package eu.bcvsolutions.idm.core.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;

import eu.bcvsolutions.idm.core.api.rest.BaseController;
import eu.bcvsolutions.idm.test.api.AbstractRestTest;
import eu.bcvsolutions.idm.test.api.TestHelper;

/**
 * Configuration controller tests
 * 
 * @author Radek Tomiška
 *
 */
public class ConfigurationControllerRestTest extends AbstractRestTest {
	
	@Test
    public void readAllPublic() throws Exception {
        getMockMvc().perform(get(BaseController.BASE_PATH + "/public/configurations")
                .contentType(TestHelper.HAL_CONTENT_TYPE))
                .andExpect(status().isOk());
    }
}
