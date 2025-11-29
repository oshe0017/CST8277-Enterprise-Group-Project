@Path("course")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CourseResource {

    @EJB
    protected ACMECollegeService service;

    @GET
    @RolesAllowed({ADMIN_ROLE})
    public Response getCourses() {
        List<Course> list = service.getAllCourses();
        return Response.ok(list).build();
    }

    @GET
    @Path("{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response getCourseById(@PathParam("id") int id) {
        Course c = service.getCourseById(id);
        return Response.status(c == null ? Status.NOT_FOUND : Status.OK)
                       .entity(c).build();
    }

    @POST
    @RolesAllowed({ADMIN_ROLE})
    public Response addCourse(Course c, @Context UriInfo uriInfo) {
        Course newC = service.persistCourse(c);
        UriBuilder ub = uriInfo.getAbsolutePathBuilder();
        URI uri = ub.path(Integer.toString(newC.getId())).build();
        return Response.created(uri).entity(newC).build();
    }

    @PUT
    @Path("{id}")
    @RolesAllowed({ADMIN_ROLE})
    public Response updateCourse(@PathParam("id") int id, Course detached) {
        Course updated = service.updateCourse(id, detached);
        if (updated == null) {
            throw new NotFoundException();
        }
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed({ADMIN_ROLE})
    public void deleteCourse(@PathParam("id") int id) {
        service.deleteCourse(id);
    }
}
