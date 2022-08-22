package com.enablebanking.oidc;

import lombok.Data;

@Data
public class JWK {
    private String kty;
    private String kid;
    private String[] x5c;
    private String n;
    private String e;
}
