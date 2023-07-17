package com.enablebanking.oidc;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.util.List;


import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @SneakyThrows
    @GetMapping("/auth")
    public RedirectView auth(
            @RequestParam(value = "response_type") String responseType,
            @RequestParam(value = "client_id") String clientId,
            @RequestParam(value = "redirect_uri") String redirectUri,
            @RequestParam(value = "scope") String scope,
            @RequestParam(value = "nonce") String nonce,
            @RequestParam(value = "state") String state
    ) {
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

        return new RedirectView("/selectAccount" + "?nonce=" + nonce + "&redirect_uri=" + redirectUri + "&isPsd2=" + scopes.contains("psd2") + "&state=" + state);
    }

    @GetMapping("/selectAccount")
    public String selectAccount(
            @RequestParam(value = "nonce") String nonce,
            @RequestParam(value = "redirect_uri") String redirectUri,
            @RequestParam(value = "isPsd2") boolean isPsd2,
            @RequestParam(value = "state") String state
    ) {
        return "<html>" +
                    "<body>" +
                        "<p>Please provide account id</p>" +
                        "<form action=\"/selectAccount\" method=\"post\">" +
                            "<input type=\"hidden\" name=\"redirectUri\" value=\"" + redirectUri + "\" />" +
                            "<input type=\"hidden\" name=\"nonce\" value=\"" + nonce + "\" />" +
                            "<input type=\"hidden\" name=\"isPsd2\" value=\"" + isPsd2 + "\" />" +
                            "<input type=\"hidden\" name=\"state\" value=\"" + state + "\" />" +
                            "<input type=\"text\" name=\"psuId\" required />" +
                            "<input type=\"submit\" value=\"Submit\" />" +
                        "</form>" +
                        "<form action=\"/cancel\" method=\"post\">" +
                            "<input type=\"hidden\" name=\"redirectUri\" value=\"" + redirectUri + "\" />" +
                            "<input type=\"hidden\" name=\"state\" value=\"" + state + "\" />" +
                            "<input type=\"submit\" value=\"Cancel\" />" +
                        "</form>" +
                    "</body>" +
                "</html>";
    }

    @PostMapping("/selectAccount")
    public RedirectView selectAccount(
            @ModelAttribute SelectUserModel selectUserModel,
            @RequestParam(value = "state") String state
    ) {
        return new RedirectView(
                selectUserModel.getRedirectUri() +
                        "?id_token=" +
                        authService.generateIdToken(
                                selectUserModel.getNonce(),
                                selectUserModel.getIsPsd2(),
                                selectUserModel.getPsuId()) + "&state=" + state);
    }

    @PostMapping("/cancel")
    public RedirectView cancel(
            @ModelAttribute CancelModel cancelModel,
            @RequestParam(value = "state") String state
    ) {
        return new RedirectView(
                cancelModel.getRedirectUri() +
                        "?error=access_denied" +
                        "&state=" + state);
    }

    @SneakyThrows
    @GetMapping("/jwks")
    public JWKS jwks() {
        return authService.getJwks();
    }
}