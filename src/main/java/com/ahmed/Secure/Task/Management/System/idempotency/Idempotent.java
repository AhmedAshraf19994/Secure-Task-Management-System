package com.ahmed.Secure.Task.Management.System.idempotency;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    String keyPrefix() default "";

    IdempotencyTtl timeToLive() default IdempotencyTtl.DEFAULT;

    // include the request body in the idempotency key
    boolean hashRequestBody() default false;

    //include the user id in the idempotency key
    boolean hashUserId() default true;

}
