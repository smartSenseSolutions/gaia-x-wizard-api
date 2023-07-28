package eu.gaiax.wizard.core.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CommonService {
    public String getCurrentFormattedDate(){
        Instant currentInstant = Instant.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String time = ZonedDateTime.ofInstant(currentInstant, ZoneOffset.UTC).format(formatter);
        return time;
    }
}
