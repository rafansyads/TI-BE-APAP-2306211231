package apap.ti._5.accommodation_2306211231_be.service;

import apap.ti._5.accommodation_2306211231_be.util.ProvinceUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Thin service wrapper around ProvinceUtil for DI and future extensibility
 * (e.g., moving province data to a database or remote service).
 */
@Service
@RequiredArgsConstructor
public class ProvinceService {

    /** Get province name by code (Integer). */
    public Optional<String> getNameByCode(Integer code) {
        return ProvinceUtil.getNameByCode(code);
    }

    /** Get code by province name (case-insensitive, tolerant to markers). */
    public Optional<Integer> getCodeByName(String name) {
        return ProvinceUtil.getCodeByName(name);
    }

    /** Validate if a code exists. */
    public boolean isValidCode(Integer code) {
        return ProvinceUtil.isValidCode(code);
    }

    /** Retrieve the immutable map of all code→name entries. */
    public Map<Integer, String> getAll() {
        return ProvinceUtil.getAll();
    }
}
