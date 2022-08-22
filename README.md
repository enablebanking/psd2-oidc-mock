# OIDC Mock service
This service provides an implementation of implicit flow open id connect for authorizing end user in ASPSP for testing purposes.  


## Service configuration
You can modify `src/main/resources/application.properties` file to change different application settings:     
privateKeyPath - path to private key file  
jwksPath - path to jwks file (used for verification of id_token by a third party)  
clientId - client id ("aud" claim in id_token)  
redirectUri - redirect uri (URI where a PSU is redirected after authorization)  
issuer - issuer ("iss" claim in id_token)  
salt - salt for hashing PSU's id (hashed PSU id is used as "sub" claim in id_token)  

## id_token body claims:  
iss - Issuer Identifier for the Issuer of the response. The iss value is a case sensitive URL using the https scheme that contains scheme, host, and optionally, port number and path components and no query or fragment components    
sub - Subject Identifier. A locally unique and never reassigned identifier within the Issuer for the End-User, which is intended to be consumed by the Client, e.g., 24400320 or AItOawmwtWwcT0k51BayewNvutrJUqsvl6qs7A4. It MUST NOT exceed 255 ASCII characters in length. The sub value is a case sensitive string  
aud - Audience(s) that this ID Token is intended for. It MUST contain the OAuth 2.0 client_id of the Relying Party as an audience value. It MAY also contain identifiers for other audiences. In the general case, the aud value is an array of case sensitive strings. In the common special case when there is one audience, the aud value MAY be a single case sensitive string.  
exp - Expiration time on or after which the ID Token MUST NOT be accepted for processing. The processing of this parameter requires that the current date/time MUST be before the expiration date/time listed in the value. Implementers MAY provide for some small leeway, usually no more than a few minutes, to account for clock skew. Its value is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time. See RFC 3339 [RFC3339] for details regarding date/times in general and UTC in particular.  
iat - Time at which the JWT was issued. This is a JSON number representing the number of seconds from 1970-01-01T0:0:0Z as measured in UTC until the date/time.   
nonce - String value used to associate a Client session with an ID Token, and to mitigate replay attacks. The value is passed through unmodified from the Authentication Request to the ID Token.  
psu - String value containing identification provided by DiPocket for an end-user being authenticated. In this claim DiPocket expects to receive an ID by which the user is known in its systems. This value shall differ from the value of the sub claim.  


There are also certificates for the service under `src/main/resources/certs`.  
You can replace them with your own certificates if you want.  

`keys.jwks` file is generated based on the `public.pem` key.  

## Running service locally:  
`./gradlew bootRun`  

## Building and running Docker image:  
### Build image:  
`docker build -t oidcmock .`  
### Run image:  
`docker run -p 8080:8080 oidcmock`  