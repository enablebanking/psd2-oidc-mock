package com.enablebanking.oidc;

public class CancelModel {
    private String redirectUri;

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
