package nord.autotest.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import nord.autotest.config.TestConfig;
import nord.autotest.model.Action;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class AppClient {

    static {
        RestAssured.baseURI = TestConfig.BASE_URL;
    }

    public Response sendRequestRaw(String apiKey, String token, String action) {
        Map<String, String> formParams = new HashMap<>();
        if (token != null) formParams.put("token", token);
        if (action != null) formParams.put("action", action);

        var spec = given()
                .filter(new AllureRestAssured())
                .contentType("application/x-www-form-urlencoded")
                .accept("application/json");

        if (apiKey != null) {
            spec.header("X-Api-Key", apiKey);
        }

        return spec.formParams(formParams)
                .when()
                .post("/endpoint");
    }

    public Response sendRequest(String token, Action action) {
        return sendRequestRaw(TestConfig.API_KEY, token, action != null ? action.name() : null);
    }
}