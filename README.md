# Mock OpenID Connect provider for ASPSP authentication

This repository contains a simple mock implementation of OpenID Connect implicit flow for authentication of end-users (aka PSUs or payment service users)
in an ASPSP (account servicing payment service provider) for consequent consent with authorisation request made by a TPP (third-party provider).

The implementation is mainly intended for testing, however it can be used as a basis for implementation of a real PSU authentication flow for PSD2 APIs.
In case of a real PSU authentication, it is assumed that SCA (Strong Customer Authentication, i.e. multi-factor authentication) is performed before
redirection from the authentication endpoint.

After an end-user gets authenticated by this OpenID Connect provider it is redirected to the ASPSP's web service providing PSD2 APIs to conset with
authorisation request made by a TPP (either for access to account information, or for initiation on a payment, or for confirmation of funds). The ASPSP's
web service providing PSD2 APIs shall (1) get ID token from the `id_token` parameter of the URL to which the end-user has been redirected by the OpenID
Connect provider, (2) verify the ID token, and (3) use value of the `psu` claim for internal identification of the end-user and presentation of
PSU consent details.

## ID token body claims
 
`iss` Issuer Identifier for the Issuer of the response. The iss value is a case sensitive URL using the https scheme that contains scheme, host, and
optionally, port number and path components and no query or fragment components    

`sub` Subject Identifier. A locally unique and never reassigned identifier within the Issuer for the End-User, which is intended to be consumed by the
Client, e.g., 24400320 or AItOawmwtWwcT0k51BayewNvutrJUqsvl6qs7A4. It MUST NOT exceed 255 ASCII characters in length. The sub value is a case sensitive
string
  
`aud` Audience(s) that this ID Token is intended for. It MUST contain the OAuth 2.0 client_id of the Relying Party as an audience value. It MAY also
contain identifiers for other audiences. In the general case, the aud value is an array of case sensitive strings. In the common special case when there is
one audience, the aud value MAY be a single case sensitive string.  

`exp` Expiration time on or after which the ID Token MUST NOT be accepted for processing. The processing of this parameter requires that the current
date/time MUST be before the expiration date/time listed in the value. Implementers MAY provide for some small leeway, usually no more than a few minutes,
to account for clock skew. Its value is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time. See
RFC 3339 [RFC3339] for details regarding date/times in general and UTC in particular.  

`iat` Time at which the JWT was issued. This is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the
date/time.
   
`nonce` String value used to associate a Client session with an ID Token, and to mitigate replay attacks. The value is passed through unmodified from the
Authentication Request to the ID Token.  

`psu` String value containing identification use by ASPSP for an end-user being authenticated. In this claim ASPSP's web service providing PSD2 APIs (and
implementing UI for conset with authorisation request made by a TPP) expects to receive an ID by which the user is known in its systems. This value may
differ from the value of the `sub` claim, which often contains an identifier used by an end-user to authenticate themselves (i.e. user name, email, phone
number or similar).

## Service configuration

You can modify `src/main/resources/application.properties` file to change different application settings:     

`privateKeyPath` - path to private key file  

`jwksPath` - path to jwks file (used for verification of id_token by a third party)  

`clientId` - client id ("aud" claim in id_token)  

`redirectUri` - redirect uri (URI where a PSU is redirected after authorization)  

`issuer` - issuer ("iss" claim in id_token)  

`salt` - salt for hashing PSU's id (hashed PSU id is used as "sub" claim in id_token)  

There are also certificates used by the service under `src/main/resources/certs` folder.
