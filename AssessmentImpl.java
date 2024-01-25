import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssessmentImpl implements Assessment {
    private int studentID;
    private String assessmentTitle;
    private Date closingDate;
    private String courseCode;
    private List<Question> questions;
    private Map<Question, Integer> answers;


    public AssessmentImpl(int studentID, String assessmentTitle, String courseCode, Date closingDate, List<Question> questions) {
        this.studentID = studentID;
        this.assessmentTitle = assessmentTitle;
        this.closingDate = closingDate;
        this.courseCode = courseCode;
        this.questions = questions;
        this.answers = new HashMap<>();
    }

    @Override
    public String getInformation() {
        String output = "";
        output += "Course Code: " + this.courseCode + ", ";
        output += "Title: " + this.assessmentTitle + ", ";
        output += "Student ID: " + this.studentID + ", ";
        output += "Due Date: " + this.closingDate + ".";
        return output;
    }

    @Override
    public Date getClosingDate() {
        return this.closingDate;
    }

    @Override
    public List<Question> getQuestions() {
        return this.questions;
    }

    @Override
    public Question getQuestion(int questionNumber) throws InvalidQuestionNumber {
        for (Question question : this.questions) {
            if (question.getQuestionNumber() == questionNumber) {
                return question;
            }
        }

        throw new InvalidQuestionNumber();
    }

    @Override
    public void selectAnswer(int questionNumber, int optionNumber) throws InvalidQuestionNumber, InvalidOptionNumber {
        if (questionNumber > this.questions.size() || questionNumber < 0)
            throw new InvalidQuestionNumber();
        
        for (Question question : this.questions) {
            if (question.getQuestionNumber() == questionNumber) {
                if (optionNumber > question.getAnswerOptions().length  || optionNumber < 0)
                    throw new InvalidOptionNumber();
                answers.put(question, optionNumber);
                return;
            }
        }
    }

    @Override
    public int getSelectedAnswer() {
        throw new UnsupportedOperationException("Unimplemented method 'getSelectedAnswer'");
    }

    @Override
    public int getAssociatedID() {
        return this.studentID;
    }
}
