@Entity
@Table(name = "course")
@NamedQueries({
    @NamedQuery(name = Course.ALL_COURSES_QUERY,
        query = "SELECT DISTINCT c FROM Course c"),
    @NamedQuery(name = Course.SPECIFIC_COURSE_QUERY,
        query = "SELECT DISTINCT c FROM Course c WHERE c.id = :param1")
})
public class Course extends PojoBase {

    public static final String ALL_COURSES_QUERY = "Course.findAll";
    public static final String SPECIFIC_COURSE_QUERY = "Course.findById";

    @Column(name = "course_code", length = 7, nullable = false, unique = true)
    protected String courseCode;

    @Column(name = "course_title", length = 100, nullable = false)
    protected String courseTitle;

    @Column(name = "credit_units", nullable = false)
    protected Integer creditUnits;

    @Column(name = "online", nullable = false)
    protected Short online;              // 0 or 1

    @JsonIgnore
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    protected Set<CourseRegistration> courseRegistrations = new HashSet<>();

    @Transient
    protected boolean editable;

    // getters / setters
}
