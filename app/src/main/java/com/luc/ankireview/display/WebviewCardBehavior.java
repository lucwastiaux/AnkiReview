package com.luc.ankireview.display;

import android.support.design.widget.CoordinatorLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.luc.ankireview.R;

public class WebviewCardBehavior extends CoordinatorLayout.Behavior<FrameLayout> {
    private static final String TAG = "WebviewCardBehavior";

    private static final float MIN_SCALE = 0.50f;
    private static final float MIN_ALPHA = 0.5f;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, FrameLayout child, View dependency) {
        // Log.v(TAG, "layoutDependsOn " + dependency.getClass().toString());
        if( dependency.getId() == R.id.answer_frame) {
            Log.v(TAG, "dependency on answer_frame");
            return true;
        }
        return false;
    }

    @Override
    public boolean onLayoutChild (CoordinatorLayout parent,
                                  FrameLayout child,
                                  int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);


        if( ! m_initialLayoutDone) {

            // center the question vertically
            if ( parent.getParent() instanceof WebviewFlashcardLayout) {

                int totalWindowHeight = parent.getHeight();
                int answerTargetY = (int) child.getY();

                // position the answer card outside of the screen

                FrameLayout answerFrame = parent.findViewById(R.id.answer_frame);
                answerFrame.setY(totalWindowHeight);

                // position the up arrow
                ImageView upArrow = parent.findViewById(R.id.arrow_up);
                Log.v(TAG, "upArrowHeight: " + upArrow.getHeight());
                upArrow.setY(totalWindowHeight - upArrow.getHeight() - 200);

                WebviewFlashcardLayout webviewFlashcardLayout = (WebviewFlashcardLayout) parent.getParent();
                webviewFlashcardLayout.setSpringAnimation(answerTargetY);

            }

            m_initialLayoutDone = true;

            return true;
        }


        return false;
    }

    @Override
    public boolean onDependentViewChanged (CoordinatorLayout parent,
                                           FrameLayout child,
                                           View dependency)
    {

        int totalWindowHeight = parent.getHeight();
        float currentY = dependency.getY();



        float position = 1 - currentY / totalWindowHeight;



        child.setY((currentY - totalWindowHeight) * 0.4f);

        double alpha = 1.0 + (currentY - totalWindowHeight)/totalWindowHeight;
        child.setAlpha((float)alpha);


        return true;

    }

    private boolean m_initialLayoutDone = false;

}