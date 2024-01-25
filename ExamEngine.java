
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class ExamEngine implements ExamServer {
    private static int tokenCounter = 0;
    List<Integer> students;
    Map<Integer, String> studentPasswords;
    Map<Integer, List<Assessment>> studentAssessments;
    Map<Integer, Integer> studentTokens;
    Map<Integer, Date> tokensIssued;
    Map<Question, Integer> questionAnswers;

    // Constructor is required
    public ExamEngine() {
        super();

        // Initialize students
        students = new ArrayList<>(Arrays.asList(
            1234567,
            2345678,
            3456789,
            4567890
        ));

        // Initialize student passwords
        studentPasswords = new HashMap<>(Map.of(
            1234567, "password",
            2345678, "passwordz",
            3456789, "xxXpasswordXxx",
            4567890, "pass_word"
        ));

        studentTokens = new HashMap<>();
        tokensIssued = new HashMap<>();

        questionAnswers = new HashMap<>();
        studentAssessments = new HashMap<>();
        for (Integer studentID : students) {
            // Create Questions
            Question questionA = new QuestionImpl(0, "What colour is the sky?",
                    new String[] { "Blue", "Red", "Green" });
            questionAnswers.put(questionA, 0);
            Question questionB = new QuestionImpl(1, "What colour is the sea?",
                    new String[] { "Red", "Blue", "Green" });
            questionAnswers.put(questionB, 1);

            // Create Assessment with created questions
            List<Question> questions = new ArrayList<>(Arrays.asList(
                    questionA,
                    questionB));
            Date dueDate = new Date();
            AssessmentImpl assessment = new AssessmentImpl(studentID, "Assignment 1", "CT420", dueDate, questions);
            studentAssessments.put(studentID, Arrays.asList(assessment));
        }
    }

    // Implement the methods defined in the ExamServer interface...
    // Return an access token that allows access to the server for some time period
    public int login(int studentid, String password) throws UnauthorizedAccess, RemoteException {
        // Check that student exists
        if (!this.students.contains(studentid)) {
            throw new UnauthorizedAccess("Student ID" + studentid + "does not exist");
        }

        // Check that student has not been issued a token already
        if (this.studentTokens.containsKey(studentid)) {
            throw new UnauthorizedAccess("Student " + studentid + " is already logged in.");
        }

        // Check password
        if (this.studentPasswords.get(studentid).equals(password)) {
            // Generate and return token
            int newToken = tokenCounter++;
            this.studentTokens.put(studentid, newToken);
            this.tokensIssued.put(newToken, new Date());
            return newToken;
        } 

        throw new UnauthorizedAccess("Password for user " + studentid + " is incorrect");
    }

    // Return a summary list of Assessments currently available for this studentid
    public List<String> getAvailableSummary(int token, int studentid) throws UnauthorizedAccess, NoMatchingAssessment, RemoteException {
        // Check that student exists
        if (!this.students.contains(studentid)) {
            throw new UnauthorizedAccess("Student ID" + studentid + "does not exist");
        }
    
        // Check that student has been issued a token
        if (!this.studentTokens.containsKey(studentid)) {
            throw new UnauthorizedAccess("Student " + studentid + " is already logged in.");
        }

        // Check that student has available assessments
        if (!this.studentAssessments.containsKey(studentid)) {
            throw new NoMatchingAssessment("No matching assessments");
        }

        // Return assessment summary
        List<Assessment> assessments = this.studentAssessments.get(studentid);                    
        List<String> assessmentStrings = new ArrayList<>();
        for(Assessment assessment : assessments) {
            assessmentStrings.add(assessment.getInformation());
        }
        return assessmentStrings; 
    }

    // Return an Assessment object associated with a particular course code
    public Assessment getAssessment(int token, int studentid, String courseCode) throws UnauthorizedAccess, NoMatchingAssessment, RemoteException {
        // Check that the student exists
        if (!this.students.contains(studentid)) {
            throw new UnauthorizedAccess("Student " + studentid + "does not exist");
        }

        // Check that the student has previously been issued a login token
        if (!this.studentTokens.containsKey(studentid)) {
            throw new UnauthorizedAccess("Student is not logged in");
        }

        // Check that the student's login token is accurate to our records
        if (!this.studentTokens.get(studentid).equals(token)) {
            throw new UnauthorizedAccess("Student token does not match records");
        }

        // Check that the student has assessments
        if (!this.studentAssessments.containsKey(studentid)) {
            throw new NoMatchingAssessment("No assessments found for student " + studentid);
        }
        
        // Attempt to retrieve requested assessment
        for (Assessment assessment : this.studentAssessments.get(studentid)) {
            if (assessment.getInformation().contains(courseCode)) {
                return assessment;
            }
        }
        throw new NoMatchingAssessment("No assessment for course code: " + courseCode + " found");
    }

    // Submit a completed assessment
    public void submitAssessment(int token, int studentid, Assessment completed) throws UnauthorizedAccess, NoMatchingAssessment, RemoteException {
        // Check that the student exists
        if (!this.students.contains(studentid)) {
            throw new UnauthorizedAccess("Student " + studentid + "does not exist");
        }

        // Check that the student has previously been issued a login token
        if (!this.studentTokens.containsKey(studentid)) {
            throw new UnauthorizedAccess("Student is not logged in");
        }

        // Check that the student's login token is accurate to our records        
        if (!this.studentTokens.get(studentid).equals(token)) {
            throw new UnauthorizedAccess("Student token does not match records");
        }

        // Check that the student has assessments
        if (!this.studentAssessments.containsKey(studentid)) {
            throw new NoMatchingAssessment("Student has no assessments to submit");
        }

        // Write completed assessment to file
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(completed.getAssociatedID() + ".txt");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(completed);
            objectOutputStream.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
            String name = "ExamServer";
            ExamServer engine = new ExamEngine();
            ExamServer stub = (ExamServer) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("ExamEngine bound");
        } catch (Exception e) {
            System.err.println("ExamEngine exception:");
            e.printStackTrace();
        }
    }
}
