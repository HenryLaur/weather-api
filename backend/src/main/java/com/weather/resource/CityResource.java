package com.weather.resource;

import com.weather.domain.City;
import com.weather.service.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cities")
public class CityResource {
  private final CityService cityService;

  @GetMapping
  public Page<City> getCities(Pageable pageable) {
    log.debug("Get cities with pageable: {}", pageable);
    return cityService.getCities(pageable);
  }
}
