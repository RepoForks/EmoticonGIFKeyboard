package com.kevalpatel2106.emoticongifkeyboard.internal.emoticons;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.kevalpatel2106.emoticongifkeyboard.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Keval on 21-Aug-17.
 *
 * @author <a href='https://github.com/kevalpatel2106'>Kevalpatel2106</a>
 */

class EmoticonLoader extends AsyncTask<Void, Void, Void> {
    @SuppressWarnings("unused")
    private static final String TAG = "EmoticonLoader";
    private final Context mContext;

    EmoticonLoader(@NonNull Context context) {
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            String emoticonData = readTextFile(R.raw.emoticons_list);

            if (emoticonData != null) {
                //Open the database connection
                EmoticonDbHelper helper = new EmoticonDbHelper(mContext);
                SQLiteDatabase sqLiteDatabase = helper.getWritableDatabase();
                JSONArray jsonArray = new JSONArray(emoticonData);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    if (!object.has("emoji") || !object.has("category")) continue;

                    JSONArray tagsJson = object.getJSONArray("tags");
                    String[] tags = null;
                    if (tagsJson.length() > 0) {
                        tags = new String[object.length()];
                        for (int j = 0; j < tagsJson.length(); j++) tags[j] = tagsJson.getString(j);
                    }

                    //Load to the database
                    helper.insertInDb(sqLiteDatabase,
                            object.getString("emoji"),
                            EmoticonsCategories.getCategoryFromCategoryName(object.getString("category")),
                            object.getString("description"),
                            tags);
                }

                //Close the database connection
                sqLiteDatabase.close();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    @Nullable
    private String readTextFile(int rowResource) {
        InputStream inputStream = mContext.getResources().openRawResource(rowResource); // getting json
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder builder = new StringBuilder();
        try {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                builder.append(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

}
