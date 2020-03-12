package org.hopto.dklis.zenbo_qa_service;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.SpeakConfig;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

// public class qa_answer extends AppCompatActivity {
public class qa_answer extends RobotActivity {
    // scope......
    static {
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLInputFactory",
                "com.fasterxml.aalto.stax.InputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLOutputFactory",
                "com.fasterxml.aalto.stax.OutputFactoryImpl"
        );
        System.setProperty(
                "org.apache.poi.javax.xml.stream.XMLEventFactory",
                "com.fasterxml.aalto.stax.EventFactoryImpl"
        );
    }

    /**
     * @param answer_strings , (String Array) ListView Items 資訊
     */
    private String[] answer_strings;
    private int font_size;
    private float scale;

    /**
     * @param lang     , (String)  string for language
     * @param country  , (String)  string for country
     * @param title    , (String)  string for activity title
     * @param category , (Integer) Integer for category
     * @param question , (Integer) Integer for question
     */
    private String lang    = "zh";
    private String country = "TW";
    private String title   = "";
    private int category   = 0;
    private int question   = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_answer);

        this.get_Intent(false);
        this.Views_Initial(false);
        this.View_Control(false);
    }

    /**
     * 取得 activity 傳遞的參數
     *  @param toast_disp , (boolean) 使否顯示 toast 資訊 debug 用
     */
    private void get_Intent(boolean toast_disp) {
        Intent intent = getIntent();

        /**
         * get item information
         */
        try {
            title = intent.getStringExtra("item_name");
            if (title == null) {
                title = "";
            }
        } catch (Exception e){
            Log.e("LocaleHelp - item_name", "error "+ e.toString());
            if (toast_disp) {
                Toast.makeText(getBaseContext(), "item_name error " + e.toString(), Toast.LENGTH_LONG).show();
            }
            title = "";
        }

        /**
         * get language information
         */
        try {
            lang = intent.getStringExtra("lang");
            if (lang == null) {
                lang = "zh";
            }
        } catch (Exception e){
            Log.e("LocaleHelp - lang", "error "+ e.toString());
            if (toast_disp) {
                Toast.makeText(getBaseContext(), "lang error " + e.toString(), Toast.LENGTH_LONG).show();
            }
            lang = "zh";
        }

        /**
         * get country information
         */
        try {
            country = intent.getStringExtra("country");
            if (country == null) {
                country = "zh";
            }
        } catch (Exception e){
            Log.e("LocaleHelp - country", "error "+ e.toString());
            if (toast_disp) {
                Toast.makeText(getBaseContext(), "country error " + e.toString(), Toast.LENGTH_LONG).show();
            }
            country = "TW";
        }

        /**
         * 變更語言選項
         */
        Locale locale = new Locale(lang, country);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

        /**
         * get category information
         */
        try {
            category = intent.getIntExtra("category", 0);
        } catch (Exception e){
            Log.e("LocaleHelp - category", "error "+ e.toString());
            if (toast_disp) {
                Toast.makeText(getBaseContext(), "category error " + e.toString(), Toast.LENGTH_LONG).show();
            }
            category = 0;
        }

        /**
         * get question information
         */
        try {
            question = intent.getIntExtra("question", 0);
        } catch (Exception e){
            Log.e("LocaleHelp - question", "error "+ e.toString());
            if (toast_disp) {
                Toast.makeText(getBaseContext(), "question error " + e.toString(), Toast.LENGTH_LONG).show();
            }
            question = 0;
        }
    }

    /**
     * 初始化 activiry_main.xml layout 上元件資訊
     * @param toast_disp , (boolean) 使否顯示 toast 資訊 debug 用
     */
    @SuppressLint("ResourceType")
    private void Views_Initial(boolean toast_disp) {


        /**
         * 取得畫面元件後指定給欄位變數。
         */

        /**
         * 抓取 android 系統 Scaled Density 資訊
         */
        this.scale = getResources().getDisplayMetrics().scaledDensity;

        // 設定 Activity 的標題
        TextView textview = (TextView)findViewById(R.id.qa_answer_title);
        textview.setText(title);

        /**
         *  從 resource values 讀取 dimens.xml 資料 , 將 dimens sp 數值轉換 pixels
         *  scale : Scaled Density , 實際的 font pixel size 就是從 dimen 讀到的值 除以 Scaled Density
         *  參考資料 :
         *  https://stackoverflow.com/questions/13600802/android-convert-dp-to-float
         */
        this.font_size = (int)(getResources().getDimensionPixelSize(R.dimen.activity_items) / this.scale);

        /**
         * 顯示 Toast 資訊 --> debug 使用
         */
        if (toast_disp) {
            Toast.makeText(getBaseContext(), "font size :" + this.font_size, Toast.LENGTH_LONG).show();
        }

        /**
         * 設定 listview 的資訊
         * (1) 將 resource 的 locale_items array 資訊放入 listviewitems
         * (2) 將 resource 的 locale_icons array 資訊放入 listviewicons
         * (3) 將 listView 的 text alignment 設為 center
         */
        TextView txtView = (TextView) findViewById(R.id.qa_answer_items);
        txtView.setTextSize(font_size);
        // txtView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtView.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        txtView.setTextColor(Color.RED);

        switch (category) {
            case 0: {
                switch (question) {
                    case 0: {
                        answer_strings = getResources().getStringArray(R.array.opentime_items);
                    }  break;
                    case 1: {
                        answer_strings = getResources().getStringArray(R.array.printcopy_items);
                    }  break;
                }
            }  break;
            case 1: {
                switch (question) {
                    case 0: {
                        answer_strings = getResources().getStringArray(R.array.etd_thesis_items);
                    }  break;
                    case 1: {
                        answer_strings = getResources().getStringArray(R.array.journal_items);
                    }  break;
                    case 2: {
                        answer_strings = getResources().getStringArray(R.array.conference_items);
                    }  break;
                }
            }  break;
        }

        String tmp_text = "";
        try {

            for (int i = 1; i < answer_strings.length; i += 1) {
                // tmp_text += (answer_strings[i] + "\n");
                tmp_text += (answer_strings[i]);
                robotAPI.robot.speak(answer_strings[i], new SpeakConfig().timeout(1));
            }


        } catch (Exception e){

        }

        txtView.setText(tmp_text);


    }

    /**
     * 宣告或建立 UI object 需要的監聽物件，並執行所有需要的註冊工作。
     * @param toast_disp , (boolean) 使否顯示 toast 資訊 debug 用
     */
    private void View_Control(boolean toast_disp) {
        TextView txtView = (TextView) findViewById(R.id.qa_answer_items);
        txtView.setOnClickListener(onClickView);
    }

    /**
     * 點擊ListView事件Method
     */
    private View.OnClickListener onClickView = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (answer_strings[0] == "" || answer_strings[0] == null) {

            } else {
                try {
                    Intent intent;
                    Uri uri;

                    uri = Uri.parse(answer_strings[0]);
                    intent = new Intent(Intent.ACTION_VIEW, uri);

                    startActivity(intent);
                } catch (Exception e) {

                }
            }

        }
    };

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
            Log.d("RobotDevSample", "onResult:"
                    + RobotCommand.getRobotCommand(cmd).name()
                    + ", serial:" + serial + ", err_code:" + err_code
                    + ", result:" + result.getString("RESULT"));
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };

    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {
            Log.d("RobotDevSample", "speak Complete");
        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {
        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public qa_answer() {
        super(robotCallback, robotListenCallback);
    }
}
