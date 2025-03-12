package com.yash.api_koodo.services;

import com.yash.api_koodo.model.KoodoPlan;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class KoodoScraperService {

    private WebDriver driver;

    @Autowired
    private redisService redisService;

    // Initialize the WebDriver
    private void initializeDriver() {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/134.0.0.0 Safari/537.36");
            this.driver = new ChromeDriver(options);
        }
    }

    // Shutdown WebDriver
    public void shutdownDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    private void handleCookieConsent(WebDriverWait wait) {
        try {
            // Wait for the cookie consent banner
            WebElement consentBanner = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("onetrust-banner-sdk")));
            
            // Try to find and click the accept button
            WebElement acceptButton = consentBanner.findElement(By.id("onetrust-accept-btn-handler"));
            if (acceptButton != null && acceptButton.isDisplayed()) {
                acceptButton.click();
                // Wait for banner to disappear
                wait.until(ExpectedConditions.invisibilityOf(consentBanner));
            }
        } catch (Exception e) {
            System.out.println("Cookie consent banner not found or already accepted: " + e.getMessage());
        }
    }

    private void waitForElementToBeClickable(WebDriverWait wait, WebElement element) {
        try {
            // Scroll element into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
            // Add a small delay to let the page settle
            Thread.sleep(500);
            // Wait for element to be clickable
            wait.until(ExpectedConditions.elementToBeClickable(element));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<KoodoPlan> scrapePrepaidPlans() {
        List<KoodoPlan> plans = new ArrayList<>();
        initializeDriver();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            
            // Navigate to the Koodo Mobile prepaid plans page
            driver.get("https://www.koodomobile.com/en/prepaid-plans");
            
            // Handle cookie consent first
            handleCookieConsent(wait);

            // Wait for the prepaid plans to load
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".app-container")));

            // Extracting plan types from tabs
            List<WebElement> tabs = driver.findElements(By.cssSelector(".KDS_Tabs-modules__tabButton___1RuCu"));
            
            for (WebElement tab : tabs) {
                try {
                    String planType = tab.getText();
                    System.out.println("Processing tab: " + planType);

                    // Make sure tab is clickable before clicking
                    waitForElementToBeClickable(wait, tab);
                    tab.click();
                    
                    // Wait for content to load after tab click
                    Thread.sleep(1000);

                    // Extract based on plan type
                    switch (planType) {
                        case "4G Base Plans":
                            plans.addAll(extract4GBasePlans(planType));
                            break;
                        case "360 Day Plans":
                            plans.addAll(extract360DayPlans(planType));
                            break;
                        case "Booster Add-ons":
                            plans.addAll(extractBoosterAddons(planType));
                            break;
                        default:
                            System.out.println("No specific extraction logic for: " + planType);
                            break;
                    }
                } catch (Exception e) {
                    System.out.println("Error processing tab: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error during scraping: " + e.getMessage());
            e.printStackTrace();
        }

        return plans;
    }

    private List<KoodoPlan> extract4GBasePlans(String planType) {
        List<KoodoPlan> plans = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".KDS_Item-modules__item___1fILq")));
            List<WebElement> planCards = driver.findElements(By.cssSelector(".KDS_Item-modules__item___1fILq"));
            
            for (WebElement planCard : planCards) {
                try {
                    WebElement detailsElement = planCard.findElement(By.cssSelector(".KDS_Item-modules__children__wrapper___3a1Xn"));
                    String planDetails = detailsElement.getText();

                    String[] lines = planDetails.split("\n");
                    if (lines.length >= 4) {
                        String price = lines[1].trim();
                        String validity = lines[3].trim();
                        String details = String.join(", ", List.of(lines).subList(4, lines.length));

                        KoodoPlan plan = new KoodoPlan(planType, validity, details, price);
                        plans.add(plan);
                        System.out.println("Extracted plan: " + plan);
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting plan card: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting 4G base plans: " + e.getMessage());
        }

        return plans;
    }

    private List<KoodoPlan> extract360DayPlans(String planType) {
        return extract4GBasePlans(planType);
    }

    private List<KoodoPlan> extractBoosterAddons(String planType) {
        List<KoodoPlan> plans = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector(".KDS_Item-modules__item___1fILq")));
            List<WebElement> planCards = driver.findElements(By.cssSelector(".KDS_Item-modules__item___1fILq"));

            for (WebElement planCard : planCards) {
                try {
                    WebElement detailsElement = planCard.findElement(By.cssSelector(".KDS_Item-modules__children__wrapper___3a1Xn"));
                    String planDetails = detailsElement.getText();

                    String[] lines = planDetails.split("\n");
                    if (lines.length >= 2) {
                        String price = lines[1].trim();
                        String details = lines.length > 2 ? lines[2].trim() : "Details not available";

                        KoodoPlan plan = new KoodoPlan(planType, "NA", details, price);
                        plans.add(plan);
                        System.out.println("Extracted booster addon: " + plan);
                    }
                } catch (Exception e) {
                    System.out.println("Error extracting booster addon: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting booster addons: " + e.getMessage());
        }

        return plans;
    }
}
