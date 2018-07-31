package com.boclips.e2e

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.test.context.junit4.SpringRunner
import javax.annotation.PreDestroy


@RunWith(SpringRunner::class)
@SpringBootTest
class E2eApplicationTests {

    @Autowired
    lateinit var loginPage: LoginPage
    @Autowired
    lateinit var homePage: HomePage

    @Test
    @Ignore("sample test")
    fun boclips_pearson_loads() {
        loginPage.go()
        loginPage.login("slim-pearson-staging-vendor", "Passw0rd")

        await().untilAsserted {
            assertThat(homePage.getCollections()).hasSize(9)
        }
    }
}

@Component
class WebDriverFactory {

    var driver: WebDriver = ChromeDriver()

    @PreDestroy
    fun killDriver() {
        driver.quit()
    }
}

@Configuration
class WebDriverConfig(val webDriverSingleton: WebDriverFactory) {

    @Bean
    fun webDriver() = webDriverSingleton.driver
}

@Component
class HomePage(val driver: WebDriver) {

    fun getCollections() = driver.findElements(By.cssSelector(".card"))
            .map { _ -> Collection("not-implemented") }

    data class Collection(val name: String)

}

@Component
class LoginPage(val driver: WebDriver) {

    fun login(username: String, password: String) {
        driver.findElement(By.cssSelector("input[type=text]")).sendKeys(username)
        driver.findElement(By.cssSelector("input[type=password]")).sendKeys(password)
        driver.findElement(By.cssSelector("button[type=submit]")).click()
    }

    fun go() {
        driver.get("http://pearson-staging.boclips.com/login")
    }
}