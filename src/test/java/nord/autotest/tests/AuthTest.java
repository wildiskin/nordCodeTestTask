package nord.autotest.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import nord.autotest.mock.WireMockHelper;
import nord.autotest.model.Action;
import nord.autotest.model.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("Управление сессиями")
@Feature("Аутентификация пользователя (LOGIN)")
public class AuthTest extends BaseTest {

    @Test
    @Story("Успешная аутентификация")
    @DisplayName("Успешный LOGIN при положительном ответе от внешнего сервиса авторизации")
    void shouldSuccessfullyLoginUser() {
        String token = generateToken();

        Allure.step("Настроить мок внешнего сервиса: /auth возвращает 200 OK", () ->
                WireMockHelper.stubAuth(200));

        Response response = Allure.step("Отправить запрос LOGIN с токеном " + token, () ->
                appClient.sendRequest(token, Action.LOGIN));

        Allure.step("Проверить, что приложение вернуло результат OK", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("OK");
        });

        Allure.step("Убедиться, что запрос был передан на внешний сервис /auth с переданным токеном", () ->
                WireMockHelper.verifyAuthCalled(token, 1));
    }

    @Test
    @Story("Неуспешная аутентификация")
    @DisplayName("Ошибка LOGIN при отказе внешнего сервиса (401 Unauthorized)")
    void shouldFailLoginWhenExternalAuthFails() {
        String token = generateToken();

        Allure.step("Настроить мок внешнего сервиса: /auth возвращает 401 Unauthorized", () ->
                WireMockHelper.stubAuth(401));

        Response response = Allure.step("Отправить запрос LOGIN с токеном " + token, () ->
                appClient.sendRequest(token, Action.LOGIN));

        Allure.step("Проверить, что приложение вернуло ошибку и описание", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("ERROR");
            assertThat(apiResponse.getMessage()).isNotNull().isNotEmpty();
        });

        Allure.step("Убедиться, что запрос был передан на внешний сервис /auth", () ->
                WireMockHelper.verifyAuthCalled(token, 1));
    }
}