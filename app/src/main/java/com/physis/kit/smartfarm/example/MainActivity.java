package com.physis.kit.smartfarm.example;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.physicomtech.kit.physislibrary.PHYSIsMQTTActivity;

public class MainActivity extends PHYSIsMQTTActivity {

    private final String SERIAL_NUMBER = "XXXXXXXXXXXX";        // PHYSIs Maker Kit 시리얼번호

    private static final String SUB_SENSING_TOPIC = "Sensing";      // 센싱 데이터 수신 Topic
    private static final String PUC_CONTROL_TOPIC = "Control";      // 제어 데이터 전송 Topic

    Button btnConnect, btnDisconnect, btnStart, btnStop;        // 액티비티 위젯
    Button btnPumpOn, btnPumpOff, btnLedOn, btnLedOff, btnFanSetup;
    TextView tvTempValue, tvHumiValue, tvLightValue;
    EditText etFanSpeed;
    ProgressBar pgbConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();                   // 위젯 생성 및 초기화 함수 호출
        setEventListener();             // 이벤트 리스너 설정 함수 호출
    }

    /*
      # 위젯 생성 및 초기화
   */
    private void initWidget() {
        tvTempValue = findViewById(R.id.tv_temp_val);             // 텍스트뷰 생성
        tvHumiValue = findViewById(R.id.tv_humi_val);
        tvLightValue = findViewById(R.id.tv_lux_val);

        btnConnect = findViewById(R.id.btn_connect);                // 버튼 생성
        btnDisconnect = findViewById(R.id.btn_disconnect);
        btnStart = findViewById(R.id.btn_start);
        btnStop = findViewById(R.id.btn_stop);
        btnPumpOn = findViewById(R.id.btn_pump_on);
        btnPumpOff = findViewById(R.id.btn_pump_off);
        btnLedOn = findViewById(R.id.btn_led_on);
        btnLedOff = findViewById(R.id.btn_led_off);
        btnFanSetup = findViewById(R.id.btn_fan_speed);
        pgbConnect = findViewById(R.id.pgb_connect);                // 프로그래스 생성

        etFanSpeed = findViewById(R.id.et_fan_speed);
    }

    /*
      # 뷰 (버튼) 이벤트 리스너 설정
   */
    private void setEventListener() {
        btnConnect.setOnClickListener(new View.OnClickListener() {                  // 연결 버튼
            @Override
            public void onClick(View v) {           // 버튼 클릭 시 호출
                btnConnect.setEnabled(false);               // 연결 버튼 비활성화 설정
                pgbConnect.setVisibility(View.VISIBLE);     // 연결 프로그래스 가시화 설정
                connectMQTT();                              // MQTT 연결 시도
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {               // 연결 종료 버튼
            @Override
            public void onClick(View v) {
                disconnectMQTT();                               // MQTT 연결 종료
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {                    // 모니터링 시작 버튼
            @Override
            public void onClick(View v) {
                startSubscribe(SERIAL_NUMBER, SUB_SENSING_TOPIC);       // Subscribe 시작
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {                     // 모니터링 종료 버튼
            @Override
            public void onClick(View v) {
                stopSubscribe(SERIAL_NUMBER, SUB_SENSING_TOPIC);       // Subscribe 중지
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });

        btnPumpOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(SERIAL_NUMBER, PUC_CONTROL_TOPIC, "P1");
            }
        });

        btnPumpOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(SERIAL_NUMBER, PUC_CONTROL_TOPIC, "P0");
            }
        });

        btnLedOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(SERIAL_NUMBER, PUC_CONTROL_TOPIC, "L1");
            }
        });

        btnLedOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(SERIAL_NUMBER, PUC_CONTROL_TOPIC, "L0");
            }
        });

        btnFanSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fanSpeed = etFanSpeed.getText().toString();
                int pwm = fanSpeed.length() != 0 ? Integer.parseInt(fanSpeed) : 0;
                publish(SERIAL_NUMBER, PUC_CONTROL_TOPIC, "F" + pwm);
            }
        });
    }

    /*
        # MQTT 연결 결과 수신
        - MQTT Broker 연결에 따른 결과를 전달받을 때 호출
        - 인자로 연결 성공 여부를 전달
     */
    @Override
    protected void onMQTTConnectedStatus(boolean result) {
        super.onMQTTConnectedStatus(result);
        pgbConnect.setVisibility(View.INVISIBLE);               // 연결 프로그래스 비가시화 설정
        String toastMsg;                                        // 연결 결과에 따른 Toast 메시지 출력
        if(result){
            toastMsg = "MQTT Broker와 연결되었습니다.";
        }else{
            toastMsg = "MQTT Broker와 연결에 실패하였습니다.";
        }
        Toast.makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();

        btnConnect.setEnabled(!result);                          // 연결 버튼 활성화 상태 설정
        btnDisconnect.setEnabled(result);
        btnStart.setEnabled(result);                             // 모니터링 제어 버튼 상태 설정
        btnStop.setEnabled(false);
    }

    /*
          # MQTT 연결 종료 처리
    */
    @Override
    protected void onMQTTDisconnected() {
        super.onMQTTDisconnected();
        Toast.makeText(getApplicationContext(), "MQTT Broker와 연결이 종료되었습니다.", Toast.LENGTH_SHORT).show();

        btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnStart.setEnabled(false);
        btnStop.setEnabled(false);
    }

    /*
       # MQTT Subscribe 리스너
       - Subscribe한 Topic에 대한 Publish 메시지가 수신되었을 때 호출
    */
    @Override
    protected void onSubscribeListener(String serialNum, String topic, String data) {
        super.onSubscribeListener(serialNum, topic, data);
        if(serialNum.equals(SERIAL_NUMBER) && topic.equals(SUB_SENSING_TOPIC)){     // 토픽에 따른 데이터 처리
            showMonitoringData(data);               // 센싱 정보 출력 함수 호출
        }
    }

    @SuppressLint("SetTextI18n")
    private void showMonitoringData(String data) {
        if(data == null || data.equals(""))
            return;
        String[] values = data.split(",");

        if (values.length != 3)
            return;

        tvTempValue.setText("온도\n" + values[0] + " ℃");
        tvHumiValue.setText("습도\n" + values[1] + " %");
        tvLightValue.setText("조도\n" + values[2] + " %");
    }
}