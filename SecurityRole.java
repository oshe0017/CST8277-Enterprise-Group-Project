@Entity
@Table(name = "security_role")
public class SecurityRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    protected int id;

    @Column(name = "name", length = 45, nullable = false, unique = true)
    protected String roleName;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")
    protected Set<SecurityUser> users = new HashSet<>();

    // getters / setters
}
