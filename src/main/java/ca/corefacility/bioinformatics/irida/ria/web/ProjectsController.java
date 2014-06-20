package ca.corefacility.bioinformatics.irida.ria.web;

import ca.corefacility.bioinformatics.irida.model.Project;
import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.ria.utilities.DataTable;
import ca.corefacility.bioinformatics.irida.ria.utilities.Formats;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.sample.SampleService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for all project related views
 *
 * @author Josh Adam <josh.adam@phac-aspc.gc.ca>
 */
@Controller
@RequestMapping(value = "/projects")
public class ProjectsController {
	public static final String PROJECTS_PAGE = "projects";
	public static final String SORT_BY_ID = "id";
	public static final String SORT_BY_NAME = "name";
	public static final String SORT_BY_CREATED_DATE = "createdDate";
	public static final String SORT_BY_MODIFIED_DATE = "modifiedDate";
	public static final String SORT_ASCENDING = "asc";
	public static final String COLUMN_PROJECT_ID = "0";
	public static final String COLUMN_NAME = "1";
	public static final String COLUMN_DATE_CREATED = "5";
	public static final String COLUMN_DATE_MODIFIED = "6";
	private ProjectService projectService;
	private SampleService sampleService;
	private UserService userService;

	@Autowired
	public ProjectsController(ProjectService projectService, SampleService sampleService, UserService userService) {
		this.projectService = projectService;
		this.sampleService = sampleService;
		this.userService = userService;
	}

	@RequestMapping(value = "")
	public String getProjectsPage() {
		return PROJECTS_PAGE;
	}

	@RequestMapping(value = "/ajax/list", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody
	Map<String, Object> getAjaxProjectList(final Principal principal, WebRequest request) {
		User user = userService.getUserByUsername(principal.getName());

		int start = Integer.parseInt(request.getParameter(DataTable.REQUEST_PARAM_START));
		int length = Integer.parseInt(request.getParameter(DataTable.REQUEST_PARAM_LENGTH));
		int draw = Integer.parseInt(request.getParameter(DataTable.REQUEST_PARAM_DRAW));
		String sortColumn = request.getParameter(DataTable.REQUEST_PARAM_SORT_COLUMN);
		String sortString;

		// If there is a sort by column set the correct name to sort by.
		// The default should be by date modified since that is what the
		// user would have most recently used.
		switch (sortColumn) {
			case COLUMN_PROJECT_ID:
				sortString = SORT_BY_ID;
				break;
			case COLUMN_NAME:
				sortString = SORT_BY_NAME;
				break;
			case COLUMN_DATE_CREATED:
				sortString = SORT_BY_CREATED_DATE;
				break;
			case COLUMN_DATE_MODIFIED:
				sortString = SORT_BY_MODIFIED_DATE;
				break;
			default:
				sortString = SORT_BY_MODIFIED_DATE;
		}
		Sort.Direction sortDirection = request.getParameter(DataTable.REQUEST_PARAM_SORT_DIRECTION).equals(SORT_ASCENDING) ? Sort.Direction.ASC : Sort.Direction.DESC;
		String searchValue = request.getParameter(DataTable.REQUEST_PARAM_SEARCH_VALUE);

		int pageNum = (int) Math.floor(start / length);
		Page<ProjectUserJoin> page = projectService.searchProjectsByNameForUser(user, searchValue, pageNum, length, sortDirection, sortString);
		List<ProjectUserJoin> projectList = page.getContent();

		Map<String, Object> map = new HashMap<>();
		map.put(DataTable.RESPONSE_PARAM_DRAW, draw);
		map.put(DataTable.RESPONSE_PARAM_RECORDS_TOTAL, page.getTotalElements());
		map.put(DataTable.RESPONSE_PARAM_RECORDS_FILTERED, page.getTotalElements());

		// Create the format required by DataTable
		List<List<String>> projectsData = new ArrayList<>();
		for (ProjectUserJoin projectUserJoin : projectList) {
			Project p = projectUserJoin.getSubject();
			ProjectRole role = projectUserJoin.getProjectRole();
			List<String> l = new ArrayList<>();
			l.add(p.getId().toString());
			l.add(p.getName());
			l.add(role.toString());
			l.add(String.valueOf(sampleService.getSamplesForProject(p).size()));
			l.add(String.valueOf(userService.getUsersForProject(p).size()));
			l.add(String.valueOf(Formats.DATE.format(p.getTimestamp())));
			l.add(String.valueOf(Formats.DATE.format(p.getModifiedDate())));
			projectsData.add(l);
		}
		map.put(DataTable.RESPONSE_PARAM_DATA, projectsData);
		return map;
	}
}
