package com.diph.lumovie.service;

import com.diph.lumovie.dto.ophim.OPhimDetailResponse;
import com.diph.lumovie.dto.ophim.OPhimItem;
import com.diph.lumovie.dto.ophim.OPhimListResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OPhimService {

    // ← Đổi sang phimapi.com
    private static final String BASE_URL         = "https://phimapi.com";
    private static final String LIST_ENDPOINT    = BASE_URL + "/danh-sach/phim-moi-cap-nhat?page={page}";
    private static final String DETAIL_ENDPOINT  = BASE_URL + "/phim/{slug}";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Lấy 1 trang danh sách
    public OPhimListResponse fetchMovieList(int page) {
        try {
            String url = LIST_ENDPOINT.replace("{page}", String.valueOf(page));
            log.info("Fetching page {}: {}", page, url);
            String json = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(json, OPhimListResponse.class);
        } catch (Exception e) {
            log.error("Lỗi fetch trang {}: {}", page, e.getMessage());
            return null;
        }
    }

    // Lấy tất cả items từ nhiều trang
    public List<OPhimItem> fetchAllMovies(int maxPages) {
        List<OPhimItem> allItems = new ArrayList<>();

        OPhimListResponse first = fetchMovieList(1);
        if (first == null || first.getItems() == null) {
            log.warn("Không lấy được dữ liệu");
            return allItems;
        }

        allItems.addAll(first.getItems());

        int totalPages = first.getPagination() != null
                ? first.getPagination().getTotalPages() : 1;
        int pagesToFetch = Math.min(maxPages, totalPages);

        log.info("Tổng {} trang — sẽ fetch {} trang", totalPages, pagesToFetch);

        for (int page = 2; page <= pagesToFetch; page++) {
            OPhimListResponse resp = fetchMovieList(page);
            if (resp != null && resp.getItems() != null) {
                allItems.addAll(resp.getItems());
                log.info("Trang {}/{} — {} phim", page, pagesToFetch, resp.getItems().size());
            }
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
        }

        log.info("Tổng: {} phim", allItems.size());
        return allItems;
    }

    // Lấy chi tiết 1 phim
    public OPhimDetailResponse fetchMovieDetail(String slug) {
        try {
            String url = DETAIL_ENDPOINT.replace("{slug}", slug);
            log.debug("Fetching detail: {}", url);
            String json = restTemplate.getForObject(url, String.class);
            return objectMapper.readValue(json, OPhimDetailResponse.class);
        } catch (Exception e) {
            log.error("Lỗi fetch detail [{}]: {}", slug, e.getMessage());
            return null;
        }
    }
}


//.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"
//http://localhost:8080/dev/seed/full?pages=2