package com.adswizz.test;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
    public List<JSONObject> readTestDataFile(String fileName) {
        InputStream is = this.getFileAsIOStream(fileName);
        List<JSONObject> myList = new ArrayList<>();
        try (InputStreamReader isr = new InputStreamReader(is);
             BufferedReader br = new BufferedReader(isr))
        {
            String line;
            while ((line = br.readLine()) != null) {
                myList.add(new JSONObject(line));
            }
            is.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return myList;
    }

    public String readExpectedDataFile(String fileName){
        InputStream is = this.getFileAsIOStream(fileName);
        try (InputStreamReader isr = new InputStreamReader(is);
             BufferedReader reader = new BufferedReader(isr)) {
            return reader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getFileAsIOStream(final String fileName) {
        InputStream ioStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(fileName);

        if (ioStream == null) {
            throw new IllegalArgumentException(fileName + " is not found");
        }
        return ioStream;
    }

    public static JSONObject getMostPopularShow(List<JSONObject> listOfDownloads) {
        List<String> listOfShows = getListOfShows(listOfDownloads);
        List<JSONObject> viewsPerShow = new ArrayList<>();
        listOfShows.forEach(show -> viewsPerShow.add(new JSONObject()
                .put("showName", show)
                .put("downloads", listOfDownloads.stream()
                    .filter(item->item.getJSONObject("downloadIdentifier").getString("showId").equals(show))
                    .filter(item -> item.get("city").equals("san francisco"))
                    .count())));
        return viewsPerShow.stream().max(Comparator.comparingInt(item-> item.getInt("downloads"))).get();
    }

    public static JSONObject getMostUsedDevice(List<JSONObject> listOfDownloads) {
        List<JSONObject> listOfDeviceOptions = listOfDownloads.stream()
                .map(item -> new JSONObject().put("deviceType", item.get("deviceType").toString()))
                .distinct()
                .toList();
        listOfDeviceOptions.forEach(device -> device.put("downloads", listOfDownloads.stream()
                .filter(item -> item.get("deviceType").equals(device.get("deviceType")))
                .count()));
        return listOfDeviceOptions.stream().max(Comparator.comparingInt(item -> item.getInt("downloads"))).get();
    }

    public static List<JSONObject> getPrerollOpportunities(List<JSONObject> listOfDownloads) {
        List<String> listOfShows = getListOfShows(listOfDownloads);
        List<JSONObject> finalList = new ArrayList<>();
        listOfShows.forEach(show -> {
            List<JSONArray> opportunitiesPerDownload = listOfDownloads.stream()
                    .filter(item -> item.getJSONObject("downloadIdentifier").get("showId").equals(show))
                    .map(item -> item.getJSONArray("opportunities"))
                    .toList();

            List<JSONObject> opportunitiesForShow = new ArrayList<>();
            for (JSONArray opportunities : opportunitiesPerDownload) {
                for (int i = 0; i < opportunities.length(); i++) {
                    JSONArray values = opportunities.getJSONObject(i).getJSONObject("positionUrlSegments").getJSONArray("aw_0_ais.adBreakIndex");
                    for (int j = 0; j < values.length(); j++) {
                        if (values.getString(j).equals("preroll")) {
                            opportunitiesForShow.add(opportunities.getJSONObject(i));
                            break;
                        }
                    }

                }
            }
            finalList.add(new JSONObject().put("showName", show).put("prerollOpportunities", opportunitiesForShow.size()));
        });
        Comparator<JSONObject> compare = (item1, item2) -> Integer.compare(item2.getInt("prerollOpportunities"),(item1.getInt("prerollOpportunities")));
        return finalList.stream().sorted(compare.thenComparing(item -> item.getString("showName"))).toList();
    }

    public static List<JSONObject> getWeeklyShows(List<JSONObject> listOfDownloads) {
        List<String> listOfShows = getListOfShows(listOfDownloads);
        List<JSONObject> finalList = new ArrayList<>();
        listOfShows.forEach(show->{
            List<Long> times = listOfDownloads.stream()
                    .filter(item->item.getJSONObject("downloadIdentifier").get("showId").equals(show))
                    .map(item->item.getJSONArray("opportunities").getJSONObject(0).getLong("originalEventTime"))
                    .distinct().sorted().toList();

            if(isShowWeekly(times)){
                Timestamp ts = new Timestamp(times.getFirst());
                ZonedDateTime z = ZonedDateTime.ofInstant(ts.toInstant(), ZoneId.of("UTC"));
                finalList.add(new JSONObject()
                        .put("showName", show)
                        .put("releaseInfo", z.format(DateTimeFormatter.ofPattern("E HH:mm"))));
            }
        });
        return finalList.stream().sorted(Comparator.comparing(item -> item.getString("showName"))).toList();
    }

    private static List<String> getListOfShows(List<JSONObject> listOfDownloads) {
        return listOfDownloads.stream()
                .map(item -> item.getJSONObject("downloadIdentifier").get("showId").toString())
                .distinct()
                .toList();
    }

    private static boolean isShowWeekly(List<Long> showTimes){
        long highValue = showTimes.getLast();
        boolean isWeekly = true;
        for (Long showTime : showTimes) {
            double res = (double) (highValue - showTime) / 604800;
            if (res % 1 != 0) {
                isWeekly = false;
                break;
            }
        }

        return isWeekly;
    }
}
