package org.hopto.dklis.zenbo_qa_service;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Intent;
import android.view.View;

import com.asus.robotframework.API.MotionControl;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotCommand;
import com.asus.robotframework.API.RobotErrorCode;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

// public class MainActivity extends AppCompatActivity {
public class MainActivity extends RobotActivity {
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
     * @param listView      , (Object) 設定 ListView 資訊
     * @param listviewitems , (String Array) ListView Items 資訊
     * @Param listAdapter   , (Object) 用來管理 ListView 資訊
     * @oaram font_size     , (float) font size 資訊
     * @param scale         , (float) Scaled Density 資訊
     */
    private ListView listView;
    private String[] listviewitems;
    private ArrayAdapter listAdapter;
    private int font_size;
    private float scale;

    /**
     * @param lang    , (String) string for language
     * @param country , (String) string for country
     */
    public String lang    = "zh";
    public String country = "TW";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Keep the screen on
         * app 開啟後一直顯示在螢幕上 , 類似 windows app StayOnTop
         * 參考資料 :
         * https://developer.android.com/training/scheduling/wakelock
         */
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /**
         * Keep the device awake
         *
         * 參考資料 :
         * https://developer.android.com/training/scheduling/wakelock
         * https://developer.android.com/reference/android/os/PowerManager.WakeLock
         */
        // this.powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        // this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyApp::"+getApplicationContext().getResources().getString(R.string.app_name));
        // this.wakeLock.acquire();

        this.get_Intent(false);
        this.Views_Initial(false);
        this.View_Control(false);

        this.head_pos(0, 30);
    }

    /**
     * 取得 activity 傳遞的參數
     *  @param toast_disp , (boolean) 使否顯示 toast 資訊 debug 用
     */
    private void get_Intent(boolean toast_disp) {
        Intent intent = getIntent();

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
                country = "TW";
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

        /* Scaled Density 資訊 */
        this.scale = getResources().getDisplayMetrics().scaledDensity;

        // 設定 Activity 的標題
        TextView textview = (TextView)findViewById(R.id.main_title);
        textview.setText(R.string.main_title);

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
            Toast.makeText(MainActivity.this, "font size :" + this.font_size, Toast.LENGTH_LONG).show();
        }

        /**
         * 設定 listview 的資訊
         * (1) 將 resource 的 main_items array 資訊放入 listviewitems
         * (2) 將 listView 的 text alignment 設為 center
         */
        listView = (ListView)findViewById(R.id.locale_items);
        listviewitems = getResources().getStringArray(R.array.main_items);
        listView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        /**
         * 使用 ArrayAdpater 管理 ListView 的資訊
         */
        listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listviewitems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                /**
                 * runtime 設定
                 * (1) 變更 textView 的 font size
                 * (2) 將 textView 的 text alignment 設為 center
                 * (3) 設定 text color
                 */
                TextView textView=(TextView) view.findViewById(android.R.id.text1);
                textView.setTextSize(font_size);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextColor(Color.RED);

                return view;
            }
        };
        listView.setAdapter(listAdapter);

    }

    /**
     * 宣告或建立 UI object 需要的監聽物件，並執行所有需要的註冊工作。
     * @param toast_disp , (boolean) 使否顯示 toast 資訊 debug 用
     */
    private void View_Control(boolean toast_disp) {
        try {
            // 點擊ListView事件
            listView.setOnItemClickListener(onClickListView);
        }  catch (Exception e) {
            Log.e("aaa", "error "+ e.toString());
            if (toast_disp) {
                Toast.makeText(MainActivity.this, "error " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * 點擊ListView事件Method
     */
    private AdapterView.OnItemClickListener onClickListView = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent;
            Uri uri;
            TextView txtvw = (TextView) view;

            switch (position) {
                case 0: {

                }  break;
                case 1: {

                }  break;
                case 2: {

                }  break;
                case 3: {
                    switch (lang) {
                        case "zh":
                        default: {
                            uri = Uri.parse("https://dec.lib.nsysu.edu.tw/search~S1*cht/");
                        }  break;
                        case "en": {
                            uri = Uri.parse("https://dec.lib.nsysu.edu.tw/search~S1*eng/");
                        }  break;
                    }

                    intent = new Intent(Intent.ACTION_VIEW, uri);

                    startActivity(intent);
                }  break;
                case 4: {
                    uri = Uri.parse("https://service.lis.nsysu.edu.tw/database/?lang="+lang);

                    intent = new Intent(Intent.ACTION_VIEW, uri);

                    startActivity(intent);
                }  break;
                case 5: {
                    switch (lang) {
                        case "zh":
                        default: {
                            uri = Uri.parse("https://findit.lis.nsysu.edu.tw:3443/nsysu/journalsearch?lang=cht");
                        }  break;
                        case "en": {
                            uri = Uri.parse("https://findit.lis.nsysu.edu.tw:3443/nsysu/journalsearch?lang=eng");
                        }  break;
                    }

                    intent = new Intent(Intent.ACTION_VIEW, uri);

                    startActivity(intent);
                }  break;
                case 6: {
                    intent = new Intent(getApplicationContext(), LocaleHelp.class);

                    intent.putExtra("pos", position);
                    intent.putExtra("item_name", txtvw.getText().toString());
                    intent.putExtra("lang", lang);
                    intent.putExtra("country", country);

                    startActivity(intent);
                }  break;
            }


            Toast.makeText(MainActivity.this, "點選第 "+position+" 個 \n內容："+txtvw.getText().toString(), Toast.LENGTH_LONG).show();
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

    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }

    /**
     * zenbo 頭部移動
     * @param x_yaw   , (float) 頭部 x 方向移動
     * @param y_pitch , (float) 頭部 y 方向移動
     */
    private void head_pos(float x_yaw, float y_pitch) {
        float yaw = (float)Math.toRadians(Float.valueOf(x_yaw));
        float pitch = (float)Math.toRadians(Float.valueOf(y_pitch));

        robotAPI.motion.moveHead(yaw, pitch, MotionControl.SpeedLevel.Head.L1);
    }
}
