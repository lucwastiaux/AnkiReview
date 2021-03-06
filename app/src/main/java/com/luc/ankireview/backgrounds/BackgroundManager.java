package com.luc.ankireview.backgrounds;

import androidx.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.cloudinary.Transformation;
import com.cloudinary.Url;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Vector;


public class BackgroundManager {
    private static final String TAG = "BackgroundManager";

    public static final String TEACHERS = "teachers";
    public static final String BACKGROUNDS = "backgrounds";

    public static final String CROPMODE_SCALE = "imagga_scale";
    public static final String CROPMODE_CROP = "imagga_crop";
    public static final String CROPMODE_FILL = "fill";

    public enum BackgroundType {
        Teachers(TEACHERS, CROPMODE_SCALE, false),
        Backgrounds(BACKGROUNDS, CROPMODE_SCALE, true),
        BackgroundsFull(BACKGROUNDS, CROPMODE_FILL, false);

        BackgroundType(String setType, String cropMode, boolean applyBlur) {
            m_setType = setType;
            m_cropMode = cropMode;
            m_applyBlur = applyBlur;
        }

        public String getSetType() {
            return m_setType;
        }

        public String getCropMode() {
            return m_cropMode;
        }

        public boolean getApplyBlur() {
            return m_applyBlur;
        }

        private final String m_setType;
        private final String m_cropMode;
        private final boolean m_applyBlur;
    }


    public BackgroundManager(BackgroundType backgroundType, String setName, int changeImageEveryNumTicks) {
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        m_firestoreDb = FirebaseFirestore.getInstance();
        m_firestoreDb.setFirestoreSettings(settings);

        m_backgroundType = backgroundType;

        m_changeImageNumTicks = changeImageEveryNumTicks;

        m_imageView = null;
        m_imageUrlList = new Vector<String>();

        m_firestoreDb.collection(m_backgroundType.getSetType()).document(setName).collection("images")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                m_imageUrlList.add(document.getString("public_id"));
                            }
                            Collections.shuffle(m_imageUrlList);
                            // process backlog of "fillImageView"
                            if(m_imageView != null) {
                                fillImageViewComplete(m_imageView);
                            }
                            m_backgroundListReady = true;
                            Log.v(TAG, "retrieved " + m_imageUrlList.size() + " backgrounds");

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }


    private String getImage() {

        // get current URL
        m_currentBackgroundIndex++;
        if(m_currentBackgroundIndex > m_imageUrlList.size() - 1) {
            m_currentBackgroundIndex = 0;
        }
        String imgUrl = m_imageUrlList.get(m_currentBackgroundIndex);

        return imgUrl;
    }

    private void fillImageViewComplete(final ImageView imageView) {
        String imagePublicId = getImage();
        Url baseUrl = MediaManager.get().url().secure(true).transformation(new Transformation().quality("auto").fetchFormat("webp")).publicId(imagePublicId);

        if(m_backgroundType.getApplyBlur()) {
            baseUrl = MediaManager.get().url().secure(true).
                    transformation(new Transformation().quality("auto").fetchFormat("webp")).
                    transformation(new Transformation().effect("blur:200")).
                    publicId(imagePublicId);
        }

        Log.v(TAG, "fillImageViewComplete, baseUrl: " + baseUrl);

        MediaManager.get().responsiveUrl(true, true, m_backgroundType.getCropMode(), null)
                .stepSize(100)
                .minDimension(100)
                .maxDimension(2500)
                .generate(baseUrl, imageView, new ResponsiveUrl.Callback() {
                    @Override
                    public void onUrlReady(Url url) {
                        String finalUrl = url.generate();
                        Log.v(TAG, "final URL: " + finalUrl);
                        Picasso.get()
                                .load(finalUrl )
                                .placeholder(imageView.getDrawable())// still show last image
                                .into(imageView);
                    }
                });
    }

    public void fillImageView(final ImageView imageView)
    {
        Log.v(TAG, "fillImageView");

        if( m_backgroundListReady ) {
            // we have the backgrounds, go ahead
            fillImageViewComplete(imageView);
        } else {
            // need to queue up this action
            m_imageView = imageView;
        }

    }

    public void tick() {
        m_ticks += 1;
        if( m_ticks % m_changeImageNumTicks == 0) {
            fillImageView(m_imageView);
        }
    }


    private BackgroundType m_backgroundType;
    private boolean m_backgroundListReady = false;
    private Vector<String> m_imageUrlList;
    private int m_currentBackgroundIndex = 0;
    private FirebaseFirestore m_firestoreDb;
    private ImageView m_imageView;
    private int m_changeImageNumTicks = 3;
    private int m_ticks = 0;

}
