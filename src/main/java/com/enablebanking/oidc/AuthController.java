package com.enablebanking.oidc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URLEncoder;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("classpath:certs/keys.jwks")
    Resource jwks;

    @SneakyThrows
    @GetMapping("/auth")
    public RedirectView auth(
            @RequestParam(value = "response_type") String responseType,
            @RequestParam(value = "client_id") String clientId,
            @RequestParam(value = "redirect_uri") String redirectUri,
            @RequestParam(value = "scope") String scope,
            @RequestParam(value = "nonce") String nonce) {
        if (!"id_token".equals(responseType)) {
            return new RedirectView(redirectUri + "?error=unsupported_response_type");
        }
        if (!authService.isClientIdValid(clientId)) {
            return new RedirectView(redirectUri + "?error=access_denied&error_description=" + URLEncoder.encode("Invalid client_id", UTF_8));
        }
        if (!authService.isRedirectUriValid(redirectUri)) {
            return new RedirectView(redirectUri + "?error=access_denied&error_description=" + URLEncoder.encode("Invalid redirect_uri", UTF_8));
        }
        List<String> scopes = List.of(scope.split(" "));
        if (!scopes.contains("openid")) {
            return new RedirectView(redirectUri + "?error=access_denied&error_description=" + URLEncoder.encode("scope \"openid\" is not present", UTF_8));
        }

        return new RedirectView("/selectAccount" + "?nonce=" + nonce + "&redirect_uri=" + redirectUri + "&isPsd2=" + scopes.contains("psd2"));
    }

    @GetMapping("/selectAccount")
    public String selectAccount(
            @RequestParam(value = "nonce") String nonce,
            @RequestParam(value = "redirect_uri") String redirectUri,
            @RequestParam(value = "isPsd2") boolean isPsd2
    ) {
        return "<html><body><p>Please provide account id</p><form action=\"/selectAccount\" method=\"post\">" +
                "<input type=\"hidden\" name=\"redirectUri\" value=\"" + redirectUri + "\" />" +
                "<input type=\"hidden\" name=\"nonce\" value=\"" + nonce + "\" />" +
                "<input type=\"hidden\" name=\"isPsd2\" value=\"" + isPsd2 + "\" />" +
                "<input type=\"text\" name=\"psuId\" required />" +
                "<input type=\"submit\" value=\"Submit\" />" +
                "</form></body></html>";
    }

    @PostMapping("/selectAccount")
    public RedirectView selectAccount(
            @ModelAttribute SelectUserModel selectUserModel
    ) {
        return new RedirectView(
                selectUserModel.getRedirectUri() +
                        "?id_token=" +
                        authService.generateIdToken(
                                selectUserModel.getNonce(),
                                selectUserModel.getIsPsd2(),
                                selectUserModel.getPsuId()));
    }

    @SneakyThrows
    @GetMapping("/jwks")
    public JWKS jwks() {
        Reader reader = new InputStreamReader(jwks.getInputStream(), UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(FileCopyUtils.copyToString(reader), JWKS.class);
    }
}