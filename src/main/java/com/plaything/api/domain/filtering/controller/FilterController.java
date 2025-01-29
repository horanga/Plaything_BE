package com.plaything.api.domain.filtering.controller;

import com.plaything.api.domain.filtering.model.response.TopFilteredWords;
import com.plaything.api.domain.filtering.service.FilteringService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Filtering", description = "V1 Filter API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/vi/filter")
public class FilterController {

    private final FilteringService filteringService;

    @Operation(
            summary = "필터링 단어 통계 조회",
            description = """
                    현 시각 기준으로 가장 많이 필터링 된 단어 10개와 필터링된 횟수가 조회됩니다.
                    """
    )
    @GetMapping("/top-words")
    public ResponseEntity<List<TopFilteredWords>> getFilteredWordsStatics() {
        return ResponseEntity.ok().body(filteringService.getFilterWordsStatistics());
    }

    @Operation(
            summary = "필터링 단어 저장",
            description = """
                    DB에 차단 단어를 추가합니다.
                    """
    )
    @PostMapping("/words")
    public void addFilterWord(@RequestBody List<String> words) {
        filteringService.insertWords(words);
    }

    @Operation(
            summary = "필터링 단어 통계 동기화",
            description = """
                    개발 목적 API
                    """
    )
    @PostMapping("/sync-statistics")
    public void syncFilterStatistics() {
        filteringService.cleanupMap();

    }
}
