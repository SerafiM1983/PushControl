/** В будующих версиях будет собирать ошибки приложения и отправлять на сервер */

package com.serafimApp.pushcontrol;

import android.util.Log;

public class LogCat {
	private boolean show = false;
	private String tag;
	private String massage;

	public LogCat(boolean show) {
		this.show = show;
	}
	public void logShow(String tag, String massage) {
		if (show) Log.d(tag, massage);
	}
}
