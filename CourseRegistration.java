@Entity
@Table(name = "course_registration")
@NamedQueries({
    @NamedQuery(name = CourseRegistration.ALL_COURSEREG_QUERY,
        query = "SELECT DISTINCT cr FROM CourseRegistration cr "
              + "LEFT JOIN FETCH cr.student "
              + "LEFT JOIN FETCH cr.course "
              + "LEFT JOIN FETCH cr.professor"),
    @NamedQuery(name = CourseRegistration.SPECIFIC_COURSEREG_QUERY,
        query = "SELECT DISTINCT cr FROM CourseRegistration cr "
              + "LEFT JOIN FETCH cr.student "
              + "LEFT JOIN FETCH cr.course "
              + "LEFT JOIN FETCH cr.professor "
              + "WHERE cr.id.studentId = :studentId AND cr.id.courseId = :courseId")
})
public class CourseRegistration extends PojoBaseCompositeKey<CourseRegistrationPK> {

    public static final String ALL_COURSEREG_QUERY = "CourseRegistration.findAll";
    public static final String SPECIFIC_COURSEREG_QUERY = "CourseRegistration.findByPk";

    @EmbeddedId
    protected CourseRegistrationPK id;

    @ManyToOne(optional = false)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    protected Student student;

    @ManyToOne(optional = false)
    @MapsId("courseId")
    @JoinColumn(name = "course_id")
    protected Course course;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    protected Professor professor;

    @Column(name = "year", nullable = false)
    protected int year;

    @Column(name = "semester", length = 6, nullable = false)
    protected String semester;     // FALL, WINTER, etc.

    @Column(name = "letter_grade", length = 3)
    protected String letterGrade;

    // getters / setters
}
