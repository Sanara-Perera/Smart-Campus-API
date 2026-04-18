package com.smartcampus.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application Configuration.
 *
 * The @ApplicationPath("/api/v1") annotation marks this as the root of our API.
 * Everything registered here becomes available under /api/v1/...
 *
 * We extend ResourceConfig (Jersey's version of javax.ws.rs.core.Application)
 * which gives us the convenient packages() scanner — it automatically finds all
 * JAX-RS resources, filters, and exception mappers in the given package and
 * registers them. No need to list every class manually.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Automatically scan and register all classes in our package tree.
        // This picks up: @Path resources, @Provider exception mappers, @Provider filters.
        packages("com.smartcampus.api");

        // Register Jackson so JAX-RS can automatically convert Java objects to JSON
        // and parse incoming JSON request bodies into Java objects.
        register(JacksonFeature.class);

        // Give this application a display name (useful in logs)
        property("jersey.config.server.application.name", "SmartCampusAPI");
    }
}
