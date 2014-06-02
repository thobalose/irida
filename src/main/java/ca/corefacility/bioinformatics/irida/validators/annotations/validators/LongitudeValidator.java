package ca.corefacility.bioinformatics.irida.validators.annotations.validators;

import java.util.regex.Pattern;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import ca.corefacility.bioinformatics.irida.validators.annotations.Longitude;

/**
 * Validator for validating longitude portion of a geographic coordinate.
 * 
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 *
 */
public class LongitudeValidator implements ConstraintValidator<Longitude, String> {
	
	private static final Pattern LONGITUDE_PATTERN = Pattern.compile("^-?(\\d){3}(\\.(\\d){1,2})?$");

	private static final Float LONG_MIN = -180f;
	private static final Float LONG_MAX = 180f;

	@Override
	public void initialize(Longitude constraintAnnotation) {
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (LONGITUDE_PATTERN.matcher(value).matches()) {
			Float longitude = Float.valueOf(value);
			return longitude >= LONG_MIN && longitude <= LONG_MAX;
		}
		
		return false;
	}

}
