package com.weather.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.domain.City;
import com.weather.dto.CityDTO;
import com.weather.dto.WeatherResponse;
import com.weather.repository.CityRepository;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityUpdateService {

  private static final Duration TIMEOUT = Duration.ofSeconds(10);
  private static final int MAX_CONCURRENT_REQUESTS = 50;
  public static final String CITY_LIST_JSON_LOCATION = "static/city.list.json";

  private final CityRepository cityRepository;
  private final CityService cityService;
  private final ObjectMapper objectMapper;
  private final WebClient webClient;
  private final Resource citiesResource = new ClassPathResource(CITY_LIST_JSON_LOCATION);

  @Value("${api-key}")
  private String apiKey;

  @PostConstruct
  public void init() {
    initCities();
  }

  @CacheEvict(
      value = {"cities"},
      allEntries = true)
  @Scheduled(cron = "0 0 0 * * *") // Every night
  @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000))
  public void dailyUpdate() {
    log.debug("Daily update of city temperatures");
    List<City> cities = cityRepository.findAll();
    updateTemperatures(
        cities.stream()
            .map(city -> CityDTO.builder().id(city.getId()).name(city.getName()).build())
            .toList());
  }

  public List<CityDTO> loadCities() {
    try (InputStream inputStream = citiesResource.getInputStream()) {
      return objectMapper.readValue(inputStream, new TypeReference<>() {});
    } catch (IOException e) {
      throw new RuntimeException("Failed to load cities", e);
    }
  }

  public void initCities() {
    List<CityDTO> cities = loadCities();
    Collections.shuffle(cities);
    cities = cities.subList(0, 100);

    updateTemperatures(cities);
  }

  @Async
  public Mono<String> fetchTemperatureAsync(Long cityId) {
    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/weather")
                    .queryParam("id", cityId)
                    .queryParam("appid", apiKey)
                    .build())
        .retrieve()
        .bodyToMono(WeatherResponse.class)
        .timeout(TIMEOUT)
        .map(response -> response.getMain().getTemp())
        .onErrorResume(
            e -> {
              log.error(
                  "Failed to fetch temperature for city {}: {}, {}",
                  cityId,
                  e.getMessage(),
                  apiKey);
              return Mono.empty();
            });
  }

  public void updateTemperatures(List<CityDTO> cities) {
    Flux.fromIterable(cities)
        .parallel(MAX_CONCURRENT_REQUESTS)
        .runOn(Schedulers.boundedElastic())
        .flatMap(
            city ->
                fetchTemperatureAsync(city.getId())
                    .flatMap(
                        temp ->
                            Mono.just(
                                City.builder()
                                    .id(city.getId())
                                    .name(city.getName())
                                    .temperature(temp)
                                    .build())))
        .sequential()
        .collectList()
        .flatMap(
            updatedCities -> {
              log.info("Updating {} cities", updatedCities.size());
              return Mono.fromCallable(() -> cityService.saveAll(updatedCities));
            })
        .subscribe(
            result -> log.info("Successfully updated temperatures"),
            error -> log.error("Failed to update temperatures", error));
  }
}
