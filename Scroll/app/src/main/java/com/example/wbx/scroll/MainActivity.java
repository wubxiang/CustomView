package com.example.wbx.scroll;

import android.app.Activity;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editText = findViewById(R.id.et_edit_pbook_poster_custom_content);
        editText.setImeActionLabel("return", EditorInfo.IME_ACTION_NEXT);
        editText.setHorizontallyScrolling(false);
        editText.setMaxLines(3);
    }
}
