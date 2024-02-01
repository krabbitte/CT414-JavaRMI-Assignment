import java.rmi.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ExamClient
{
	public static void main (String args[])
	{
		try {
			/*
				Valid Student ID and Password Combinations
				StudentID	Password
			 	1234567		password
            	2345678		passwordz
            	3456789		xxXpasswordXxx
            	4567890		pass_word

				Valid Course Code for assessments: CT420
			*/

			ExamServer exam = (ExamServer) Naming.lookup("//localhost/ExamServer");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			// Get user's student ID
			System.out.println("Enter your student ID:");
			int studentId = 0;
			Boolean continueInput = true;
			do {
				String input = br.readLine();
				// Check for correct input format
				try {
					studentId = Integer.parseInt(input);
					continueInput = false;
				} catch (NumberFormatException exception) {
					System.out.println("Incorrect password format");
				}
			} while (continueInput);

			// Get user's password
			System.out.println("Enter your password:");
			String password = br.readLine();

			// Log into ExamServer and retrieve token using inputted studentID and password
			int token = exam.login(studentId, password);

			// Print summary of available assessments for student
			ArrayList<String> summaries = (ArrayList<String>)exam.getAvailableSummary(token, studentId);
			for(String summary : summaries) {
				System.out.println(summary);
			}

			// Get assessment
			continueInput = true;
			Assessment assessment = null;
			do {
				System.out.println("Enter course code:");
				String courseCode = br.readLine();
				try {
					assessment = exam.getAssessment(token, studentId, courseCode);
					continueInput = false;
				} catch (Exception exception) {
					System.out.println(exception.getMessage());
				}
			} while(continueInput);

			// Prompt user to answer assessment questions
			for(Question question : assessment.getQuestions()) {
				System.out.println("Question " + question.getQuestionNumber() + ": " + question.getQuestionDetail());
				// Print questions options
				String[] options = question.getAnswerOptions();
				for(int i = 0; i < options.length; i++) {
					System.out.println(i + ": " + options[i]);
				}
				// Get user answer
				int answer = -1;
				continueInput = true;
				do {
					String input = br.readLine();
					// Check for correct input format
					try {
						answer = Integer.parseInt(input);
					} catch (NumberFormatException exception) {
						System.out.println("Incorrect input format");
						continue;
					}
					// Check answer for correct range
					if (answer < options.length && answer >= 0) {
						continueInput = false;
					} else {
						System.out.println("Answer outside range [0," + (options.length-1) + "]");
					}
				} while (continueInput);
				// Use input to select answer
				assessment.selectAnswer(question.getQuestionNumber(), answer);
				System.out.println("-------------------------");
			}
			br.close();
			// Submit assessment
			exam.submitAssessment(token, studentId, assessment);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		}
	}
