@Entity
@Table(name = "student_club")
@NamedQueries({
    @NamedQuery(name = StudentClub.ALL_STUDENT_CLUBS_QUERY,
        query = "SELECT DISTINCT sc FROM StudentClub sc LEFT JOIN FETCH sc.studentMembers"),
    @NamedQuery(name = StudentClub.SPECIFIC_STUDENT_CLUB_QUERY,
        query = "SELECT DISTINCT sc FROM StudentClub sc LEFT JOIN FETCH sc.studentMembers WHERE sc.id = :param1")
})
public class StudentClub extends PojoBase {

    public static final String ALL_STUDENT_CLUBS_QUERY = "StudentClub.findAll";
    public static final String SPECIFIC_STUDENT_CLUB_QUERY = "StudentClub.findById";

    @Column(name = "name", length = 100, nullable = false)
    protected String name;

    @Column(name = "description", length = 100)
    protected String desc;

    @Column(name = "academic", nullable = false)
    protected boolean isAcademic;

    @JsonSerialize(using = StudentMemberCountSerializer.class)
    @ManyToMany
    @JoinTable(name = "club_membership",
        joinColumns = @JoinColumn(name = "club_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id"))
    protected Set<Student> studentMembers = new HashSet<>();

    @Transient
    protected boolean editable;

    // getters / setters
}
