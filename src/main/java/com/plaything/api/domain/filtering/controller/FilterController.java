package com.plaything.api.domain.filtering.controller;

import com.plaything.api.domain.filtering.service.FilteringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class FilterController {

    private final FilteringService filteringService;

    @GetMapping("/filter")
    public String filter(
            @RequestParam("words") String word)
    {
        filteringService.filterWords(word);
        return "필터링되지 않아습니다";
    }

    @PostMapping("insert-word")
    public void insertWord(
            @RequestBody String word
    ){
        filteringService.insertWords(word);
    }
}
