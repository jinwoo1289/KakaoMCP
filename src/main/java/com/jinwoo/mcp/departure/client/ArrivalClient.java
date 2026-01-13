package com.jinwoo.mcp.departure.client;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public interface ArrivalClient {
    List<Integer> getRemainingMinutes(String station, String line, String direction);
}
