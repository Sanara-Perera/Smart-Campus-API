package com.smartcampus.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Part 1 — Discovery Endpoint
 *
 * GET /api/v1/
 *
 * Returns API metadata: version info, admin contact, and hypermedia links
 * (HATEOAS) to the primary resource collections.
 *
 * HATEOAS (Hypermedia As The Engine Of Application State):
 * Instead of forcing clients to read static documentation to know that rooms
 * live at /api/v1/rooms, we embed those links directly in the response.
 * The client can discover the entire API structure from this one endpoint —
 * just like how a browser follows links on a webpage. This decouples the
 * client from hardcoded URL knowledge, so if we ever rename a path, clients
 * that follow links (rather than hardcode URLs) automatically adapt.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @Context
    private UriInfo uriInfo;

    /**
     * GET /api/v1/
     * Returns JSON metadata describing the API and its primary resource links.
     */
    @GET
    public Response discover() {

        String base = uriInfo.getBaseUri().toString();

        // Build the links map (HATEOAS navigation)
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms",   base + "rooms");
        links.put("sensors", base + "sensors");
        links.put("self",    base);

        // Build the full response body
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name",        "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0.0");
        response.put("description", "RESTful API for managing university campus rooms and IoT sensors");
        response.put("contact",     "admin@smartcampus.ac.uk");
        response.put("basePath",    "/api/v1");
        response.put("_links",      links);

        return Response.ok(response).build();
    }
    
}
