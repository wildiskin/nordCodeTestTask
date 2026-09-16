package nord.autotest.mock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WireMockHelper {

    public static void stubAuth(int statusCode) {
        stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse().withStatus(statusCode)));
    }

    public static void stubDoAction(int statusCode) {
        stubFor(post(urlEqualTo("/doAction"))
                .willReturn(aResponse().withStatus(statusCode)));
    }

    public static void verifyAuthCalled(String token, int count) {
        verify(count, postRequestedFor(urlEqualTo("/auth"))
                .withRequestBody(equalTo("token=" + token)));
    }

    public static void verifyDoActionCalled(String token, int count) {
        verify(count, postRequestedFor(urlEqualTo("/doAction"))
                .withRequestBody(equalTo("token=" + token)));
    }
}