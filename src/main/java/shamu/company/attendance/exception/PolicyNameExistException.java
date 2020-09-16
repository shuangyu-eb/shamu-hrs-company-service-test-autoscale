package shamu.company.attendance.exception;

import me.alidg.errors.annotation.ExceptionMapping;
import me.alidg.errors.annotation.ExposeAsArg;
import org.springframework.http.HttpStatus;

/**
 * @author mshumaker
 */

@ExceptionMapping(statusCode = HttpStatus.BAD_REQUEST, errorCode = "duplicate_name")
public class PolicyNameExistException extends RuntimeException {
    @ExposeAsArg(0)
    private final String name;

    private static final long serialVersionUID = 48797873421319940L;

    public PolicyNameExistException(final String message, final String name) {
        super(message);
        this.name = name;

    }
}

