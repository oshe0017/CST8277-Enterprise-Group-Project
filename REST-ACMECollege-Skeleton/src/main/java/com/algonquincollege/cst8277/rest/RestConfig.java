/********************************************************************************************************
 * File:  RestConfig.java
 * Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Mike Norman
 * 
 * Note:  Students do NOT need to change anything in this class.
 */
package com.algonquincollege.cst8277.rest;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_API_VERSION;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import com.algonquincollege.cst8277.rest.resource.StudentResource;
import com.algonquincollege.cst8277.rest.resource.CourseResource;
import com.algonquincollege.cst8277.rest.resource.ProfessorResource;
import com.algonquincollege.cst8277.rest.resource.CourseRegistrationResource;
import com.algonquincollege.cst8277.rest.resource.StudentClubResource;
// MyObjectMapperProvider is auto-discovered via @Provider annotation, no need to import
import com.algonquincollege.cst8277.rest.ClientErrorExceptionMapper;

@ApplicationPath(APPLICATION_API_VERSION)
//This used to be in web.xml
@DeclareRoles({USER_ROLE, ADMIN_ROLE})
public class RestConfig extends Application {

    /*
     * Without the following 'feature', the default serialization/deserialization for Jakarta EE 8
     * is JSON-B via a project called 'Yasson'.
     * 
     * However Yasson does not 'nicely' handle a variety of issues ... so substitute a non-standard
     * (but much more popular!) package called 'Jackson'
     */
    
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        // Register all REST resources explicitly
        classes.add(StudentResource.class);
        classes.add(CourseResource.class);
        classes.add(ProfessorResource.class);
        classes.add(CourseRegistrationResource.class);
        classes.add(StudentClubResource.class);
        // Register providers
        // Note: MyObjectMapperProvider has @Provider annotation and will be auto-discovered
        // Explicit registration causes classloader issues with JacksonJsonProvider
        // classes.add(MyObjectMapperProvider.class);
        classes.add(ClientErrorExceptionMapper.class);
        return classes;
    }
    
    // Alternative: Enable auto-discovery by returning empty Set
    // Uncomment below and comment out getClasses() above if explicit registration doesn't work
    /*
    @Override
    public Set<Class<?>> getClasses() {
        // Return empty Set to enable auto-discovery of @Path annotated classes
        return new HashSet<>();
    }
    */
    
    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jersey.config.jsonFeature", "JacksonFeature");
        // Add x-jersey-tracing tracing headers Note:  If more than 100 trace messages, need to alter configuration to allow for more
        //props.put("jersey.config.server.tracing.type", "ALL");
        //props.put("jersey.config.server.tracing.threshold", "VERBOSE");
        return props;
    }
}