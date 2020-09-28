package com.siterwell.application.common;


import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.siterwell.application.R;
import com.siterwell.sdk.http.BaseHttpUtil;
import com.siterwell.sdk.http.HekrCodeUtil;
import com.siterwell.sdk.http.HekrUser;

import java.util.Locale;

import cz.msebera.android.httpclient.Header;


public class Config {
	private static final String TAG = "Config";

	public final static String RootPath = "61.164.94.198:1415/app";
	public final static String PushPath = "61.164.94.198:1415/SiterLink/";
	public final static String PushUrl = "http://" + PushPath ;
	public final static String ApkVerUrl = "http://" + RootPath + "/SiterLink.ver";
	public final static String ApkUrl = "http://" + RootPath + "/SiterLink.apk";

	public static final String UPDATE_APKNAME="Siterlink.apk";
	public final static String PrivacyPolicyUrl = "file:///android_asset/register/";
	public final static String UserProtocolUrl = "file:///android_asset/userProtocol/";

	public static int getVerCode(Context context, String packageName) {
		int verCode = -1;
		try {
			verCode = context.getPackageManager().getPackageInfo(
					packageName, 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verCode;
	}
	
	public static String getVerName(Context context, String packageName) {
		String verName = "";
		try {
			verName = context.getPackageManager().getPackageInfo(
					packageName, 0).versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verName;	

	}
	
	public static String getAppName(Context context) {
		return context.getText(R.string.app_name).toString();
	}

	/**
	 * 隐私政策网址
	 */
	public static String getPrivacyAgreement(Context context){
		Locale locale = context.getResources().getConfiguration().locale;
		String lan = locale.getLanguage();
		switch (lan) {
			case "zh":
				return PrivacyPolicyUrl + "register_zh.html";
			case "fr":
				return PrivacyPolicyUrl + "register_fr.html";
			case "de":
				return PrivacyPolicyUrl + "register_de.html";
			case "es":
				return PrivacyPolicyUrl + "register_es.html";
			case "en":
			default:
				return PrivacyPolicyUrl + "register_en.html";
		}
	}

	/**
	 * 用户协议网址
	 */
	public static String getUserProtocol(Context context){
		Locale locale = context.getResources().getConfiguration().locale;
		String lan = locale.getLanguage();
		switch (lan) {
			case "zh":
				return UserProtocolUrl + "user_protocol_zh.html";
			case "fr":
				return UserProtocolUrl + "user_protocol_fr.html";
			case "de":
				return UserProtocolUrl + "user_protocol_de.html";
			case "es":
				return UserProtocolUrl + "user_protocol_es.html";
			case "en":
			default:
				return UserProtocolUrl + "user_protocol_en.html";
		}
	}

	/**
	 * 手机推送设置
	 */
	public static String getPhonePushSetting(Context context){
		Locale locale = context.getResources().getConfiguration().locale;
		String lan = locale.getLanguage();
		switch (lan) {
			case "zh":
				return PushUrl + "zh/shezhi.html";
			case "fr":
				return PushUrl + "fr/shezhi.html";
			case "de":
				return PushUrl + "de/shezhi.html";
			case "en":
			default:
				return PushUrl + "en/shezhi.html";
		}
	}

	/**
	 * 3.5 获取APP更新信息
	 *
	 * @param loginListener 回调接口
	 */

	public static void getUpdateInfo(Context mContext, final HekrUser.LoginListener loginListener) {

		BaseHttpUtil.getData(mContext, ApkVerUrl, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int i, Header[] headers, byte[] bytes) {
				loginListener.loginSuccess(new String(bytes));
			}

			@Override
			public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
				loginListener.loginFail(HekrCodeUtil.getErrorCode(i, bytes));
			}
		});
	}

	public static class UpdateInfo {
		public int code;
		public String name;

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	};
}

