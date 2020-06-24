package shamu.company.common.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import shamu.company.common.exception.errormapping.ForbiddenException;
import shamu.company.server.exception.FeignResourceNotFoundException;
import shamu.company.server.exception.FeignServerException;

public class FeignErrorDecoder implements ErrorDecoder {

  @Override
  public Exception decode(final String methodKey, final Response response) {

    switch (response.status()) {
      case 403:
        return new ForbiddenException(response.toString());
      case 404:
        return new FeignResourceNotFoundException(response.toString());
      default:
        return new FeignServerException("Feign Generic error:" + response.reason());
    }
  }
}
