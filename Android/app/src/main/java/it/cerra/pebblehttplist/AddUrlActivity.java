package it.cerra.pebblehttplist;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddUrlActivity extends AppCompatActivity {

    static final String TAG = "AddUrlActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_url);

        final SharedPreferences sharedpreferences = getSharedPreferences("urls", Context.MODE_PRIVATE);
        final EditText name = (EditText)findViewById(R.id.edit_name);
        final EditText url = (EditText)findViewById(R.id.edit_url);

        Button enter = (Button)findViewById(R.id.buttonSave);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String strName = name.getText().toString();
                String strUrl = url.getText().toString();

                if(TextUtils.isEmpty(strName)) {
                    name.setError("Please enter a symbolic name");
                    return;
                }

                if(TextUtils.isEmpty(strUrl)) {
                    url.setError("Please enter a URL");
                    return;
                }

                //Log.d(TAG, "Save " + name.getText() + url.getText());

                String strJson = sharedpreferences.getString("json_array","[]");

                //Log.d(TAG, strJson);

                try {

                    JSONArray jsonArr = new JSONArray(strJson);

                    JSONObject pnObj = new JSONObject();
                    pnObj.put("name", strName);
                    pnObj.put("url", strUrl);

                    jsonArr.put(pnObj);

                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString("json_array", jsonArr.toString());
                    editor.apply();
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
