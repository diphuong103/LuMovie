package com.diph.lumovie.dto.ophim;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// ═══════════════════════════════════════════════════
// OPhimListResponse — /danh-sach/phim-moi-cap-nhat
// ═══════════════════════════════════════════════════
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OPhimListResponse {

    private boolean status;
    private String msg;
    private List<OPhimItem> items;
    private Pagination pagination;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pagination {
        @JsonProperty("totalItems")
        private int totalItems;

        @JsonProperty("totalItemsPerPage")
        private int totalItemsPerPage;

        @JsonProperty("currentPage")
        private int currentPage;

        @JsonProperty("totalPages")
        private int totalPages;
    }
}
