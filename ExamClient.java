import java.rmi.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ExamClient
{
	public static void main (String args[])
	{
		try {
			ExamServer exam = (ExamServer) Naming.lookup("//localhost/ExamServer");

			// Log into ExamServer and retrieve token
			int studentId = 2345678;
			String password = "passwordz";
			int token = exam.login(studentId, password);

			// Print summary of available assessments for student
			ArrayList<String> summaries = (ArrayList<String>)exam.getAvailableSummary(token, studentId);
			for(String summary : summaries) {
				System.out.println(summary);
			}

			// Get assessment for course code CT420
			Assessment assessment = exam.getAssessment(token, studentId, "CT420");

			// Prompt user to answer assessment questions
			Scanner scanner = new Scanner(System.in);
			for(Question question : assessment.getQuestions()) {
				System.out.println("Question " + question.getQuestionNumber() + ": " + question.getQuestionDetail());

				// Print questions options
				String[] options = question.getAnswerOptions();
				for(int i = 0; i < options.length; i++) {
					System.out.println(i + ": " + options[i]);
				}

				// Get user answer
				int answer;
				do {
					answer = scanner.nextInt();
					if (answer >= options.length || answer < 0) {
						System.out.println("Answer outside range [0," + (options.length-1) + "]");
					}
				} while (answer >= options.length || answer < 0);

				// Use input to select answer
				assessment.selectAnswer(question.getQuestionNumber(), answer);

				System.out.println("-------------------------");
			}
			scanner.close();

			// Submit assessment
			exam.submitAssessment(token, studentId, assessment);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		}
	}
