/*
 * Copyright (c) 2015. PayPoint
 */

package com.paypoint.sdk.demo.merchant;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.paypoint.sdk.library.log.Logger;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.http.POST;

/**
 * Handles reading of PayPoint merchant token - when integrating this will be the responsibility
 * of the merchant to implement this REST call to their own server
 * Who:  Pete
 * When: 20/04/2015
 * What:
 */
public class MerchantTokenManager {

    public interface GetTokenCallback {
        public void getTokenSucceeded(String token);

        public void getTokenFailed(RetrofitError error);
    }

    // REST service to get merchant token
    public interface MerchantTokenService {
        @POST("/getToken")
        void getMerchantToken(Callback<TokenResponse> callback);
    }

    private MerchantTokenService getService(String serverUrl) {

        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(serverUrl)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog(Logger.TAG))
                .build();

        return adapter.create(MerchantTokenService.class);
    }

    /**
     * Class describing the JSON response from getToken
     */
    private static class TokenResponse {

        @SerializedName("accessToken")
        private String accessToken;
    }

    /**
     * Reads a merchant token from the merchant server - this will be the responsibility of the
     * merchant to implement
     * @param serverUrl
     */
    public void getMerchantToken(String serverUrl, final GetTokenCallback callback) {
        MerchantTokenService service = getService(serverUrl);

        service.getMerchantToken(new Callback<TokenResponse>() {
            @Override
            public void success(TokenResponse response, retrofit.client.Response response2) {
                if (callback != null) {
                    if (response != null &&
                        !TextUtils.isEmpty(response.accessToken)) {
                        callback.getTokenSucceeded(response.accessToken);
                    } else {
                        callback.getTokenFailed(null);
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                callback.getTokenFailed(error);
            }
        });
    }
}
