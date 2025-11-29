@Singleton
public class CustomIdentityStoreJPAHelper {

    private static final Logger LOG = LogManager.getLogger();

    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;

    public SecurityUser findUserByName(String username) {
        LOG.debug("find a User by the name = {}", username);
        TypedQuery<SecurityUser> q = em.createQuery(
            "SELECT DISTINCT u FROM SecurityUser u "
          + "LEFT JOIN FETCH u.roles "
          + "LEFT JOIN FETCH u.student "
          + "WHERE u.username = :username",
            SecurityUser.class);
        q.setParameter("username", username);
        List<SecurityUser> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public Set<String> getRoles(SecurityUser user) {
        return user.getRoles().stream()
                .map(SecurityRole::getRoleName)
                .collect(Collectors.toSet());
    }
}
