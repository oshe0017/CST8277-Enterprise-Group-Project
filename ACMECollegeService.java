@Stateless
public class ACMECollegeService {

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;

    // ---------- Course ----------

    public List<Course> getAllCourses() {
        return em.createNamedQuery(Course.ALL_COURSES_QUERY, Course.class)
                 .getResultList();
    }

    public Course getCourseById(int id) {
        return em.find(Course.class, id);
    }

    public Course persistCourse(Course c) {
        em.persist(c);
        return c;
    }

    public Course updateCourse(int id, Course detached) {
        Course c = getCourseById(id);
        if (c == null) return null;
        c.setCourseCode(detached.getCourseCode());
        c.setCourseTitle(detached.getCourseTitle());
        c.setCreditUnits(detached.getCreditUnits());
        c.setOnline(detached.getOnline());
        return em.merge(c);
    }

    public void deleteCourse(int id) {
        Course c = getCourseById(id);
        if (c != null) em.remove(c);
    }

    // ---------- Professor ----------

    // same pattern: getAllProfessors, getProfessorById, persistProfessor, updateProfessor, deleteProfessor

    // ---------- StudentClub ----------

    public List<StudentClub> getAllStudentClubs() {
        return em.createNamedQuery(StudentClub.ALL_STUDENT_CLUBS_QUERY, StudentClub.class)
                 .getResultList();
    }

    public StudentClub getStudentClubById(int id) {
        return em.createNamedQuery(StudentClub.SPECIFIC_STUDENT_CLUB_QUERY, StudentClub.class)
                 .setParameter("param1", id)
                 .getSingleResult();
    }

    public StudentClub persistStudentClub(StudentClub sc) {
        em.persist(sc);
        return sc;
    }

    public StudentClub updateStudentClub(int id, StudentClub detached) {
        StudentClub sc = em.find(StudentClub.class, id);
        if (sc == null) return null;
        sc.setName(detached.getName());
        sc.setDesc(detached.getDesc());
        sc.setAcademic(detached.isAcademic());
        return em.merge(sc);
    }

    public void deleteStudentClub(int id) {
        StudentClub sc = em.find(StudentClub.class, id);
        if (sc != null) em.remove(sc);
    }

    // ---------- CourseRegistration operations ----------

    public CourseRegistration createCourseRegistration(int studentId, int courseId,
                                                       int year, String semester) {
        Student s = em.find(Student.class, studentId);
        Course c = em.find(Course.class, courseId);
        if (s == null || c == null) {
            throw new IllegalArgumentException("Invalid student or course");
        }

        CourseRegistrationPK pk = new CourseRegistrationPK();
        pk.setStudentId(studentId);
        pk.setCourseId(courseId);

        CourseRegistration cr = new CourseRegistration();
        cr.setId(pk);
        cr.setStudent(s);
        cr.setCourse(c);
        cr.setYear(year);
        cr.setSemester(semester);

        em.persist(cr);
        return cr;
    }

    public CourseRegistration assignProfessorToCourseRegistration(int studentId,
                                                                  int courseId,
                                                                  int professorId) {
        CourseRegistration cr = getCourseRegistration(studentId, courseId);
        Professor p = em.find(Professor.class, professorId);
        cr.setProfessor(p);
        return em.merge(cr);
    }

    public CourseRegistration assignGradeToCourseRegistration(int studentId,
                                                              int courseId,
                                                              String letterGrade) {
        CourseRegistration cr = getCourseRegistration(studentId, courseId);
        cr.setLetterGrade(letterGrade);
        return em.merge(cr);
    }

    public CourseRegistration getCourseRegistration(int studentId, int courseId) {
        CourseRegistrationPK pk = new CourseRegistrationPK();
        pk.setStudentId(studentId);
        pk.setCourseId(courseId);
        return em.find(CourseRegistration.class, pk);
    }

    public List<CourseRegistration> getRegistrationsForStudent(int studentId) {
        return em.createQuery(
                "SELECT cr FROM CourseRegistration cr "
              + "LEFT JOIN FETCH cr.course "
              + "LEFT JOIN FETCH cr.professor "
              + "WHERE cr.student.id = :sid",
                CourseRegistration.class)
                 .setParameter("sid", studentId)
                 .getResultList();
    }

    public void deleteCourseRegistration(int studentId, int courseId) {
        CourseRegistration cr = getCourseRegistration(studentId, courseId);
        if (cr != null) em.remove(cr);
    }

    // etc.
}
