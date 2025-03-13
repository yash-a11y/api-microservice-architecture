package com.yash.api_virgin.service;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanScraperService {
    public static List<Map<String, String>> scrapePlans() throws InterruptedException {
        List<Map<String, String>> plansList = new ArrayList<>();
        // Set ChromeOptions to enable headless mode
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");  // Enable headless mode
        options.addArguments("--window-size=1920,1080");  // Set a window size for consistent rendering
        WebDriver driver = new ChromeDriver(options);  // Initiate ChromeDriver with options

        try {
            // Navigate to the Virgin Plus plans page
            driver.get("https://www.virginplus.ca/en/plans/postpaid.html#!/BYOP/research");

            // Wait for the page to load (use WebDriverWait or Thread.sleep as needed)
            Thread.sleep(5000);

            // Find and iterate over each plan element
            List<WebElement> plans = driver.findElements(By.cssSelector(".planInner.tb"));
            for (WebElement plan : plans) {
                Map<String, String> planData = new HashMap<>();

                // Extract the plan name, details, and price
                String planName = plan.findElement(By.cssSelector(".planHeading span")).getText();
                List<WebElement> detailsElements = plan.findElements(By.cssSelector(".tb.attributeList .tr .tc.ng-binding"));

                // Collect plan details
                StringBuilder details = new StringBuilder();
                for (WebElement detail : detailsElements) {
                    details.append(detail.getText()).append(", ");
                }

                if (details.length() > 0) {
                    details.setLength(details.length() - 2); // Remove the trailing comma and space
                }

                // Extract the plan price
                String price = plan.findElement(By.cssSelector(".planPriceContainer .ultra")).getText();
                String numericPrice = price.replaceAll("[^\\d]","");

                if(planName.isEmpty() || numericPrice.isEmpty()){
                    continue;
                }

                // Store the extracted data
                planData.put("planname", planName);
                planData.put("details", details.toString());
                planData.put("planprice", numericPrice);

                plansList.add(planData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit(); // Close the browser after scraping
        }
        return plansList;
    }
}
