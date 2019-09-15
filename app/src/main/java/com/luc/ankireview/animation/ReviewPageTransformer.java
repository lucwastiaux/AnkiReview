package com.luc.ankireview.animation;

import androidx.viewpager.widget.ViewPager;
import android.view.View;

import com.luc.ankireview.display.AnswerCard;
import com.luc.ankireview.display.QuestionCard;
import com.luc.ankireview.R;

public class ReviewPageTransformer implements ViewPager.PageTransformer  {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        QuestionCard questionCard = view.findViewById(R.id.question_card);
        AnswerCard answerCard = view.findViewById(R.id.answer_card);

        // questionCard.setTranslationX((float) (position * (pageWidth / 4.0)));
        answerCard.setTranslationX((float) (position * (pageWidth / 2.0)));

    }

}
