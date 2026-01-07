public class Student extends User {
    private final String universityName;
    private final String studentId;
    private final boolean verified;

    public Student(long userId, String name, String email,  //constructor for student class
                   String universityName, String studentId, boolean verified) {
        super(userId, name, email);

        if (universityName == null || universityName.isBlank()) {
            throw new IllegalArgumentException("universityName must not be blank.");    //validate inputs
        }
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("studentId must not be blank.");
        }

        this.universityName = universityName;
        this.studentId = studentId;
        this.verified = verified;
    }
    //getters

    public String getUniversityName() {
        return universityName;
    }

    public String getStudentId() {
        return studentId;
    }

    public boolean isVerified() {
        return verified;
    }
}