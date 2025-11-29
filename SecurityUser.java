@Entity
@Table(name = "security_user")
public class SecurityUser implements Principal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    protected int id;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    protected String username;

    @Column(name = "password_hash", length = 256, nullable = false)
    protected String pwHash;

    // Task 3 – 1:1 mapping with Student :contentReference[oaicite:4]{index=4}
    @OneToOne
    @JoinColumn(name = "student_id")
    protected Student student;

    @ManyToMany
    @JoinTable(name = "user_has_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    protected Set<SecurityRole> roles = new HashSet<>();

    @Override
    public String getName() {
        return username;
    }

    // getters / setters
}
