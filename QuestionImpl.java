public class QuestionImpl implements Question {

    private int questionNo;
    private String questionDetail;
    private String[] answerOptions;

    public QuestionImpl(int questionNo, String questionDetail, String[] answerOptions) {
        this.questionNo = questionNo;
        this.questionDetail = questionDetail;
        this.answerOptions = answerOptions;
    }

    @Override
    public int getQuestionNumber() {
        return this.questionNo;
    }

    @Override
    public String getQuestionDetail() {
        return this.questionDetail;
    }

    @Override
    public String[] getAnswerOptions() {
        return this.answerOptions;
    }
}
