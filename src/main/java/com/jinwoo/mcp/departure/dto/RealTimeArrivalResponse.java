package com.jinwoo.mcp.departure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RealTimeArrivalResponse {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "row")
    private List<Row> row;

    public List<Row> getRow() { return row; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Row {
        @JacksonXmlProperty(localName = "statnNm")
        private String statnNm;

        @JacksonXmlProperty(localName = "trainLineNm")
        private String trainLineNm;

        @JacksonXmlProperty(localName = "updnLine")
        private String updnLine;

        @JacksonXmlProperty(localName = "subwayId")
        private String subwayId;

        @JacksonXmlProperty(localName = "barvlDt")
        private String barvlDt;

        public String getStatnNm() { return statnNm; }
        public String getTrainLineNm() { return trainLineNm; }
        public String getUpdnLine() { return updnLine; }
        public String getSubwayId() { return subwayId; }
        public String getBarvlDt() { return barvlDt; }
    }

}
