package com.topface.topface.utils.social;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.state.AppState;
import com.topface.topface.state.TopfaceAppState;

import java.io.IOException;

import javax.inject.Inject;

import ru.ok.android.sdk.Odnoklassniki;

/**
 * Created by Петр on 03.04.2016.
 * Get user data in background
 */
public class CurrentUser {

    @Inject
    TopfaceAppState mAppState;
    private GetUserListener mListener;

    public CurrentUser() {
        App.from(App.getContext()).inject(this);
    }

    public void setUserListener(GetUserListener listener) {
        mListener = listener;
    }

    public void getUser(Odnoklassniki ok) {
        new GetCurrentUserTask(ok).execute();
    }

    private final class GetCurrentUserTask extends AsyncTask<Void, Void, String> {

        private final Odnoklassniki odnoklassniki;

        public GetCurrentUserTask(Odnoklassniki ok) {
            odnoklassniki = ok;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return odnoklassniki.request("users.getCurrentUser", null, "get");
            } catch (IOException e) {
                Debug.error("Odnoklassniki doInBackground error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            Debug.log("Odnoklassniki users.getCurrentUser result: " + s);
            OkUserData data = !TextUtils.isEmpty(s) ? JsonUtils.fromJson(s, OkUserData.class) : null;
            if (data != null) {
                mAppState.setData(data);
            }
            if (mListener != null) {
                mListener.onSuccess(data);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Debug.error("Odnoklassniki auth cancelled");
        }
    }

    public interface GetUserListener {

        void onSuccess(OkUserData data);

    }
}
