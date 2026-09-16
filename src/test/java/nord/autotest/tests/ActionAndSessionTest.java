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
@Feature("Выполнение действий (ACTION) и завершение сессии (LOGOUT)")
public class ActionAndSessionTest extends BaseTest {

    @Test
    @Story("Выполнение действия")
    @DisplayName("Успешное выполнение ACTION для предварительно авторизованного токена")
    void shouldExecuteActionForLoggedInUser() {
        String token = generateToken();

        Allure.step("Предварительный вход: мок /auth отвечает 200, выполняем LOGIN", () -> {
            WireMockHelper.stubAuth(200);
            appClient.sendRequest(token, Action.LOGIN);
        });

        Allure.step("Настроить мок внешнего сервиса: /doAction возвращает 200 OK", () ->
                WireMockHelper.stubDoAction(200));

        Response response = Allure.step("Отправить запрос ACTION с авторизованным токеном", () ->
                appClient.sendRequest(token, Action.ACTION));

        Allure.step("Проверить, что приложение вернуло результат OK", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("OK");
        });

        Allure.step("Убедиться, что внешний сервис /doAction получил запрос с токеном", () ->
                WireMockHelper.verifyDoActionCalled(token, 1));
    }

    @Test
    @Story("Выполнение действия")
    @DisplayName("Отказ в ACTION для токена, который не прошел LOGIN")
    void shouldRejectActionForNonLoggedInUser() {
        String token = generateToken();

        Response response = Allure.step("Отправить запрос ACTION для неавторизованного токена", () ->
                appClient.sendRequest(token, Action.ACTION));

        Allure.step("Проверить, что приложение вернуло ERROR", () -> {
            ApiResponse apiResponse = response.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("ERROR");
        });

        Allure.step("Убедиться, что запрос к внешнему сервису /doAction НЕ отправлялся", () ->
                WireMockHelper.verifyDoActionCalled(token, 0));
    }

    @Test
    @Story("Завершение сессии")
    @DisplayName("После LOGOUT токен удаляется и выполнение ACTION блокируется")
    void shouldInvalidateSessionAfterLogout() {
        String token = generateToken();

        Allure.step("1. Вход в систему (LOGIN)", () -> {
            WireMockHelper.stubAuth(200);
            appClient.sendRequest(token, Action.LOGIN);
        });

        Allure.step("2. Выход из системы (LOGOUT)", () -> {
            Response logoutResponse = appClient.sendRequest(token, Action.LOGOUT);
            ApiResponse apiResponse = logoutResponse.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("OK");
        });

        Allure.step("3. Попытка выполнить ACTION после LOGOUT", () -> {
            Response actionResponse = appClient.sendRequest(token, Action.ACTION);
            ApiResponse apiResponse = actionResponse.as(ApiResponse.class);
            assertThat(apiResponse.getResult()).isEqualTo("ERROR");
        });

        Allure.step("Убедиться, что внешний сервис /doAction не вызывался после логаута", () ->
                WireMockHelper.verifyDoActionCalled(token, 0));
    }
}