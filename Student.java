@Entity
@Table(name = "student")
@NamedQueries({
    @NamedQuery(name = Student.ALL_STUDENTS_QUERY,
        query = "SELECT DISTINCT s FROM Student s"),
    @NamedQuery(name = Student.SPECIFIC_STUDENT_QUERY,
        query = "SELECT DISTINCT s FROM Student s WHERE s.id = :param1")
})
public class Student extends PojoBase {

    public static final String ALL_STUDENTS_QUERY = "Student.findAll";
    public static final String SPECIFIC_STUDENT_QUERY = "Student.findById";

    @Column(name = "first_name", length = 50, nullable = false)
    protected String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    protected String lastName;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    protected String email;

    @Column(name = "phone", length = 10, nullable = false)
    protected String phone;

    @Column(name = "program", length = 45, nullable = false)
    protected String program;

    @JsonIgnore                    // avoid recursion
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    protected Set<CourseRegistration> courseRegistrations = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "studentMembers")
    protected Set<StudentClub> studentClubs = new HashSet<>();

    @JsonIgnore
    @OneToOne(mappedBy = "student")
    protected SecurityUser securityUser;

    @Transient
    protected boolean editable;

    // getters / setters
}
