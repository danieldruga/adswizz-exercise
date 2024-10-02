package com.adswizz.test;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDownloadedData {
    private static List<JSONObject> testData;
    private static JSONObject expected;

    @BeforeAll
    public static void loadData() {
        Util instance = new Util();
        testData = instance.readTestDataFile("downloads.txt");
        expected = new JSONObject(instance.readExpectedDataFile("expected.json"));
    }

    @Test
    @Order(1)
    public void checkMostPopularShow(){
        JSONObject actualResult = Util.getMostPopularShow(testData);

        assertEquals(expected.getJSONObject("test1").get("showName"), actualResult.get("showName"));
        System.out.printf("Most popular show is: %s%n", actualResult.get("showName"));
        assertEquals(expected.getJSONObject("test1").get("downloads"), actualResult.getInt("downloads"));
        System.out.printf("Number of downloads is: %2d%n", actualResult.getInt("downloads"));
    }

    @Test
    @Order(2)
    public void checkMostUsedDevice(){
        JSONObject actualResult = Util.getMostUsedDevice(testData);

        assertEquals(expected.getJSONObject("test2").get("deviceType"), actualResult.get("deviceType"));
        System.out.printf("Most popular device is: %s%n", actualResult.get("deviceType"));
        assertEquals(expected.getJSONObject("test2").get("downloads"), actualResult.getInt("downloads"));
        System.out.printf("Number of downloads is: %2d%n", actualResult.getInt("downloads"));
    }

    @Test
    @Order(3)
    public void checkPreRollOpportunities(){
        List<JSONObject> actualResult = Util.getPrerollOpportunities(testData);
        JSONArray expectedResult = expected.getJSONArray("test3");

        assertEquals(expectedResult.length(), actualResult.size());
        for (int i = 0; i < expectedResult.length(); i++){
            assertEquals(expectedResult.getJSONObject(i).get("showName"), actualResult.get(i).get("showName"));
            assertEquals(expectedResult.getJSONObject(i).getInt("prerollOpportunities"), actualResult.get(i).getInt("prerollOpportunities"));
            System.out.printf("Show Id: %s, Preroll Opportunity Number: %2d%n", actualResult.get(i).get("showName"), actualResult.get(i).getInt("prerollOpportunities"));
        }
    }

    @Test
    @Order(4)
    public void checkWeeklyShows(){
        List<JSONObject> actualResult = Util.getWeeklyShows(testData);
        JSONArray expectedResult = expected.getJSONArray("test4");

        assertEquals(expectedResult.length(), actualResult.size());
        for (int i = 0; i < expectedResult.length(); i++){
            assertEquals(expectedResult.getJSONObject(i).get("showName"), actualResult.get(i).get("showName"));
            assertEquals(expectedResult.getJSONObject(i).get("releaseInfo"), actualResult.get(i).get("releaseInfo").toString());
            System.out.printf("%s - %s%n", actualResult.get(i).get("showName"), actualResult.get(i).get("releaseInfo").toString());
        }
    }
}
