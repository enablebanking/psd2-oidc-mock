package com.enablebanking.oidc;

public class SelectUserModel {
    private String psuId;
    private String redirectUri;
    private String nonce;
    private Boolean isPsd2;

    public String getPsuId() {
        return psuId;
    }

    public void setPsuId(String psuId) {
        this.psuId = psuId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public Boolean getIsPsd2() {
        return isPsd2;
    }

    public void setIsPsd2(Boolean isPsd2) {
        this.isPsd2 = isPsd2;
    }
}
