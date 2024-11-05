package com.example.tsiisware;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class InformationActivity extends AppCompatActivity {
    String label = null;
    String category = null;
    //TODO: The label of the object should be send to this activity as EXTRA INFORMATION. And it needs to get the category from the user. (quiz, text/video, video)
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            label = getIntent().getStringExtra("label");
            category = getIntent().getStringExtra("category");
            switch (category) {
                case "Quiz":
                    setContentView(R.layout.activity_main_informationview_quiz);
                    break;
                case "Text + Video":
                    setContentView(R.layout.activity_main_informationview_text_video);
                    break;
                case "Video":
                    setContentView(R.layout.activity_main_informationview_video);
                    break;
                default:
                    setContentView(R.layout.ar_view);
            }
        }

}
