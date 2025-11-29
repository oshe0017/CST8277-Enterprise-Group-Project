@TestInstance(Lifecycle.PER_CLASS)
public class TestACMECollegeSystem {

    private static final String BASE_URI = "http://localhost:8080/REST-ACMECollege-Solution/api/v1/";

    private Client client;
    private WebTarget webTarget;

    private HttpAuthenticationFeature adminAuth;
    private HttpAuthenticationFeature userAuth;

    @BeforeAll
    public void oneTimeSetUp() {
        client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);

        adminAuth = HttpAuthenticationFeature.basic("admin", "admin");
        userAuth = HttpAuthenticationFeature.basic("cst8277", "8277");
    }

    @Test
    public void test01_get_all_students_with_adminrole() {
        Response response = webTarget
                .register(adminAuth)
                .path("student")
                .request()
                .get();
        assertThat(response.getStatus(), is(200));
        List<Student> students =
            response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
    }

    @Test
    public void test02_admin_can_create_course() {
        Course c = new Course();
        c.setCourseCode("CST9999");
        c.setCourseTitle("Test Course");
        c.setCreditUnits(3);
        c.setOnline((short)1);

        Response response = webTarget
                .register(adminAuth)
                .path("course")
                .request()
                .post(Entity.json(c));

        assertThat(response.getStatus(), is(201));
        Course created = response.readEntity(Course.class);
        assertThat(created.getId(), notNullValue());
    }

    @Test
    public void test03_user_cannot_create_course() {
        Course c = new Course();
        c.setCourseCode("CST8888");
        c.setCourseTitle("Bad Course");
        c.setCreditUnits(3);
        c.setOnline((short)0);

        Response response = webTarget
                .register(userAuth)
                .path("course")
                .request()
                .post(Entity.json(c));

        assertThat(response.getStatus(), is(403));  // forbidden
    }

    @Test
    public void test04_admin_can_create_course_registration() {
        CourseRegistrationDTO dto = new CourseRegistrationDTO();
        dto.setStudentId(1);
        dto.setCourseId(2);
        dto.setYear(2025);
        dto.setSemester("FALL");

        Response response = webTarget
                .register(adminAuth)
                .path("courseregistration")
                .request()
                .post(Entity.json(dto));

        assertThat(response.getStatus(), is(201));
    }

    @Test
    public void test05_user_can_see_own_registrations_only() {
        Response response = webTarget
                .register(userAuth)
                .path("courseregistration")
                .request()
                .get();

        assertThat(response.getStatus(), is(200));
        List<CourseRegistration> regs =
            response.readEntity(new GenericType<List<CourseRegistration>>(){});
        // add assertions on student id, etc.
    }

    // …and so on up to test20_xxx covering:
    // - CRUD for student clubs
    // - Assign professor success vs invalid ids
    // - Assign grade
    // - Delete operations (admin only)
    // - Negative tests for invalid roles / bad input
}
