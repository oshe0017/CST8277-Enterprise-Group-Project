@Path("courseregistration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseRegistrationResource {

    @EJB
    protected ACMECollegeService service;

    @Context
    protected SecurityContext sc;

    // list registrations – ADMIN sees all, USER sees own registrations
    @GET
    @RolesAllowed({ADMIN_ROLE, USER_ROLE})
    public Response getRegistrations() {
        if (sc.isCallerInRole(ADMIN_ROLE)) {
            // admin: all registrations
            List<CourseRegistration> list = em.createNamedQuery(
                    CourseRegistration.ALL_COURSEREG_QUERY,
                    CourseRegistration.class).getResultList();
            return Response.ok(list).build();
        } else {
            // user: only own
            WrappingCallerPrincipal wcp = (WrappingCallerPrincipal) sc.getCallerPrincipal();
            SecurityUser sUser = (SecurityUser) wcp.getWrapped();
            Student stu = sUser.getStudent();
            List<CourseRegistration> list = service.getRegistrationsForStudent(stu.getId());
            return Response.ok(list).build();
        }
    }

    // create registration  (POST body with embedded student & course or just ids)
    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response createRegistration(CourseRegistrationDTO dto,
                                       @Context UriInfo uriInfo) {
        CourseRegistration cr = service.createCourseRegistration(
            dto.getStudentId(), dto.getCourseId(), dto.getYear(), dto.getSemester());
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI url = ub.path("student/" + cr.getStudent().getId()
                          + "/course/" + cr.getCourse().getId()).build();
        return Response.created(url).entity(cr).build();
    }

    // assign professor
    @PUT
    @Path("student/{sid}/course/{cid}")
    @RolesAllowed({ADMIN_ROLE})
    public Response assignProfessor(@PathParam("sid") int sid,
                                    @PathParam("cid") int cid,
                                    Professor p) {
        CourseRegistration cr =
            service.assignProfessorToCourseRegistration(sid, cid, p.getId());
        return Response.ok(cr).build();
    }

    // assign grade  (text/plain body, matching Postman sample) :contentReference[oaicite:10]{index=10}
    @PUT
    @Path("student/{sid}/course/{cid}/grade")
    @Consumes(MediaType.TEXT_PLAIN)
    @RolesAllowed({ADMIN_ROLE})
    public Response assignGrade(@PathParam("sid") int sid,
                                @PathParam("cid") int cid,
                                String grade) {
        CourseRegistration cr =
            service.assignGradeToCourseRegistration(sid, cid, grade.trim());
        return Response.ok(cr).build();
    }

    @DELETE
    @Path("student/{sid}/course/{cid}")
    @RolesAllowed({ADMIN_ROLE})
    public void deleteRegistration(@PathParam("sid") int sid,
                                   @PathParam("cid") int cid) {
        service.deleteCourseRegistration(sid, cid);
    }
}
