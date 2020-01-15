package ru.otus.rzdtelegrambot.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.otus.rzdtelegrambot.model.TrainStation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**Позволяет получить код станции по ее названию.
 *
 * @author Sergei Viacheslaev
 */
@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StationCodeService {
    final String STATIONCODE_REQUEST_TEMPLATE = "https://pass.rzd.ru/suggester?stationNamePart={stationNamePart}&lang=ru";
    private RestTemplate restTemplate;
    private Map<String, Integer> stationCodeCache = new HashMap<>();

    public StationCodeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int getStationCode(String stationName) {
        String stationNameParam = stationName.toUpperCase();

        Integer stationCode = stationCodeCache.get(stationNameParam);
        if (stationCode != null) return stationCode;

        processStationCodeRequest(stationNameParam);

        stationCode = stationCodeCache.get(stationNameParam);

        if (stationCode != null) {
            return stationCode;
        } else {
            return -1;
        }
    }

    private void processStationCodeRequest(String stationNamePart) {
        ResponseEntity<TrainStation[]> response =
                restTemplate.getForEntity(
                        STATIONCODE_REQUEST_TEMPLATE,
                        TrainStation[].class, stationNamePart);
        TrainStation[] stations = response.getBody();

        log.info("Stations {}", Arrays.toString(stations));
        for (TrainStation station : stations) {
            stationCodeCache.put(station.getStationName(), station.getStationCode());
        }
    }
}
