package io.github.itfinally.http.parameters;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface FailureResponse {
    void responding( String content, HttpServletResponse response ) throws IOException;
}
