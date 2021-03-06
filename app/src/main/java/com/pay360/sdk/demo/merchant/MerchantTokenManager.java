/*
 * Copyright (c) 2016 Capita plc
 */

package com.pay360.sdk.demo.merchant;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.pay360.sdk.library.log.Logger;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.android.AndroidLog;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Handles reading of Pay360 merchant token - when integrating this will be the responsibility
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
        @GET("/getToken/{installationId}")
        void getMerchantToken(@Path("installationId") String installationId,
                              Callback<TokenResponse> callback);
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
    public void getMerchantToken(String serverUrl, String installationId, final GetTokenCallback callback) {
        MerchantTokenService service = getService(serverUrl);

        service.getMerchantToken(installationId, new Callback<TokenResponse>() {
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
