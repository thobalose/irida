package ca.corefacility.bioinformatics.irida.service.analysis.execution.galaxy.phylogenonmics.impl.unit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import ca.corefacility.bioinformatics.irida.model.workflow.galaxy.GalaxyAnalysisId;
import ca.corefacility.bioinformatics.irida.model.workflow.galaxy.phylogenomics.RemoteWorkflowPhylogenomics;
import ca.corefacility.bioinformatics.irida.model.workflow.submission.galaxy.phylogenomics.AnalysisSubmissionPhylogenomics;
import ca.corefacility.bioinformatics.irida.exceptions.ExecutionManagerException;
import ca.corefacility.bioinformatics.irida.exceptions.WorkflowException;
import ca.corefacility.bioinformatics.irida.model.workflow.WorkflowState;
import ca.corefacility.bioinformatics.irida.model.workflow.WorkflowStatus;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyHistoriesService;
import ca.corefacility.bioinformatics.irida.pipeline.upload.galaxy.GalaxyWorkflowService;
import ca.corefacility.bioinformatics.irida.service.analysis.execution.galaxy.phylogenomics.impl.AnalysisExecutionServicePhylogenomics;
import ca.corefacility.bioinformatics.irida.service.analysis.workspace.galaxy.phylogenomics.impl.WorkspaceServicePhylogenomics;

public class AnalysisExecutionServicePhylogenomicsTest {
	
	@Mock private GalaxyHistoriesService galaxyHistoriesService;
	@Mock private GalaxyWorkflowService galaxyWorkflowService;
	@Mock private AnalysisSubmissionPhylogenomics analysisSubmission;
	@Mock private AnalysisSubmissionPhylogenomics submittedAnalysisGalaxy;
	@Mock private WorkspaceServicePhylogenomics galaxyWorkflowPreparationServicePhylogenomicsPipeline;

	private AnalysisExecutionServicePhylogenomics workflowManagement;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		workflowManagement = new AnalysisExecutionServicePhylogenomics(galaxyWorkflowService,
				galaxyHistoriesService, galaxyWorkflowPreparationServicePhylogenomicsPipeline);
		
		RemoteWorkflowPhylogenomics remoteWorkflow = 
				new RemoteWorkflowPhylogenomics("1", "1", "1", "1");
		
		when(submittedAnalysisGalaxy.getRemoteWorkflow()).thenReturn(remoteWorkflow);
		when(submittedAnalysisGalaxy.getRemoteAnalysisId()).thenReturn(new GalaxyAnalysisId("1"));
	}
	
	@Ignore
	@Test
	public void testExecuteAnalysisSuccess() throws ExecutionManagerException {
		workflowManagement.executeAnalysis(analysisSubmission);
	}
	
	/**
	 * Tests out successfully getting the status of a workflow.
	 * @throws ExecutionManagerException
	 */
	@Test
	public void testGetWorkflowStatusSuccess() throws ExecutionManagerException {
		WorkflowStatus workflowStatus = new WorkflowStatus(WorkflowState.OK, 1.0f);
		
		when(galaxyHistoriesService.getStatusForHistory(
				submittedAnalysisGalaxy.getRemoteAnalysisId().getValue())).thenReturn(workflowStatus);
		
		assertEquals(workflowStatus, workflowManagement.getWorkflowStatus(submittedAnalysisGalaxy));
	}
	
	/**
	 * Tests failure to get the status of a workflow (no status).
	 * @throws ExecutionManagerException
	 */
	@Test(expected=WorkflowException.class)
	public void testGetWorkflowStatusFailNoStatus() throws ExecutionManagerException {		
		when(galaxyHistoriesService.getStatusForHistory(submittedAnalysisGalaxy.
				getRemoteAnalysisId().getValue())).thenThrow(new WorkflowException());
		
		workflowManagement.getWorkflowStatus(submittedAnalysisGalaxy);
	}
}
