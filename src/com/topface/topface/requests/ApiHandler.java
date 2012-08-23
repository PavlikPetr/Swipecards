package com.topface.topface.requests;

import com.topface.topface.utils.Debug;
import android.os.Handler;

abstract public class ApiHandler extends Handler {
	public void response(ApiResponse response) {
		try {
			if (response.code == ApiResponse.ERRORS_PROCCESED)
				;
			else if (response.code != ApiResponse.RESULT_OK)
				fail(response.code, response);			
			else
				success(response);
		} catch (Exception e) {
			Debug.error("api handler exception:", e);
			Debug.log(e.toString());
		}
	}	

	abstract public void success(ApiResponse response) throws NullPointerException;
	abstract public void fail(int codeError, ApiResponse response) throws NullPointerException;
}
