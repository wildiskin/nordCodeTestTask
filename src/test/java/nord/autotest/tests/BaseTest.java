package nord.autotest.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import nord.autotest.client.AppClient;
import nord.autotest.config.TestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

public abstract class BaseTest {

    protected static WireMockServer wireMockServer;
    protected AppClient appClient = new AppClient();

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(TestConfig.WIREMOCK_PORT));
        wireMockServer.start();
        WireMock.configureFor("localhost", TestConfig.WIREMOCK_PORT);
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void resetMocks() {
        WireMock.reset();
    }

    // Генерация валидного токена (32 символа, A-Z0-9)
    protected String generateToken() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}