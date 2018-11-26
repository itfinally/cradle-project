package io.github.itfinally.http.parameters;

import java.lang.annotation.*;

@Documented
@Target( { ElementType.METHOD, ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
public @interface RequestVerify {

    // Provide verifier
    Class<? extends HttpArgumentsVerifier> value() default HttpArgumentsVerifier.class;

    // Verify by specify method of verifier
    String method() default "";
}
