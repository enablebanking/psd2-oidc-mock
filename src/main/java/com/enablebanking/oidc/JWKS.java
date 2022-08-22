package com.enablebanking.oidc;

import lombok.Data;

@Data
public class JWKS {
    private JWK[] keys;
}
