/********************************************************************************************************
 * File:  ACMECollegeService.java Course Materials CST 8277
 *
 * @author Teddy Yap
 * @author Shariar (Shawn) Emami
 * 
 */
package com.algonquincollege.cst8277.ejb;

import static com.algonquincollege.cst8277.entity.Student.ALL_STUDENTS_QUERY_NAME;
import static com.algonquincollege.cst8277.entity.Course.ALL_COURSES_QUERY;
import static com.algonquincollege.cst8277.entity.Professor.ALL_PROFESSORS_QUERY;
import static com.algonquincollege.cst8277.entity.CourseRegistration.ALL_COURSE_REGISTRATIONS_QUERY_NAME;
import static com.algonquincollege.cst8277.entity.StudentClub.ALL_STUDENT_CLUBS_QUERY;
import static com.algonquincollege.cst8277.entity.SecurityRole.SECURITY_ROLE_BY_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PASSWORD;
import static com.algonquincollege.cst8277.utility.MyConstants.DEFAULT_USER_PREFIX;
import static com.algonquincollege.cst8277.utility.MyConstants.PARAM1;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ALGORITHM;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_ITERATIONS;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_KEY_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PROPERTY_SALT_SIZE;
import static com.algonquincollege.cst8277.utility.MyConstants.PU_NAME;
import static com.algonquincollege.cst8277.utility.MyConstants.USER_ROLE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ejb.Singleton;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.security.enterprise.identitystore.Pbkdf2PasswordHash;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.algonquincollege.cst8277.entity.Course;
import com.algonquincollege.cst8277.entity.CourseRegistration;
import com.algonquincollege.cst8277.entity.CourseRegistrationPK;
import com.algonquincollege.cst8277.entity.Professor;
import com.algonquincollege.cst8277.entity.SecurityRole;
import com.algonquincollege.cst8277.entity.SecurityUser;
import com.algonquincollege.cst8277.entity.Student;
import com.algonquincollege.cst8277.entity.StudentClub;

@SuppressWarnings("unused")

/**
 * Stateless Singleton EJB Bean - ACMECollegeService
 */
@Singleton
public class ACMECollegeService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String READ_ALL_PROGRAMS = "SELECT name FROM program";
    //TODO ACMECS01 - Add your query constants here.
    
    @PersistenceContext(name = PU_NAME)
    protected EntityManager em;
    
    @Inject
    protected Pbkdf2PasswordHash pbAndjPasswordHash;

    public List<Student> getAllStudents() {
        return em.createNamedQuery(ALL_STUDENTS_QUERY_NAME, Student.class)
            .getResultList();
    }

    public Student getStudentById(int id) {
        return em.find(Student.class, id);
    }

    @Transactional
    public Student persistStudent(Student newStudent) {
        em.persist(newStudent);
        return newStudent;
    }

    @Transactional
    public void buildUserForNewStudent(Student newStudent) {
        SecurityUser userForNewStudent = new SecurityUser();
        userForNewStudent.setUsername(
            DEFAULT_USER_PREFIX + "_" + newStudent.getFirstName() + "." + newStudent.getLastName());
        Map<String, String> pbAndjProperties = new HashMap<>();
        pbAndjProperties.put(PROPERTY_ALGORITHM, DEFAULT_PROPERTY_ALGORITHM);
        pbAndjProperties.put(PROPERTY_ITERATIONS, DEFAULT_PROPERTY_ITERATIONS);
        pbAndjProperties.put(PROPERTY_SALT_SIZE, DEFAULT_SALT_SIZE);
        pbAndjProperties.put(PROPERTY_KEY_SIZE, DEFAULT_KEY_SIZE);
        pbAndjPasswordHash.initialize(pbAndjProperties);
        String pwHash = pbAndjPasswordHash.generate(DEFAULT_USER_PASSWORD.toCharArray());
        userForNewStudent.setPwHash(pwHash);
        userForNewStudent.setStudent(newStudent);
        TypedQuery<SecurityRole> roleQuery = em.createNamedQuery(SECURITY_ROLE_BY_NAME, SecurityRole.class);
        roleQuery.setParameter(PARAM1, USER_ROLE);
        SecurityRole userRole = roleQuery.getSingleResult();
        userForNewStudent.getRoles().add(userRole);
        userRole.getUsers().add(userForNewStudent);
        em.persist(userForNewStudent);
    }

    /**
     * To update a student
     * 
     * @param id - id of entity to update
     * @param studentWithUpdates - entity with updated information
     * @return Entity with updated information
     */
    @Transactional
    public Student updateStudentById(int id, Student studentWithUpdates) {
    	Student studentToBeUpdated = getStudentById(id);
        if (studentToBeUpdated != null) {
            em.refresh(studentToBeUpdated);
            studentWithUpdates.setId(id);
            Student mergedStudent = em.merge(studentWithUpdates);
            em.flush();
            return mergedStudent;
        }
        return null;
    }

    /**
     * To delete a student by id
     * 
     * @param id - student id to delete
     */
    @Transactional
    public Student deleteStudentById(int id) {
        Student student = getStudentById(id);
        if (student != null) {
            em.refresh(student);
            TypedQuery<SecurityUser> findUser = em.createNamedQuery(SecurityUser.SECURITY_USER_BY_STUDENT_ID, SecurityUser.class);
            findUser.setParameter(PARAM1, id);
            SecurityUser sUser = null;
            try {
                sUser = findUser.getSingleResult();
            } catch (jakarta.persistence.NoResultException e) {
                LOG.debug("No SecurityUser found for student id = {}", id);
            }
            if (sUser != null) {
                em.remove(sUser);
            }
            em.remove(student);
        }
        return student;
    }
    
	@SuppressWarnings("unchecked")
    public List<String> getAllPrograms() {
		List<String> programs = new ArrayList<>();
		try {
			programs = (List<String>) em.createNativeQuery(READ_ALL_PROGRAMS).getResultList();
		}
		catch (Exception e) {
		}
		return programs;
    }

	// Course CRUD methods
	public List<Course> getAllCourses() {
		TypedQuery<Course> query = em.createNamedQuery(ALL_COURSES_QUERY, Course.class);
		return query.getResultList();
	}

	public Course getCourseById(int id) {
		return em.find(Course.class, id);
	}

	@Transactional
	public Course persistCourse(Course newCourse) {
		em.persist(newCourse);
		return newCourse;
	}

	@Transactional
	public Course updateCourseById(int id, Course courseWithUpdates) {
		Course courseToBeUpdated = getCourseById(id);
		if (courseToBeUpdated != null) {
			em.refresh(courseToBeUpdated);
			courseWithUpdates.setId(id);
			Course mergedCourse = em.merge(courseWithUpdates);
			em.flush();
			return mergedCourse;
		}
		return null;
	}

	@Transactional
	public Course deleteCourseById(int id) {
		Course course = getCourseById(id);
		if (course != null) {
			em.refresh(course);
			em.remove(course);
		}
		return course;
	}

	// Professor CRUD methods
	public List<Professor> getAllProfessors() {
		TypedQuery<Professor> query = em.createNamedQuery(ALL_PROFESSORS_QUERY, Professor.class);
		return query.getResultList();
	}

	public Professor getProfessorById(int id) {
		return em.find(Professor.class, id);
	}

	@Transactional
	public Professor persistProfessor(Professor newProfessor) {
		em.persist(newProfessor);
		return newProfessor;
	}

	@Transactional
	public Professor updateProfessorById(int id, Professor professorWithUpdates) {
		Professor professorToBeUpdated = getProfessorById(id);
		if (professorToBeUpdated != null) {
			em.refresh(professorToBeUpdated);
			professorWithUpdates.setId(id);
			Professor mergedProfessor = em.merge(professorWithUpdates);
			em.flush();
			return mergedProfessor;
		}
		return null;
	}

	@Transactional
	public Professor deleteProfessorById(int id) {
		Professor professor = getProfessorById(id);
		if (professor != null) {
			em.refresh(professor);
			em.remove(professor);
		}
		return professor;
	}

	// CourseRegistration CRUD methods
	public List<CourseRegistration> getAllCourseRegistrations() {
		TypedQuery<CourseRegistration> query = em.createNamedQuery(ALL_COURSE_REGISTRATIONS_QUERY_NAME, CourseRegistration.class);
		return query.getResultList();
	}

	public CourseRegistration getCourseRegistrationById(int studentId, int courseId) {
		CourseRegistrationPK pk = new CourseRegistrationPK(studentId, courseId);
		return em.find(CourseRegistration.class, pk);
	}

	@Transactional
	public CourseRegistration persistCourseRegistration(CourseRegistration newCourseRegistration) {
		em.persist(newCourseRegistration);
		return newCourseRegistration;
	}

	@Transactional
	public CourseRegistration updateCourseRegistration(int studentId, int courseId, CourseRegistration courseRegistrationWithUpdates) {
		CourseRegistration courseRegistrationToBeUpdated = getCourseRegistrationById(studentId, courseId);
		if (courseRegistrationToBeUpdated != null) {
			em.refresh(courseRegistrationToBeUpdated);
			courseRegistrationWithUpdates.getId().setStudentId(studentId);
			courseRegistrationWithUpdates.getId().setCourseId(courseId);
			em.merge(courseRegistrationWithUpdates);
			em.flush();
		}
		return courseRegistrationWithUpdates;
	}

	@Transactional
	public CourseRegistration deleteCourseRegistrationById(int studentId, int courseId) {
		CourseRegistration courseRegistration = getCourseRegistrationById(studentId, courseId);
		if (courseRegistration != null) {
			em.refresh(courseRegistration);
			em.remove(courseRegistration);
		}
		return courseRegistration;
	}

	// StudentClub CRUD methods
	public List<StudentClub> getAllStudentClubs() {
		TypedQuery<StudentClub> query = em.createNamedQuery(ALL_STUDENT_CLUBS_QUERY, StudentClub.class);
		return query.getResultList();
	}

	public StudentClub getStudentClubById(int id) {
		return em.find(StudentClub.class, id);
	}

	@Transactional
	public StudentClub persistStudentClub(StudentClub newStudentClub) {
		LOG.debug("=== DEBUG: persistStudentClub START ===");
		LOG.debug("DEBUG: StudentClub type: {}", newStudentClub.getClass().getSimpleName());
		LOG.debug("DEBUG: StudentClub - Name: {}, Desc: {}, isAcademic: {}", 
			newStudentClub.getName(), newStudentClub.getDesc(), newStudentClub.getAcademic());
		
		try {
			LOG.debug("DEBUG: Calling em.persist()...");
			em.persist(newStudentClub);
			LOG.debug("DEBUG: em.persist() completed successfully");
			LOG.debug("DEBUG: Generated ID: {}", newStudentClub.getId());
		} catch (Exception e) {
			LOG.error("DEBUG: Exception in persistStudentClub: {}", e.getMessage(), e);
			LOG.error("DEBUG: Exception type: {}", e.getClass().getName());
			if (e.getCause() != null) {
				LOG.error("DEBUG: Caused by: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
			}
			throw e;
		}
		LOG.debug("=== DEBUG: persistStudentClub END ===");
		return newStudentClub;
	}

	@Transactional
	public StudentClub updateStudentClubById(int id, StudentClub studentClubWithUpdates) {
		StudentClub studentClubToBeUpdated = getStudentClubById(id);
		if (studentClubToBeUpdated != null) {
			em.refresh(studentClubToBeUpdated);
			studentClubWithUpdates.setId(id);
			em.merge(studentClubWithUpdates);
			em.flush();
		}
		return studentClubWithUpdates;
	}

	@Transactional
	public StudentClub deleteStudentClubById(int id) {
		StudentClub studentClub = getStudentClubById(id);
		if (studentClub != null) {
			em.refresh(studentClub);
			em.remove(studentClub);
		}
		return studentClub;
	}

	// Helper methods for associations
	@Transactional
	public CourseRegistration assignProfessorToCourseRegistration(int studentId, int courseId, int professorId) {
		CourseRegistration courseRegistration = getCourseRegistrationById(studentId, courseId);
		if (courseRegistration != null) {
			Professor professor = getProfessorById(professorId);
			if (professor != null) {
				courseRegistration.setProfessor(professor);
				em.merge(courseRegistration);
				em.flush();
			}
		}
		return courseRegistration;
	}

	@Transactional
	public CourseRegistration assignGradeToCourseRegistration(int studentId, int courseId, String letterGrade) {
		CourseRegistration courseRegistration = getCourseRegistrationById(studentId, courseId);
		if (courseRegistration != null) {
			courseRegistration.setLetterGrade(letterGrade);
			em.merge(courseRegistration);
			em.flush();
		}
		return courseRegistration;
	}

	@Transactional
	public StudentClub addStudentToClub(int studentId, int clubId) {
		Student student = getStudentById(studentId);
		StudentClub club = getStudentClubById(clubId);
		if (student != null && club != null) {
			club.getStudentMembers().add(student);
			student.getStudentClubs().add(club);
			em.merge(club);
			em.merge(student);
			em.flush();
		}
		return club;
	}
	
}