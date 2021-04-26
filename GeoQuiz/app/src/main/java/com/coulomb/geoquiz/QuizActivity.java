package com.coulomb.geoquiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class QuizActivity extends AppCompatActivity {

    private static final String TAG = "QuizActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_CORRECT = "correct";
    private static final String KEY_INCORRECT = "incorrect";
    private static final String KEY_ANSWERED = "answered";
    private static final String KEY_ANSWER_SHOWN = "answer_shown";
    private static final String KEY_CHEAT_COUNTS = "cheat_counts";
    private static final int REQUEST_CODE_CHEAT = 0;
    private static final int CHEAT_COUNTS = 3;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private Button mPrevButton;
    private Button mNextButton;
    private TextView mQuestionTextView;
    private TextView mTimesLeftTextView;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true)
    };

    private int mCurrentIndex = 0;
    private int mCorrectCount = 0;
    private int mIncorrectCount = 0;
    private int mCheatCounts = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Android系统调用了onCreate(Bundle)方法。");
        setContentView(R.layout.activity_quiz);

        if (savedInstanceState != null) {
            mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            mCorrectCount = savedInstanceState.getInt(KEY_CORRECT, 0);
            mIncorrectCount = savedInstanceState.getInt(KEY_INCORRECT, 0);
            mCheatCounts = savedInstanceState.getInt(KEY_CHEAT_COUNTS, 0);
            for (int i = 0; i < mQuestionBank.length; i++) {
                mQuestionBank[i].setAnswered(savedInstanceState.getBoolean(KEY_ANSWERED + "_" + i, false));
                mQuestionBank[i].setAnswerShown(savedInstanceState.getBoolean(KEY_ANSWER_SHOWN + "_" + i, false));
            }
            Log.d(TAG, "从已存实例状态中取出了当前题目序号" + mCurrentIndex + "。");
        }

        mQuestionTextView = findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            updateQuestion();
        });

        mTrueButton = findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(v -> {
//            Toast toast = Toast.makeText(QuizActivity.this, R.string.correct_toast, Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.TOP, Gravity.CENTER, Gravity.CENTER);
//            toast.show();
            checkAnswer(true);
        });

        mFalseButton = findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(v -> {
//            Toast toast = Toast.makeText(QuizActivity.this, R.string.incorrect_toast, Toast.LENGTH_SHORT);
//            toast.setGravity(Gravity.TOP, Gravity.CENTER, Gravity.CENTER);
//            toast.show();
            checkAnswer(false);
        });

        mTimesLeftTextView = findViewById(R.id.times_left_text_view);

        mCheatButton = findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(v -> {
            boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
//            Intent intent = new Intent(QuizActivity.this, CheatActivity.class);
            Intent intent = CheatActivity.newIntent(QuizActivity.this, answerIsTrue);
//            startActivity(intent);
            startActivityForResult(intent, REQUEST_CODE_CHEAT);
        });

        mPrevButton = findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(v -> {
            if (mCurrentIndex == 0) mCurrentIndex = mQuestionBank.length;
            mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
            updateQuestion();
        });

        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(v -> {
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
            updateQuestion();
        });

        updateQuestion();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Android系统调用了onStart()方法。");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Android系统调用了onResume()方法。");
        mTimesLeftTextView.setText("剩余" + (CHEAT_COUNTS - mCheatCounts) + "次作弊机会");
        if (mCheatCounts == CHEAT_COUNTS) mCheatButton.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Android系统调用了onPause()方法。");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Android系统调用了onSaveInstanceState(Bundle)方法，保存离开时的状态数据（这里就是当前题目序号" +
                mCurrentIndex + "）。");
        outState.putInt(KEY_INDEX, mCurrentIndex); //保存离开时的状态
        outState.putInt(KEY_CORRECT, mCorrectCount);
        outState.putInt(KEY_INCORRECT, mIncorrectCount);
        outState.putInt(KEY_CHEAT_COUNTS, mCheatCounts);
        //这里注意要保存每个Question（每道题）“答过题”和“作过弊”的状态，
        //因为QuestionBank里每道题的初始状态都是“没答过”和“没作过”，在Activity重建过程中未保存的状态都会被重置为初始值。
        for (int i = 0; i < mQuestionBank.length; i++) {
            outState.putBoolean(KEY_ANSWERED + "_" + i, mQuestionBank[i].isAnswered());
            outState.putBoolean(KEY_ANSWER_SHOWN + "_" + i, mQuestionBank[i].isAnswerShown());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Android系统调用了onStop()方法。");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Android系统调用了onDestroy()方法。");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            boolean answerShown = CheatActivity.wasAnswerShown(data);
            // 同一道题第一次作弊才计算作弊次数
            if (answerShown && !mQuestionBank[mCurrentIndex].isAnswerShown()) mCheatCounts++;

            mQuestionBank[mCurrentIndex].setAnswerShown(answerShown);
        }
    }

    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);
        if (mQuestionBank[mCurrentIndex].isAnswered()) {
            mTrueButton.setEnabled(false);
            mFalseButton.setEnabled(false);
        } else {
            mTrueButton.setEnabled(true);
            mFalseButton.setEnabled(true);
        }
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;
        if (userPressedTrue == answerIsTrue) {
            if (mQuestionBank[mCurrentIndex].isAnswerShown())
                messageResId = R.string.judgment_toast;
            else
                messageResId = R.string.correct_toast;
            mCorrectCount++;
        } else {
            messageResId = R.string.incorrect_toast;
            mIncorrectCount++;
        }
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();

        mQuestionBank[mCurrentIndex].setAnswered(true);
        mTrueButton.setEnabled(false);
        mFalseButton.setEnabled(false);

        if (mCorrectCount + mIncorrectCount == mQuestionBank.length) {
            Toast.makeText(QuizActivity.this,
                    String.format("答题完毕，得分%d%%.", Math.round((float) 100 * mCorrectCount / (mCorrectCount + mIncorrectCount))),
                    Toast.LENGTH_SHORT).show();
        }
    }
}