package com.coolweather.app.activity;

import com.coolweather.app.R;
import com.coolweather.app.service.AutoUpdateService;
import com.coolweather.app.util.HttpCallbackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author aiyuan
 *
 */
public class WeatherActivity extends Activity implements OnClickListener{
	String TAG = "WeatherActivity";
	//�ϴΰ��·��ؼ���ϵͳʱ��  
    private long lastBackTime = 0;  
    //��ǰ���·��ؼ���ϵͳʱ��  
    private long currentBackTime = 0; 
	private LinearLayout weatherInfoLayout;
	/*
	 * ������ʾ������
	 */
	private TextView cityNameText;
	/*
	 * ������ʾ����ʱ��
	 */
	private TextView publishText;
	/*
	 * ������ʾ��������С��
	 */
	private TextView weatherdesptext;
	/*
	 * ������ʾ����1
	 */
	private TextView temp1Text;
	/*
	 * ������ʾ����2
	 */
	private TextView temp2Text;
	/*
	 * ������ʾ��ǰ����
	 */
	private TextView currentDateText;

	private Button switchCity;
	private Button refreshWeather;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ�����ؼ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherdesptext = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		Log.d(TAG, "hello");
		String countyCode = getIntent().getStringExtra("county_code");
		Log.d(TAG, "haha" + countyCode + "haha");
		if(!countyCode.isEmpty()){
			//���ؼ�����ʱ��ȥ��ѯ����
			publishText.setText("ͬ����");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeathercode(countyCode);
		}else {
			//û���ؼ���������ֱ����ʾ��������
			showWeather();
		}
	}

	/*
	 * ��sharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ��������
	 */
	private void showWeather() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(sharedPreferences.getString("city_name", ""));
		temp1Text.setText(sharedPreferences.getString("temp1", ""));
		temp2Text.setText(sharedPreferences.getString("temp2", ""));
		weatherdesptext.setText(sharedPreferences.getString("weather_Desp", ""));
		publishText.setText("����" + sharedPreferences.getString("publish_time", "") + "����");
		currentDateText.setText(sharedPreferences.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		Intent intent = new Intent(this, AutoUpdateService.class);
		startService(intent);
	}
		

	/*
	 * ��ѯ�ؼ���������Ӧ����������
	 */
	private void queryWeathercode(String countyCode) {
		String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
		queryFromServer(address,"countyCode");
	}

	/**
	 * ���ݴ���ĵ�ַ������ȥ���������ѯ�������Ż���������Ϣ
	 * @param address
	 * @param string
	 */
	private void queryFromServer(String address, final String type) {
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				if("countyCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						//�ӷ��������ص������н�������������
						String[] array = response.split("\\|");
						if(array != null && array.length == 2){
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if("weatherCode".equals(type)){
					//������������ص�������Ϣ
					Utility.handlerWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						publishText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}

	/**
	 * ��ѯ������������Ӧ������
	 * @param weatherCode
	 */
	private void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address,"weatherCode");
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			Log.d(TAG, 123+"");
			intent.putExtra("from_weather_activity", true);
			Log.d(TAG, 12223+"");
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("ͬ����");
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			String weatherCode = sharedPreferences.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
				}
			break;
		default:
		break;
		}
		
			
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//���񷵻ؼ����µ��¼�  
        if(keyCode == KeyEvent.KEYCODE_BACK){  
            //��ȡ��ǰϵͳʱ��ĺ�����  
            currentBackTime = System.currentTimeMillis();  
            //�Ƚ��ϴΰ��·��ؼ��͵�ǰ���·��ؼ���ʱ���������2�룬����ʾ�ٰ�һ���˳�  
            if(currentBackTime - lastBackTime > 2 * 1000){  
                Toast.makeText(this, "�ٰ�һ�η��ؼ��˳�", Toast.LENGTH_SHORT).show();  
                lastBackTime = currentBackTime;  
            }else{ //������ΰ��µ�ʱ���С��2�룬���˳�����  
                finish();  
            }  
            return true;  
        }  
        return super.onKeyDown(keyCode, event);
	}

}
