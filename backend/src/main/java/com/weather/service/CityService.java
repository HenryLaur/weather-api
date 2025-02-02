package com.weather.service;

import com.weather.domain.City;
import com.weather.repository.CityRepository;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {

  private final CityRepository cityRepository;

  public Page<City> getCities(Pageable pageable) {
    return cityRepository.findAll(pageable);
  }

  public List<City> saveAll(Collection<City> cities) {
    log.debug("Saving {} cities", cities.size());
    // validation etc
    return cityRepository.saveAll(cities);
  }
}
