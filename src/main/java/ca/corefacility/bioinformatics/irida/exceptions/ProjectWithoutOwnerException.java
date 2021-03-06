package ca.corefacility.bioinformatics.irida.exceptions;

/**
 * Exception thrown if role change is not allowed
 * 
 *
 */
public class ProjectWithoutOwnerException extends Exception {

	private static final long serialVersionUID = -1057466626539625849L;

	public ProjectWithoutOwnerException(String message) {
		super(message);
	}
}
