package ca.corefacility.bioinformatics.irida.ria.integration.projects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import ca.corefacility.bioinformatics.irida.config.IridaApiPropertyPlaceholderConfig;
import ca.corefacility.bioinformatics.irida.config.data.IridaApiJdbcDataSourceConfig;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.LoginPage;
import ca.corefacility.bioinformatics.irida.ria.integration.pages.ProjectDetailsPage;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import com.github.springtestdbunit.annotation.DatabaseTearDown;
import com.google.common.collect.ImmutableList;

/**
 * <p>
 * Integration test to ensure that the Project Details Page.
 * </p>
 * 
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = { IridaApiJdbcDataSourceConfig.class,
		IridaApiPropertyPlaceholderConfig.class })
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class })
@ActiveProfiles("it")
@DatabaseSetup("/ca/corefacility/bioinformatics/irida/ria/web/ProjectsPageIT.xml")
@DatabaseTearDown("classpath:/ca/corefacility/bioinformatics/irida/test/integration/TableReset.xml")
public class ProjectDetailsPageIT {
	public static final Long PROJECT_ID = 1L;
	public static final String PROJECT_NAME = "project";
	public static final String PROJECT_OWNER = "Mr. Manager";
	public static final String PROJECT_CREATED_DATE = "12 Jul 2013";
	public static final String PROJECT_ORGANISM = "E. coli";
	public static final String ASSOCIATED_PROJECT_NO_RIGHTS = "project5";
	public static final int MODIFIED_DATE_DB_YEAR = 2013;

	public static final ImmutableList<String> ASSOCIATED_PROJECTS_WITH_RIGHTS = ImmutableList
			.of("project2", "project3");

	private WebDriver driver;
	private ProjectDetailsPage detailsPage;

	@Before
	public void setUp() {
		driver = new ChromeDriver();
		LoginPage loginPage = LoginPage.to(driver);
		loginPage.doLogin();

		detailsPage = new ProjectDetailsPage(driver, PROJECT_ID);
	}

	@After
	public void destroy() {
		if (driver != null) {
			driver.close();
            driver.quit();
		}
	}

	@Test
	public void hasCorrectMetaData() {
		assertEquals("Page should show correct title", PROJECT_NAME, detailsPage.getPageTitle());
		assertEquals("Should have the organism displayed", PROJECT_ORGANISM, detailsPage.getOrganism());
		assertEquals("Should have the correct date format for creation date", PROJECT_CREATED_DATE,
				detailsPage.getCreatedDate());
		assertEquals("Should have the correct date format for modified date", getFormattedModifiedDate(),
				detailsPage.getModifiedDate());
	}

	@Test
	public void hasTheCorrectAssociatedProjects() {
		List<String> projectsDiv = detailsPage.getAssociatedProjects();
		assertEquals("Has the correct number of associated projects", 3, projectsDiv.size());

		assertEquals("Has project with no rights", ASSOCIATED_PROJECT_NO_RIGHTS, detailsPage.getProjectWithNoRights());
		List<String> projectsWithRights = detailsPage.getProjectsWithRights();
		for (String project : ASSOCIATED_PROJECTS_WITH_RIGHTS) {
			assertTrue("Contains projects with authorization (" + project + ")", projectsWithRights.contains(project));
		}
	}

	/**
	 * The modified date for a project is expressed in time since modification.
	 * This will return the correct format for the displayed date.
	 * 
	 * @return the format for the modified year
	 */
	private String getFormattedModifiedDate() {
		// Need to format the expected modified date
		LocalDateTime time = LocalDateTime.now();
		int years = time.getYear() - MODIFIED_DATE_DB_YEAR;
		String modifiedDate;
		if (years == 1) {
			modifiedDate = "a year ago";
		} else {
			modifiedDate = years + "years ago";
		}
		return modifiedDate;
	}
}
