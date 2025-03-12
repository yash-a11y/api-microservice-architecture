package com.yash.api_rogers.service;



import com.yash.api_rogers.models.Plan;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class WebScraperService {

    private WebDriver initializeWebDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        // Add performance optimization arguments
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--blink-settings=imagesEnabled=false"); // Disable images
        options.addArguments("--disk-cache-size=0"); // Disable disk cache
        options.setPageLoadStrategy(PageLoadStrategy.EAGER); // Use EAGER load strategy
        return new ChromeDriver(options);
    }

    public List<Plan> scrapePlans() {
        List<Plan> allPlans = new ArrayList<>();
        
        // Create threads for parallel scraping
        Thread fiveGThread = new Thread(() -> {
            WebDriver driver = initializeWebDriver();
            driver.manage().window().maximize();
            try {
                List<Plan> fiveGPlans = scrapePlans(driver, "5G Mobile Plan", "/plans?icid=R_WIR_CMH_6WMCMZ");
                synchronized (allPlans) {
                    allPlans.addAll(fiveGPlans);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.quit();
            }
        });

        Thread prepaidThread = new Thread(() -> {
            WebDriver driver = initializeWebDriver();
            driver.manage().window().maximize();
            try {
                List<Plan> prepaidPlans = scrapePlans(driver, "Prepaid Plan", "/plans/prepaid?icid=R_WIR_CMH_EIZF9L");
                synchronized (allPlans) {
                    allPlans.addAll(prepaidPlans);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                driver.quit();
            }
        });

        // Start both threads
        fiveGThread.start();
        prepaidThread.start();

        // Wait for both threads to complete
        try {
            fiveGThread.join();
            prepaidThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return allPlans;
    }

    private void navigateToPlansPage(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        // Click on "Mobile"
        WebElement mobileLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("geMainMenuDropdown_0")));
        mobileLink.click();

        // Click on "Plans"
        WebElement plansLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[aria-label='Plans']")));
        plansLink.click();

        // Wait briefly to ensure the plans page loads
        Thread.sleep(2000);
    }

    private void navigateToPhonesPage(WebDriver driver, WebDriverWait wait) throws InterruptedException {
        // Click on "Mobile"
        WebElement mobileLink = wait.until(ExpectedConditions.elementToBeClickable(By.id("geMainMenuDropdown_0")));
        mobileLink.click();

//        WebElement phoneLink = wait.until(ExpectedConditions.elementToBeClickable(
//                By.cssSelector("a[href='/phones/?icid=R_WIR_CMH_GJJPYK']")));
//        phoneLink.click();
        // Click on "Phones"
        WebElement phoneLink = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[aria-label='Phones']")));
        phoneLink.click();

        // Wait briefly to ensure the page loads
        Thread.sleep(2000);
    }

    private List<Plan> scrapePlans(WebDriver driver, String planType, String planUrl) throws InterruptedException {
        List<Plan> plans = new ArrayList<>();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        
        int maxRetries = 3;
        int retryCount = 0;
        while (retryCount < maxRetries) {
            try {
                driver.get("https://www.rogers.com" + planUrl);
                
                // Wait for page load
                wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
                
                // Wait for any dynamic content to load
                Thread.sleep(3000);
                
                // Try multiple selectors to find plan elements
                List<WebElement> planElements = null;
                String[] possibleSelectors = {
                    "dsa-vertical-tile__top",           // Original selector
                    "dsa-vertical-tile",               // Alternative selector
                    "wireless-plan-tile",              // Another possible selector
                    "plan-card"                        // Generic plan card selector
                };
                
                for (String selector : possibleSelectors) {
                    try {
                        // First try to find elements
                        planElements = driver.findElements(By.className(selector));
                        if (!planElements.isEmpty()) {
                            System.out.println("Found elements using selector: " + selector);
                            break;
                        }
                        
                        // If no elements found, try CSS selector
                        planElements = driver.findElements(By.cssSelector("." + selector));
                        if (!planElements.isEmpty()) {
                            System.out.println("Found elements using CSS selector: ." + selector);
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
                
                if (planElements == null || planElements.isEmpty()) {
                    // Try to find any plan-related content
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String script = 
                        "return Array.from(document.querySelectorAll('*')).find(el => {" +
                        "   const text = el.textContent.toLowerCase();" +
                        "   return (text.includes('plan') || text.includes('package')) &&" +
                        "          (el.querySelector('[class*=\"price\"]') || el.querySelector('[class*=\"amount\"]'));" +
                        "});";
                    
                    WebElement planContainer = (WebElement) js.executeScript(script);
                    if (planContainer != null) {
                        planElements = planContainer.findElements(By.xpath(".//*[contains(@class, 'tile') or contains(@class, 'card')]"));
                    }
                }
                
                if (planElements == null || planElements.isEmpty()) {
                    throw new NoSuchElementException("Could not find any plan elements on the page");
                }

                // Process found elements
                for (WebElement planElement : planElements) {
                    try {
                        // Try multiple selectors for plan name
                        String planName = findTextByMultipleSelectors(planElement, 
                            new String[]{"dsa-vertical-tile__heading", "plan-name", "title"});
                        
                        // Try multiple selectors for price
                        String price = findTextByMultipleSelectors(planElement, 
                            new String[]{"ds-price__amountDollars", "price", "amount"});
                        
                        // Build plan details
                        StringBuilder planDetails = new StringBuilder();
                        List<WebElement> detailElements = null;
                        
                        // Try multiple selectors for details
                        String[] detailSelectors = {
                            "p.dsa-vertical-tile__highlightBody ul>li",
                            "dsa-vertical-tile__highlight",
                            ".plan-features li",
                            ".details li"
                        };
                        
                        for (String selector : detailSelectors) {
                            try {
                                detailElements = planElement.findElements(By.cssSelector(selector));
                                if (!detailElements.isEmpty()) break;
                            } catch (Exception e) {
                                continue;
                            }
                        }
                        
                        if (detailElements != null) {
                            for (WebElement detail : detailElements) {
                                String detailText = detail.getText().trim();
                                if (!detailText.isEmpty()) {
                                    planDetails.append(detailText).append("; ");
                                }
                            }
                        }
                        
                        if (planName != null && price != null) {
                            plans.add(new Plan(planType, planName, planDetails.toString().replace(",", ";"), price));
                        }
                        
                    } catch (Exception e) {
                        System.out.println("Error processing plan element: " + e.getMessage());
                        continue;
                    }
                }
                
                if (!plans.isEmpty()) {
                    break;
                }
                
                retryCount++;
                if (retryCount < maxRetries) {
                    System.out.println("Retry " + retryCount + " of " + maxRetries);
                    driver.navigate().refresh();
                    Thread.sleep(2000);
                }
                
            } catch (Exception e) {
                System.out.println("Error during scraping: " + e.getMessage());
                retryCount++;
                if (retryCount == maxRetries) throw e;
                driver.navigate().refresh();
                Thread.sleep(2000);
            }
        }

        return plans;
    }

    private String findTextByMultipleSelectors(WebElement element, String[] selectors) {
        for (String selector : selectors) {
            try {
                // Try class name
                WebElement foundElement = element.findElement(By.className(selector));
                String text = foundElement.getText().trim();
                if (!text.isEmpty()) return text;
            } catch (Exception e) {
                try {
                    // Try CSS selector
                    WebElement foundElement = element.findElement(By.cssSelector("." + selector));
                    String text = foundElement.getText().trim();
                    if (!text.isEmpty()) return text;
                } catch (Exception e2) {
                    continue;
                }
            }
        }
        return null;
    }

//    private List<Phone> scrapeSmartphones(WebDriver driver, WebDriverWait wait) throws InterruptedException {
//        List<Phone> phones = new ArrayList<>();
//
//        // Scroll to the bottom to trigger lazy loading
//        JavascriptExecutor js = (JavascriptExecutor) driver;
//        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
//
//        // Wait for smartphones section to load
//        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dsa-nacTile")));
//
//        // Define selectors
//        String phoneTileClass = "dsa-nacTile";
//        String phoneNameClass = "text-title-5";
//        String priceCss = "div.ds-price";
//        String conditionClass = "text-body-sm";
//        String fullPriceCss = "p.text-body-sm.mb-0.text-semi";
//        String imageXpath = ".//picture/img"; // Relative XPath
//
//        List<WebElement> phoneElements = driver.findElements(By.className(phoneTileClass));
//
//        for (WebElement phoneElement : phoneElements) {
//            try {
//                String phoneName = phoneElement.findElement(By.className(phoneNameClass)).getText();
//                String price = phoneElement.findElement(By.cssSelector(priceCss)).getAttribute("aria-label");
//                String condition = phoneElement.findElement(By.className(conditionClass)).getText();
//                condition = condition.contains("Save & Return") ? "Save & Return" : "Pay with the Bill";
//                String fullPrice = phoneElement.findElement(By.cssSelector(fullPriceCss)).getText().replaceAll("[^\\d.]", "");
//
//                // Extract image URL
//                WebElement imgElement = phoneElement.findElement(By.xpath(imageXpath));
//                js.executeScript("arguments[0].scrollIntoView(true);", imgElement);
//                String imageUrl = imgElement.getAttribute("src");
//
//                Phone phone = new Phone(phoneName, price, condition, "$" + fullPrice, imageUrl);
//                phones.add(phone);
//            } catch (NoSuchElementException e) {
//                // Handle missing elements if necessary
//                e.printStackTrace();
//            }
//        }
//
//        return phones;
//    }
}
