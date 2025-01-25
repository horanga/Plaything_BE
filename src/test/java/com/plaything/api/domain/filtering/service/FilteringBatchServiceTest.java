package com.plaything.api.domain.filtering.service;

import com.plaything.api.domain.filtering.model.response.TopFilteredWords;
import com.plaything.api.domain.repository.entity.filter.FilteredWordStat;
import com.plaything.api.domain.repository.repo.filter.FilteredWordsRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class FilteringBatchServiceTest {

    @Autowired
    private FilteredWordsRepository filteredWordsRepository;

    @Autowired
    private FilteringBatchService filteringBatchService;

    @Autowired
    private FilteringService filteringService;

    @Autowired
    private EntityManager entityManager;

    @DisplayName("필터링된 단어들을 한번에 데이터베이스에 저장한다")
    @Test
    void test1() {
        Map<String, Integer> map = new HashMap<>();
        map.put("밥", 2);
        map.put("고기", 2);
        map.put("계좌", 10);
        map.put("삼겹살", 12);
        filteringBatchService.insertAllFilteredWords(map);

        List<FilteredWordStat> list = filteredWordsRepository.findAll();

        assertThat(list).hasSize(4);
        assertThat(list).extracting("word").containsExactly("밥", "고기", "계좌", "삼겹살");
        assertThat(list).extracting("count").containsExactly(2, 2, 10, 12);
    }


    @DisplayName("필터링된 단어들을 한번에 데이터베이스에 저장한다")
    @Test
    void test2() {
        Map<String, Integer> map = new HashMap<>();
        map.put("밥", 2);
        map.put("고기", 2);
        map.put("계좌", 10);
        map.put("삼겹살", 12);
        filteringBatchService.insertAllFilteredWords(map);

        Map<String, Integer> map2 = new HashMap<>();
        map2.put("밥", 2);
        map2.put("고기", 2);
        map2.put("계좌", 10);
        map2.put("삼겹살", 12);
        map2.put("파전", 12);

        filteringBatchService.insertAllFilteredWords(map2);

        List<FilteredWordStat> list = filteredWordsRepository.findAll();


        assertThat(list).hasSize(5);
        assertThat(list).extracting("word").containsExactly("밥", "고기", "계좌", "삼겹살", "파전");
        assertThat(list).extracting("count").containsExactly(4, 4, 20, 24, 12);

        Map<String, Integer> map3 = new HashMap<>();
        map3.put("밥", 2);
        map3.put("고기", 2);
        map3.put("계좌", 10);
        map3.put("삼겹살", 12);
        map3.put("파전", 12);

        filteringBatchService.insertAllFilteredWords(map3);
        entityManager.clear();
        List<FilteredWordStat> list2 = filteredWordsRepository.findAll();

        assertThat(list2).hasSize(5);
        assertThat(list2).extracting("word").containsExactly("밥", "고기", "계좌", "삼겹살", "파전");
        assertThat(list2).extracting("count").containsExactly(6, 6, 30, 36, 24);

    }

    @DisplayName("필터링된 단어들에 대한 통계를 확인할 수 있다")
    @Test
    void test3() {
        Map<String, Integer> map = new HashMap<>();
        map.put("밥", 2);
        map.put("고기", 2);
        map.put("계좌", 10);
        map.put("삼겹살", 14);
        map.put("치킨", 13);
        map.put("소소기", 9);
        map.put("돼지고기", 20);
        map.put("백숙", 30);
        map.put("피자", 21);
        map.put("고구마", 32);
        map.put("오징어", 2);
        map.put("회", 15);
        map.put("대패삼겹살", 50);
        map.put("떡복이", 22);
        map.put("치즈돈까스", 12);

        filteringBatchService.insertAllFilteredWords(map);
        List<TopFilteredWords> list = filteringService.getFilterWordsStatistics();
        assertThat(list).hasSize(10);
        assertThat(list).extracting("word").containsExactly(
                "대패삼겹살",
                "고구마",
                "백숙",
                "떡복이",
                "피자",
                "돼지고기",
                "회",
                "삼겹살",
                "치킨",
                "치즈돈까스");
        assertThat(list).extracting("count").containsExactly(
                50,
                32,
                30,
                22,
                21,
                20,
                15,
                14,
                13,
                12);
    }
}