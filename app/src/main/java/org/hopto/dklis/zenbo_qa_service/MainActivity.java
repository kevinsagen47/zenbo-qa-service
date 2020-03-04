package org.hopto.dklis.zenbo_qa_service;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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


    private ListView listView;
    private String[] listviewitems;
    private ArrayAdapter listAdapter;
    private int font_size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Keep the screen on
        // https://developer.android.com/training/scheduling/wakelock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Keep the CPU on
        // https://developer.android.com/training/scheduling/wakelock
        // https://developer.android.com/reference/android/os/PowerManager.WakeLock
        // this.powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        // this.wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"MyApp::"+getApplicationContext().getResources().getString(R.string.app_name));
        // this.wakeLock.acquire();

        this.head_pos(0, 30);
    }

    /**
     * 初始化 activiry_main.xml layout 上元件資訊
     * @param toast_disp , (boolean) 使否顯示 toast 資訊 debug 用
     * @param lang       , (String)  切換語系使用
     */
    private void Views_Initial(boolean toast_disp, String lang) {
        /**
         * 取得畫面元件後指定給欄位變數。
         */
        // 設定 Activity 的標題
    }

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
