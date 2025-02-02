package com.weather.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@org.springframework.context.annotation.Configuration
public class Configuration {

  @Bean
  public WebClient webClient() {
    return WebClient.create("https://api.openweathermap.org/data/2.5/");
  }
}
