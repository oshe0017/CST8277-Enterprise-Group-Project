@Entity
@Table(name = "professor")
@NamedQueries({
    @NamedQuery(name = Professor.ALL_PROFESSORS_QUERY,
        query = "SELECT DISTINCT p FROM Professor p"),
    @NamedQuery(name = Professor.SPECIFIC_PROFESSOR_QUERY,
        query = "SELECT DISTINCT p FROM Professor p WHERE p.id = :param1")
})
public class Professor extends PojoBase {

    public static final String ALL_PROFESSORS_QUERY = "Professor.findAll";
    public static final String SPECIFIC_PROFESSOR_QUERY = "Professor.findById";

    @Column(name = "first_name", length = 50, nullable = false)
    protected String firstName;

    @Column(name = "last_name", length = 50, nullable = false)
    protected String lastName;

    @Column(name = "degree", length = 45)
    protected String degree;

    @JsonIgnore
    @OneToMany(mappedBy = "professor")
    protected Set<CourseRegistration> courseRegistrations = new HashSet<>();

    @Transient
    protected boolean editable;

    // getters / setters
}
