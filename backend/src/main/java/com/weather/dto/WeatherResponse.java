package com.weather.dto;

import lombok.Data;

@Data
public class WeatherResponse {
  private Main main;

  @Data
  public static class Main {
    private String temp;
  }
}
