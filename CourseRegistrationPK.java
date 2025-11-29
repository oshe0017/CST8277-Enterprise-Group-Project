@Embeddable
public class CourseRegistrationPK implements Serializable {

    @Column(name = "student_id")
    protected int studentId;

    @Column(name = "course_id")
    protected int courseId;

    // equals / hashCode
}
