package org.hopto.dklis.zenbo_qa_service;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
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
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// public class qa_question extends AppCompatActivity {
public class qa_question extends RobotActivity {
    private static final String ITEM_TITLE = "Item title";
    private static final String ITEM_ICON  = "Item icon";

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
     * @param lang     , (String)  string for language
     * @param country  , (String)  string for country
     * @param title    , (String)  string for activity title
     * @param category , (Integer) Integer for category
     */
    private String lang    = "zh";
    private String country = "TW";
    private String title   = "";
    private int category   = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qa_question);

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
            category = intent.getIntExtra("pos", 0);
        } catch (Exception e){
            Log.e("LocaleHelp - category", "error "+ e.toString());
            if (toast_disp) {
                Toast.makeText(getBaseContext(), "category error " + e.toString(), Toast.LENGTH_LONG).show();
            }
            category = 0;
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
        TextView textview = (TextView)findViewById(R.id.qa_question_title);
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
        listView = (ListView)findViewById(R.id.qa_question_items);
        switch (category) {
            case 0: {
                listviewitems = getResources().getStringArray(R.array.entrance_items);
                listviewicons = getResources().obtainTypedArray(R.array.entrance_icons);
            }  break;
            case 1: {
                listviewitems = getResources().getStringArray(R.array.thesis_items);
                listviewicons = getResources().obtainTypedArray(R.array.thesis_icons);
            }  break;
            case 2: {
                listviewitems = getResources().getStringArray(R.array.eresource_items);
                listviewicons = getResources().obtainTypedArray(R.array.eresource_icons);
            }  break;
            case 3: {
                listviewitems = getResources().getStringArray(R.array.media_items);
                listviewicons = getResources().obtainTypedArray(R.array.media_icons);
            }  break;
            case 4: {
                listviewitems = getResources().getStringArray(R.array.software_items);
                listviewicons = getResources().obtainTypedArray(R.array.software_icons);
            }  break;
            case 5: {
                listviewitems = getResources().getStringArray(R.array.email_items);
                listviewicons = getResources().obtainTypedArray(R.array.email_icons);
            }  break;
        }

        listView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        itemList = new ArrayList<Map<String, Object>>();
        itemList.clear();

        try {
            for (int i = 0; i < listviewitems.length; i += 1) {
                Map<String, Object> item = new HashMap<String, Object>();
                item.put(ITEM_TITLE, listviewitems[i]);
                item.put(ITEM_ICON, listviewicons.getResourceId(i, 0));
                itemList.add(item);
            }
        }  catch (Exception e) {

        }

        /**
         * 使用 SimpleAdapter 管理 ListView 的資訊
         * (1) 將 res 內 locale_items 與 locale_icons 的資訊，透過 SimpleAdapter 分別把 text 與 icon 放置
         *     到 list_view_item 的 layout 內
         * (2) list_view_item 的 layout 資訊將塞入 ListView 內。
         */
        listAdapter = new SimpleAdapter(getBaseContext(), itemList, R.layout.list_view_item, new String[] {ITEM_TITLE, ITEM_ICON}, new int[] {R.id.txtView, R.id.imgView}) {
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
                 * runtime 設定
                 * (1) 變更 textView 的 font size
                 * (2) 將 textView 的 text alignment 設為 center
                 * (3) 設定 text color
                 */
                TextView textView=(TextView) view.findViewById(R.id.txtView);
                textView.setTextSize(font_size);
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
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
                Toast.makeText(getBaseContext(), "error " + e.toString(), Toast.LENGTH_LONG).show();
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
            TextView v = (TextView) view.findViewById(R.id.txtView);

            intent = new Intent(getApplicationContext(), qa_answer.class);

            intent.putExtra("category", category);
            intent.putExtra("question", position);
            intent.putExtra("item_name", v.getText().toString());
            intent.putExtra("lang", lang);
            intent.putExtra("country", country);

            startActivity(intent);

            /*switch (position) {
                case 0: {
                    intent = new Intent(getApplicationContext(), qa_answer.class);

                    intent.putExtra("category", category);
                    intent.putExtra("pos", position);
                    intent.putExtra("item_name", v.getText().toString());
                    intent.putExtra("lang", lang);
                    intent.putExtra("country", country);

                    startActivity(intent);
                }  break;
                case 1:
                default: {

                }  break;
            }*/

            Toast.makeText(getBaseContext(), "點選第 "+position+" 個 \n內容："+v.getText().toString(), Toast.LENGTH_LONG).show();
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

    public qa_question() {
        super(robotCallback, robotListenCallback);
    }
}
