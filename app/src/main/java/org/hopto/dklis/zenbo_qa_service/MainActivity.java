package org.hopto.dklis.zenbo_qa_service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
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
import com.asus.robotframework.API.RobotFace;
import com.asus.robotframework.API.RobotUtil;
import com.asus.robotframework.API.SpeakConfig;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// public class MainActivity extends AppCompatActivity {
public class MainActivity extends RobotActivity {
    private static final int REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE = 100;

    public final static String TAG = "MainActivity";
    /**
     * 必要的 DOMAIN UUID
     */
    public final static String DOMAIN = "1C03CD53372F458EBB0E62E176B50FF8";

    // private static RobotActivity m_activity;
    private static Context context;

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
     * @param listviewicons , (TypedArray) ListView Icons 資訊
     * @Param listAdapter   , (Object) 用來管理 ListView 資訊
     * @oaram font_size     , (float) font size 資訊
     * @param scale         , (float) Scaled Density 資訊
     */
    private ListView listView;
    private String[] listviewitems;
    private TypedArray listviewicons;
    private List<Map<String, Object>> itemList;
    // private ArrayAdapter listAdapter;
    private SimpleAdapter listAdapter;
    private int font_size;
    private float scale;

    /**
     * @param lang    , (String) string for language
     * @param country , (String) string for country
     */
/*    public String lang    = "zh";
    public String country = "TW";*/

    public static String lang    = "zh";
    public static String country = "TW";

    private static final String ITEM_TITLE = "Item title";
    private static final String ITEM_ICON  = "Item icon";

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

        askForWriteExternalStoragePermission();
    }

    /**
     * Returns a "static" application context. Don't try to create dialogs on
     * this, it's not gonna work!
     *
     * @return
     */
    /*public static Context getContext() {
        return context;
    }*/

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

        // context = this;
        context = getApplicationContext();

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
        /**
        listView = (ListView)findViewById(R.id.main_items);
        listviewitems = getResources().getStringArray(R.array.main_items);
        listView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
         */

        /**
         * 設定 listview 的資訊
         * (1) 將 resource 的 main_items array 資訊放入 listviewitems
         * (2) 將 resource 的 main_icons array 資訊放入 listviewicons
         * (3) 將 listView 的 text alignment 設為 center
         */
        listView = (ListView)findViewById(R.id.main_items);
        listviewitems = getResources().getStringArray(R.array.main_items);
        listviewicons = getResources().obtainTypedArray(R.array.main_icons);
        listView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        itemList = new ArrayList<Map<String, Object>>();
        itemList.clear();
        for (int i = 0; i < listviewitems.length; i += 1) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put(ITEM_TITLE, listviewitems[i]);
            item.put(ITEM_ICON, listviewicons.getResourceId(i,0));
            itemList.add(item);
        }

        /**
         * (x) 使用 ArrayAdpater 管理 ListView 的資訊
         * 使用 SimpleAdapter 管理 ListView 的資訊
         * (1) 將 res 內 main_items 與 main_icons 的資訊，透過 SimpleAdapter 分別把 text 與 icon 放置
         *     到 list_view_item 的 layout 內
         * (2) list_view_item 的 layout 資訊將塞入 ListView 內。
         */
        listAdapter = new SimpleAdapter(getBaseContext(), itemList, R.layout.list_view_item, new String[] {ITEM_TITLE, ITEM_ICON}, new int[] {R.id.txtView, R.id.imgView}) {
        // listAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listviewitems) {

            /**
             * runtime 設定
             * (1) 變更 textView 的 font size
             * (2) 將 textView 的 text alignment 設為 center
             * (3) 設定 text color
             */
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                /**
                 * 透過 (Layout) list_view_item 內的元件 id 資訊，來修改元件內的參數設定。
                 * (1) 修改 textview 的屬性設定，如：文字大小、顏色、align位置。
                 * (2) 修改 LinearLayout 的屬性設定，如：align位置。
                 */
                TextView textView=(TextView) view.findViewById(R.id.txtView);
                textView.setTextSize(font_size);
                // textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                textView.setTextColor(Color.RED);

                /*
                ImageView imageView=(ImageView) view.findViewById(R.id.imgView);
                imageView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                */

                LinearLayout layout = (LinearLayout) view.findViewById(R.id.layout_items);
                layout.setGravity(Gravity.CENTER|Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);

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
            TextView v = (TextView) view.findViewById(R.id.txtView);;

            switch (position) {
                case 0: {
                    intent = new Intent(getApplicationContext(), qa_category.class);

                    intent.putExtra("pos", position);
                    intent.putExtra("item_name", v.getText().toString());
                    intent.putExtra("lang", lang);
                    intent.putExtra("country", country);

                    startActivity(intent);
                }  break;
                case 1: {
                    intent = new Intent(getApplicationContext(), FloorLayout.class);

                    intent.putExtra("pos", position);
                    intent.putExtra("item_name", v.getText().toString());
                    intent.putExtra("lang", lang);
                    intent.putExtra("country", country);

                    startActivity(intent);
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
                    intent.putExtra("item_name", v.getText().toString());
                    intent.putExtra("lang", lang);
                    intent.putExtra("country", country);

                    startActivity(intent);
                }  break;
            }

            Toast.makeText(MainActivity.this, "點選第 "+position+" 個 \n內容："+v.getText().toString(), Toast.LENGTH_LONG).show();
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
            Log.d(TAG, "speak Complete");
        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {
            String text;
            text = "onEventUserUtterance: " + jsonObject.toString();
            Log.d(TAG, text);
        }

        @Override
        public void onResult(JSONObject jsonObject) {
            String text;
            text = "onResult: " + jsonObject.toString();
            Log.d(TAG, text);

            /**
             *  讀取 IntentionId 的資訊。
             */
            String sIntentionID = RobotUtil.queryListenResultJson(jsonObject, "IntentionId");
            Log.d(TAG, "Intention Id = " + sIntentionID);

            if(sIntentionID.equals("main_activity_plans")) {
                String sSluResultCity = RobotUtil.queryListenResultJson(jsonObject, "main_class", null);
                Log.d(TAG, "Result City = " + sSluResultCity);

                if(sSluResultCity!= null) {
                    Intent intent;
                    Uri uri;
                    String[] main_items = context.getResources().getStringArray(R.array.main_items);

                    switch (sSluResultCity) {
                        case "faq": {
                            intent = new Intent(context, qa_category.class);

                            intent.putExtra("pos", 0);
                            intent.putExtra("item_name", main_items[0]);
                            intent.putExtra("lang", lang);
                            intent.putExtra("country", country);

                            context.startActivity(intent);
                        }  break;
                        case "floor": {
                            intent = new Intent(context, FloorLayout.class);

                            intent.putExtra("pos", 1);
                            intent.putExtra("item_name", main_items[1]);
                            intent.putExtra("lang", lang);
                            intent.putExtra("country", country);

                            context.startActivity(intent);
                        }  break;
                        case "guide": {

                        }  break;
                        case "catalog": {
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

                            context.startActivity(intent);
                        }  break;
                        case "database": {
                            uri = Uri.parse("https://service.lis.nsysu.edu.tw/database/?lang="+lang);

                            intent = new Intent(Intent.ACTION_VIEW, uri);

                            context.startActivity(intent);
                        }  break;
                        case "ejournal": {
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

                            context.startActivity(intent);
                        }  break;
                        case "setting": {
                            intent = new Intent(context, LocaleHelp.class);

                            intent.putExtra("pos", 6);
                            intent.putExtra("item_name", main_items[6]);
                            intent.putExtra("lang", lang);
                            intent.putExtra("country", country);

                            context.startActivity(intent);
                        }  break;
                    }


                }
            }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 檢查收到的權限要求編號是否和我們送出的相同
        if (requestCode == REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "取得 WRITE_EXTERNAL_STORAGE 權限",
                        Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void askForWriteExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            // 這項功能尚未取得使用者的同意
            // 開始執行徵詢使用者的流程
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    MainActivity.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder altDlgBuilder =
                        new AlertDialog.Builder(MainActivity.this);
                altDlgBuilder.setTitle("提示");
                altDlgBuilder.setMessage("App需要讀寫SD卡中的資料。");
                altDlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
                altDlgBuilder.setCancelable(false);
                altDlgBuilder.setPositiveButton("確定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // 顯示詢問使用者是否同意功能權限的對話盒
                                // 使用者答覆後會執行onRequestPermissionsResult()
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{
                                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);
                            }
                        });
                altDlgBuilder.show();

                return;
            } else {
                // 顯示詢問使用者是否同意功能權限的對話盒
                // 使用者答覆後會執行onRequestPermissionsResult()
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_FOR_WRITE_EXTERNAL_STORAGE);

                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // close face
        robotAPI.robot.setExpression(RobotFace.HIDEFACE);

        // jump dialog domain
        /**
         * 指向開始的 plan 位址
         */
        robotAPI.robot.jumpToPlan(DOMAIN, "ThisPlanLaunchingThisApp");

        // listen user utterance
        robotAPI.robot.speakAndListen("您好，還迎來到圖書與資訊處~", new SpeakConfig().timeout(20));
    }

    @Override
    protected void onPause() {
        super.onPause();

        //stop listen user utterance
        robotAPI.robot.stopSpeakAndListen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
