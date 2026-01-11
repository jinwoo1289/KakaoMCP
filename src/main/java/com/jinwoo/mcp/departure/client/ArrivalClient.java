package com.jinwoo.mcp.departure.client;
import java.util.*;

public interface ArrivalClient {
    List<Integer> getRemainingMinutes(String station, String line);
}
