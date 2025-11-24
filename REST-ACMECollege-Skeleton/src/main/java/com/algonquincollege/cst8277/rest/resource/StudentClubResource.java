/********************************************************************************************************
 * File:  StudentClubResource.java 
 */
package com.algonquincollege.cst8277.rest.resource;

import static com.algonquincollege.cst8277.utility.MyConstants.ADMIN_ROLE;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_PATH;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_CLUB_RESOURCE_NAME;

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
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.ejb.ACMECollegeService;
import com.algonquincollege.cst8277.entity.StudentClub;

@Path(STUDENT_CLUB_RESOURCE_NAME)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class StudentClubResource {

    private static final Logger LOG = LogManager.getLogger();

    @EJB
    protected ACMECollegeService service;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getStudentClubs() {
        LOG.debug("retrieving all student clubs ...");
        List<StudentClub> studentClubs = service.getAllStudentClubs();
        Response response = Response.ok(studentClubs).build();
        return response;
    }

    @GET
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response getStudentClubById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        LOG.debug("try to retrieve specific student club " + id);
        StudentClub studentClub = service.getStudentClubById(id);
        Response response = Response.status(studentClub == null ? Status.NOT_FOUND : Status.OK).entity(studentClub).build();
        return response;
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addStudentClub(StudentClub newStudentClub) {
        LOG.debug("=== DEBUG: addStudentClub START ===");
        LOG.debug("DEBUG: Received StudentClub - Type: {}, Name: {}, Desc: {}", 
            newStudentClub.getClass().getSimpleName(), newStudentClub.getName(), newStudentClub.getDesc());
        LOG.debug("DEBUG: isAcademic field value: {}", newStudentClub.getAcademic());
        
        Response response = null;
        try {
            StudentClub newStudentClubWithIdTimestamps = service.persistStudentClub(newStudentClub);
            LOG.debug("DEBUG: persistStudentClub SUCCESS - ID: {}, Name: {}", 
                newStudentClubWithIdTimestamps.getId(), newStudentClubWithIdTimestamps.getName());
            response = Response.ok(newStudentClubWithIdTimestamps).build();
        } catch (Exception e) {
            LOG.error("DEBUG: Exception in addStudentClub: {}", e.getMessage(), e);
            LOG.error("DEBUG: Exception type: {}", e.getClass().getName());
            if (e.getCause() != null) {
                LOG.error("DEBUG: Caused by: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            throw e;
        }
        LOG.debug("=== DEBUG: addStudentClub END ===");
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response updateStudentClubById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id, StudentClub studentClubWithUpdates) {
        Response response = null;
        StudentClub updatedStudentClub = service.updateStudentClubById(id, studentClubWithUpdates);
        response = Response.ok(updatedStudentClub).build();
        return response;
    }
    
    @DELETE
    @RolesAllowed({ADMIN_ROLE})
    @Path(RESOURCE_PATH_ID_PATH)
    public Response deleteStudentClubById(@PathParam(RESOURCE_PATH_ID_ELEMENT) int id) {
        Response response = null;
        StudentClub studentClubDeleted = service.deleteStudentClubById(id);
        response = Response.ok(studentClubDeleted).build();
        return response;
    }

    @PUT
    @RolesAllowed({ADMIN_ROLE})
    @Path("/{clubId}/student/{studentId}")
    public Response addStudentToClub(@PathParam("clubId") int clubId, @PathParam("studentId") int studentId) {
        LOG.debug("adding student " + studentId + " to club " + clubId);
        StudentClub studentClub = service.addStudentToClub(studentId, clubId);
        Response response = Response.ok(studentClub).build();
        return response;
    }
    
}

