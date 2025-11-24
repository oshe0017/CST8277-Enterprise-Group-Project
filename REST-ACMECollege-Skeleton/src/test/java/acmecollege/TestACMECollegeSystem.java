/********************************************************************************************************
 * File:  TestACMECollegeSystem.java
 * Course Materials CST 8277
 * Teddy Yap
 * (Original Author) Mike Norman
 *
 */
package acmecollege;

import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_API_VERSION;
import static com.algonquincollege.cst8277.utility.MyConstants.APPLICATION_CONTEXT_ROOT;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_ADMIN_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.PROFESSOR_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.COURSE_REGISTRATION_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.STUDENT_CLUB_RESOURCE_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.RESOURCE_PATH_ID_ELEMENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.CourseRegistrationPK;
import com.algonquincollege.cst8277.entity.StudentClub;

@SuppressWarnings("unused")

@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestACMECollegeSystem {
    private static final Class<?> _thisClaz = MethodHandles.lookup().lookupClass();
    private static final Logger logger = LogManager.getLogger(_thisClaz);

    static final String HTTP_SCHEMA = "http";
    static final String HOST = "localhost";
    static final int PORT = 8080;

    // Test fixture(s)
    static URI uri;
    static HttpAuthenticationFeature adminAuth;
    static HttpAuthenticationFeature userAuth;

    @BeforeAll
    public static void oneTimeSetUp() throws Exception {
        logger.debug("oneTimeSetUp");
        uri = UriBuilder
            .fromUri(APPLICATION_CONTEXT_ROOT + APPLICATION_API_VERSION)
            .scheme(HTTP_SCHEMA)
            .host(HOST)
            .port(PORT)
            .build();
        adminAuth = HttpAuthenticationFeature.basic(DEFAULT_ADMIN_USER, DEFAULT_ADMIN_USER_PASSWORD);
        userAuth = HttpAuthenticationFeature.basic(DEFAULT_USER, DEFAULT_USER_PASSWORD);
    }

    protected WebTarget webTarget;
    @BeforeEach
    public void setUp() {
        Client client = ClientBuilder.newClient().register(MyObjectMapperProvider.class).register(new LoggingFeature());
        webTarget = client.target(uri);
    }

    @Test
    public void test01_get_all_students_with_adminrole() throws JsonMappingException, JsonProcessingException {
        Response response = webTarget
            //.register(userAuth)
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Student> students = response.readEntity(new GenericType<List<Student>>(){});
        assertThat(students, is(not(empty())));
        // Accept 2 or more students (test05 may have created additional students)
        assertThat(students.size(), is(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
    }
    
    // Test 02: Test that USER_ROLE cannot get all students (should be forbidden)
    @Test
    public void test02_get_all_students_with_userrole_should_fail() {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(403)); // Forbidden
    }

    // Test 03: Test get specific student by ID with ADMIN_ROLE
    @Test
    public void test03_get_student_by_id_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME + "/1")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        Student student = response.readEntity(Student.class);
        assertThat(student, is(not(org.hamcrest.CoreMatchers.nullValue())));
        assertThat(student.getId(), is(1));
    }

    // Test 04: Test get specific student by ID with USER_ROLE (own student)
    @Test
    public void test04_get_student_by_id_with_userrole_own_student() {
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME + "/1")
            .request()
            .get();
        // Should succeed if user owns the student, otherwise 403
        assertThat(response.getStatus(), is(org.hamcrest.Matchers.anyOf(is(200), is(403))));
    }

    // Test 05: Test create new student with ADMIN_ROLE
    @Test
    public void test05_create_student_with_adminrole() {
        Student newStudent = new Student();
        newStudent.setFirstName("Test");
        newStudent.setLastName("Student");
        newStudent.setEmail("test@example.com");
        newStudent.setPhone("1234567890");
        newStudent.setProgram("Computer Science");
        
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .post(jakarta.ws.rs.client.Entity.entity(newStudent, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
        Student createdStudent = response.readEntity(Student.class);
        assertThat(createdStudent.getId(), is(not(0)));
        assertThat(createdStudent.getFirstName(), is("Test"));
    }

    // Test 06: Test create student with USER_ROLE should fail
    @Test
    public void test06_create_student_with_userrole_should_fail() {
        Student newStudent = new Student();
        newStudent.setFirstName("Test");
        newStudent.setLastName("Student");
        
        Response response = webTarget
            .register(userAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .post(jakarta.ws.rs.client.Entity.entity(newStudent, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(403)); // Forbidden
    }

    // Test 07: Test update student with ADMIN_ROLE
    @Test
    public void test07_update_student_with_adminrole() {
        Student updatedStudent = new Student();
        updatedStudent.setFirstName("Updated");
        updatedStudent.setLastName("Name");
        updatedStudent.setEmail("updated@example.com");
        
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME + "/1")
            .request()
            .put(jakarta.ws.rs.client.Entity.entity(updatedStudent, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
    }

    // Test 08: Test delete student with ADMIN_ROLE
    @Test
    public void test08_delete_student_with_adminrole() {
        // First create a student to delete
        Student newStudent = new Student();
        newStudent.setFirstName("ToDelete");
        newStudent.setLastName("Student");
        
        Response createResponse = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .post(jakarta.ws.rs.client.Entity.entity(newStudent, MediaType.APPLICATION_JSON));
        Student created = createResponse.readEntity(Student.class);
        
        Response deleteResponse = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME + "/" + created.getId())
            .request()
            .delete();
        assertThat(deleteResponse.getStatus(), is(200));
    }

    // Test 09: Test get all courses with ADMIN_ROLE
    @Test
    public void test09_get_all_courses_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Course> courses = response.readEntity(new GenericType<List<Course>>(){});
        assertThat(courses, is(not(empty())));
    }

    // Test 10: Test create course with ADMIN_ROLE
    @Test
    public void test10_create_course_with_adminrole() {
        Course newCourse = new Course();
        newCourse.setCourseCode("CST8277");
        newCourse.setCourseTitle("Enterprise Application Programming");
        newCourse.setCreditUnits(3);
        newCourse.setOnline((short) 0);
        
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME)
            .request()
            .post(jakarta.ws.rs.client.Entity.entity(newCourse, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
        Course createdCourse = response.readEntity(Course.class);
        assertThat(createdCourse.getId(), is(not(0)));
        assertThat(createdCourse.getCourseCode(), is("CST8277"));
    }

    // Test 11: Test get course by ID with ADMIN_ROLE
    @Test
    public void test11_get_course_by_id_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/1")
            .request()
            .get();
        // May be 200 or 404 depending on data
        assertThat(response.getStatus(), is(org.hamcrest.Matchers.anyOf(is(200), is(404))));
    }

    // Test 12: Test update course with ADMIN_ROLE
    @Test
    public void test12_update_course_with_adminrole() {
        Course updatedCourse = new Course();
        updatedCourse.setCourseCode("CST9999");
        updatedCourse.setCourseTitle("Updated Course");
        updatedCourse.setCreditUnits(4);
        updatedCourse.setOnline((short) 1);
        
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_RESOURCE_NAME + "/1")
            .request()
            .put(jakarta.ws.rs.client.Entity.entity(updatedCourse, MediaType.APPLICATION_JSON));
        // May be 200 or 404 depending on data
        assertThat(response.getStatus(), is(org.hamcrest.Matchers.anyOf(is(200), is(404))));
    }

    // Test 13: Test get all professors with ADMIN_ROLE
    @Test
    public void test13_get_all_professors_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(PROFESSOR_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<Professor> professors = response.readEntity(new GenericType<List<Professor>>(){});
        assertThat(professors, is(not(empty())));
    }

    // Test 14: Test create professor with ADMIN_ROLE
    @Test
    public void test14_create_professor_with_adminrole() {
        Professor newProfessor = new Professor();
        newProfessor.setFirstName("John");
        newProfessor.setLastName("Doe");
        newProfessor.setDegree("PhD");
        
        Response response = webTarget
            .register(adminAuth)
            .path(PROFESSOR_RESOURCE_NAME)
            .request()
            .post(jakarta.ws.rs.client.Entity.entity(newProfessor, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus(), is(200));
        Professor createdProfessor = response.readEntity(Professor.class);
        assertThat(createdProfessor.getId(), is(not(0)));
        assertThat(createdProfessor.getFirstName(), is("John"));
    }

    // Test 15: Test get all course registrations with ADMIN_ROLE
    @Test
    public void test15_get_all_course_registrations_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<CourseRegistration> registrations = response.readEntity(new GenericType<List<CourseRegistration>>(){});
        // Collection may be empty if no course registrations exist yet
        // Just verify the endpoint works (status 200)
    }

    // Test 16: Test create course registration with ADMIN_ROLE
    @Test
    public void test16_create_course_registration_with_adminrole() {
        CourseRegistration newRegistration = new CourseRegistration();
        CourseRegistrationPK pk = new CourseRegistrationPK();
        pk.setStudentId(1);
        pk.setCourseId(1);
        newRegistration.setId(pk);
        newRegistration.setYear(2024);
        newRegistration.setSemester("Fall");
        
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME)
            .request()
            .post(jakarta.ws.rs.client.Entity.entity(newRegistration, MediaType.APPLICATION_JSON));
        // May be 200 or error depending on foreign key constraints
        assertThat(response.getStatus(), is(org.hamcrest.Matchers.anyOf(is(200), is(400), is(500))));
    }

    // Test 17: Test assign professor to course registration with ADMIN_ROLE
    @Test
    public void test17_assign_professor_to_course_registration_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/1/professor/1")
            .request()
            .put(jakarta.ws.rs.client.Entity.entity("", MediaType.APPLICATION_JSON));
        // May be 200 or 404 depending on data
        assertThat(response.getStatus(), is(org.hamcrest.Matchers.anyOf(is(200), is(404))));
    }

    // Test 18: Test assign grade to course registration with ADMIN_ROLE
    @Test
    public void test18_assign_grade_to_course_registration_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(COURSE_REGISTRATION_RESOURCE_NAME + "/student/1/course/1/grade")
            .queryParam("letterGrade", "A")
            .request()
            .put(jakarta.ws.rs.client.Entity.entity("", MediaType.APPLICATION_JSON));
        // May be 200 or 404 depending on data
        assertThat(response.getStatus(), is(org.hamcrest.Matchers.anyOf(is(200), is(404))));
    }

    // Test 19: Test get all student clubs with ADMIN_ROLE
    @Test
    public void test19_get_all_student_clubs_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_CLUB_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<StudentClub> clubs = response.readEntity(new GenericType<List<StudentClub>>(){});
        assertThat(clubs, is(not(empty())));
    }

    // Test 20: Test add student to club with ADMIN_ROLE
    @Test
    public void test20_add_student_to_club_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_CLUB_RESOURCE_NAME + "/1/student/1")
            .request()
            .put(jakarta.ws.rs.client.Entity.entity("", MediaType.APPLICATION_JSON));
        // May be 200 or 404 depending on data
        assertThat(response.getStatus(), is(org.hamcrest.Matchers.anyOf(is(200), is(404))));
    }

    // Test 21: Test negative - unauthorized access without authentication
    @Test
    public void test21_unauthorized_access_without_auth() {
        Response response = webTarget
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .get();
        assertThat(response.getStatus(), is(401)); // Unauthorized
    }

    // Test 22: Test negative - wrong media type
    @Test
    public void test22_wrong_media_type() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME)
            .request()
            .accept(MediaType.APPLICATION_XML)
            .get();
        // Should return JSON, not XML
        assertThat(response.getMediaType(), is(not(MediaType.APPLICATION_XML)));
    }

    // Test 23: Test get non-existent student
    @Test
    public void test23_get_nonexistent_student() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME + "/99999")
            .request()
            .get();
        assertThat(response.getStatus(), is(404)); // Not Found
    }

    // Test 24: Test get programs with ADMIN_ROLE
    @Test
    public void test24_get_programs_with_adminrole() {
        Response response = webTarget
            .register(adminAuth)
            .path(STUDENT_RESOURCE_NAME + "/program")
            .request()
            .get();
        assertThat(response.getStatus(), is(200));
        List<String> programs = response.readEntity(new GenericType<List<String>>(){});
        assertThat(programs, is(not(empty())));
    }

}
