package nord.autotest.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import nord.autotest.model.Action;
import nord.autotest.model.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Безопасность и валидация")
@Feature("Проверка заголовков и формата входных параметров")
public class SecurityValidationTest extends BaseTest {

    @Test
    @Story("Проверка API-ключа")
    @DisplayName("Отказ в доступе при отсутствии заголовка X-Api-Key")
    void shouldRejectRequestWithoutApiKey() {
        String token = generateToken();

        Response response = Allure.step("Отправить запрос без заголовка X-Api-Key", () ->
                appClient.sendRequestRaw(null, token, Action.LOGIN.name()));

        Allure.step("Проверить, что получен ответ с ошибкой", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("ERROR");
        });
    }

    @Test
    @Story("Проверка API-ключа")
    @DisplayName("Отказ в доступе при передаче неверного X-Api-Key")
    void shouldRejectRequestWithInvalidApiKey() {
        String token = generateToken();

        Response response = Allure.step("Отправить запрос с невалидным X-Api-Key", () ->
                appClient.sendRequestRaw("WRONG_KEY", token, Action.LOGIN.name()));

        Allure.step("Проверить, что получен ответ с ошибкой", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("ERROR");
        });
    }

    @ParameterizedTest(name = "Токен: {0}")
    @ValueSource(strings = {
            "SHORT123",                                      // меньше 32 символов
            "TOOLONGTOKEN123456789012345678901234567890",    // больше 32 символов
            "1234567890123456789012345678901!",             // недопустимый спецсимвол
            "abcdef1234567890abcdef1234567890"              // строчные буквы
    })
    @Story("Валидация токена")
    @DisplayName("Отказ при передаче токена невалидного формата или длины")
    void shouldRejectInvalidTokenFormat(String invalidToken) {
        Response response = Allure.step("Отправить запрос с невалидным токеном: " + invalidToken, () ->
                appClient.sendRequest(invalidToken, Action.LOGIN));

        Allure.step("Проверить, что получен ответ с ошибкой", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("ERROR");
        });
    }

    @Test
    @Story("Валидация действия")
    @DisplayName("Отказ при передаче неизвестного действия")
    void shouldRejectUnknownAction() {
        String token = generateToken();

        Response response = Allure.step("Отправить запрос с неизвестным действием UNKNOWN", () ->
                appClient.sendRequestRaw("qazWSXedc", token, "UNKNOWN"));

        Allure.step("Проверить, что получен ответ с ошибкой", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("ERROR");
        });
    }
}