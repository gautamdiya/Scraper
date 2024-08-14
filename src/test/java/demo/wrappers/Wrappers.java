package demo.wrappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.time.Duration;

public class Wrappers {
    /*
     * Write your selenium wrappers here
     */

    public static void navigateToWebsite(ChromeDriver driver, String url) {
        try {
            driver.get(url);
            String pageUrl = driver.getCurrentUrl();
            if (pageUrl.contains("scrape")) {
                System.out.println("Navigation is successfull");
            } else {
                System.out.println("Navigatin Failed");
            }
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    /*
     * Write your selenium wrappers here
     */
    public static void selectPage(ChromeDriver driver, String pageName) {
        try {
            WebElement pageElement = driver
                    .findElement(By.xpath("//li[contains(@id,'" + pageName.toLowerCase() + "')]"));
            pageElement.click();
            System.out.println("Page is opened successfully");
            Thread.sleep(1000);

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    public static void headingSelection(ChromeDriver driver, String headingName) {
        try {
            WebElement headingElement = driver
                    .findElement(By.xpath("//div[@class='page']//a[contains(text(),'" + headingName + "')]"));
            System.out.println(headingElement.getText());
            headingElement.click();
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }
    }

    public static void teamData(ChromeDriver driver) throws Exception {
        try {

            ArrayList<HashMap<String, Object>> teamData = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                List<WebElement> rows = driver.findElements(By.xpath("//tr[@class='team']"));
                for (WebElement row : rows) {
                    String teamName = row.findElement(By.xpath("./td[@class='name']")).getText();
                    String year = row.findElement(By.xpath("./td[@class='year']")).getText();
                    String winPercentageStr = row.findElement(By.xpath("./td[contains(@class, 'pct')]")).getText();
                    double winPercent = Double.parseDouble(winPercentageStr);
                    if (winPercent < 0.40) {
                        HashMap<String, Object> team = new HashMap<>();
                        team.put("epochTime", Instant.now().getEpochSecond());
                        team.put("TeamName", teamName);
                        team.put("year", year);
                        team.put("winPercentage", winPercent);
                        teamData.add(team);
                    }
                }
                if (i < 4) {
                    driver.findElement(By.xpath("//a[@aria-label='Next']")).click();
                    Thread.sleep(2000); // Adding a small delay to wait for the next page to load
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            String fileName = "src/test/resources/hockey-team-data.json";
            mapper.writeValue(new File(fileName), teamData);

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
        }

    }

    public static void scrapeOscarData(ChromeDriver driver) throws Exception {
        ArrayList<HashMap<String, Object>> movieData = new ArrayList<>();
        List<WebElement> years = driver.findElements(By.xpath("//a[@class='year-link']"));

        for (WebElement yearElement : years) {
            String year = yearElement.getText();
            yearElement.click();

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // Wait until rows are present
            wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//tr[@class='film']")));

            List<WebElement> rows = driver.findElements(By.xpath("//tr[@class='film']"));
            for (int i = 0; i < Math.min(5, rows.size()); i++) {
                WebElement row = rows.get(i);
                try {
                    String title = row.findElement(By.xpath(".//td[@class='film-title']")).getText();
                    System.out.println(title);
                    String nominations = row.findElement(By.xpath(".//td[@class='film-nominations']")).getText();
                    System.out.println(nominations);
                    String awards = row.findElement(By.xpath(".//td[@class='film-awards']")).getText();
                    System.out.println(awards);
                    boolean isWinner = row
                            .findElements(
                                    By.xpath(".//td[@class='film-best-picture']/i[contains(@class, 'glyphicon-flag')]"))
                            .isEmpty();
                    boolean isWinnerStatus = !isWinner;
                    System.out.println(isWinnerStatus);

                    HashMap<String, Object> movie = new HashMap<>();
                    movie.put("epochTime", Instant.now().getEpochSecond());
                    movie.put("year", year);
                    movie.put("title", title);
                    movie.put("nominations", nominations);
                    movie.put("awards", awards);
                    movie.put("isWinner", isWinnerStatus);

                    movieData.add(movie);
                } catch (org.openqa.selenium.StaleElementReferenceException e) {
                    // Handle stale element reference by retrying or logging
                    System.err.println("Stale element reference: " + e.getMessage());
                }
            }

            // Navigate back to the year list
            driver.navigate().back();
        }

        ObjectMapper mapper = new ObjectMapper();
        String filePath = "src/test/resources/oscar-winner-data.json";
        File outputFile = new File(filePath);
        outputFile.getParentFile().mkdirs(); // Create directories if not exist
        mapper.writeValue(outputFile, movieData);
    }

    public static void verifyFile() {
        String filePath = "src/test/resources/oscar-winner-data.json";
        File file = new File(filePath);
        Assert.assertTrue(file.exists(), "File does not exist");
        Assert.assertTrue(file.length() > 0, "File is empty");
    }
}