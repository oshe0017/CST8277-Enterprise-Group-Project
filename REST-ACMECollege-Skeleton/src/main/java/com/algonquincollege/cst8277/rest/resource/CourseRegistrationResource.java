/********************************************************************************************************
 * File:  CourseRegistrationResource.java
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;

import java.util.List;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.CourseRegistration;

@Path(COURSE_REGISTRATION_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseRegistrationResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getCourseRegistrations() {
        LOG.debug("retrieving all course registrations ...");
        List<CourseRegistration> courseRegistrations = service.getAllCourseRegistrations();
        Response response = Response.ok(courseRegistrations).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path("/student/{studentId}/course/{courseId}")
    public Response getCourseRegistrationById(@PathParam("studentId") int studentId, @PathParam("courseId") int courseId) {
        LOG.debug("try to retrieve specific course registration studentId=" + studentId + " courseId=" + courseId);
        CourseRegistration courseRegistration = service.getCourseRegistrationById(studentId, courseId);
        Response response = Response.status(courseRegistration == null ? Status.NOT_FOUND : Status.OK).entity(courseRegistration).build();
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourseRegistration(CourseRegistration newCourseRegistration) {
        Response response = null;
        CourseRegistration newCourseRegistrationWithIdTimestamps = service.persistCourseRegistration(newCourseRegistration);
        response = Response.ok(newCourseRegistrationWithIdTimestamps).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/student/{studentId}/course/{courseId}")
    public Response updateCourseRegistration(@PathParam("studentId") int studentId, @PathParam("courseId") int courseId, CourseRegistration courseRegistrationWithUpdates) {
        Response response = null;
        CourseRegistration updatedCourseRegistration = service.updateCourseRegistration(studentId, courseId, courseRegistrationWithUpdates);
        response = Response.ok(updatedCourseRegistration).build();
        return response;
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path("/student/{studentId}/course/{courseId}")
    public Response deleteCourseRegistrationById(@PathParam("studentId") int studentId, @PathParam("courseId") int courseId) {
        Response response = null;
        CourseRegistration courseRegistrationDeleted = service.deleteCourseRegistrationById(studentId, courseId);
        response = Response.ok(courseRegistrationDeleted).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/student/{studentId}/course/{courseId}/professor/{professorId}")
    public Response assignProfessor(@PathParam("studentId") int studentId, @PathParam("courseId") int courseId, @PathParam("professorId") int professorId) {
        LOG.debug("assigning professor " + professorId + " to course registration studentId=" + studentId + " courseId=" + courseId);
        CourseRegistration courseRegistration = service.assignProfessorToCourseRegistration(studentId, courseId, professorId);
        Response response = Response.ok(courseRegistration).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/student/{studentId}/course/{courseId}/grade")
    public Response assignGrade(@PathParam("studentId") int studentId, @PathParam("courseId") int courseId, @QueryParam("letterGrade") String letterGrade) {
        LOG.debug("assigning grade " + letterGrade + " to course registration studentId=" + studentId + " courseId=" + courseId);
        CourseRegistration courseRegistration = service.assignGradeToCourseRegistration(studentId, courseId, letterGrade);
        Response response = Response.ok(courseRegistration).build();
        return response;
    }
    
}

