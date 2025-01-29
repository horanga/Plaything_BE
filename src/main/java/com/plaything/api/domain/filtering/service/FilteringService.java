package com.plaything.api.domain.filtering.service;

import static com.plaything.api.domain.filtering.constants.PATTERN.CONSECUTIVE_NUMBER;
import static com.plaything.api.domain.filtering.constants.PATTERN.FILTER_ALL_PATTERN;
import static com.plaything.api.domain.filtering.constants.PATTERN.KEEP_ONLY_PATTERN;
import static com.plaything.api.domain.filtering.constants.PATTERN.PHONE_NUMBER;
import static com.plaything.api.domain.filtering.constants.PATTERN.WHITE_SPACE;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.filtering.model.response.TopFilteredWords;
import com.plaything.api.domain.repository.entity.filter.FilterWords;
import com.plaything.api.domain.repository.entity.filter.FilteredWordStat;
import com.plaything.api.domain.repository.repo.filter.FilterWordsRepository;
import com.plaything.api.domain.repository.repo.filter.FilteredWordsRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class FilteringService {

  private final FilterWordsRepository filterWordsRepository;
  private final FilteredWordsRepository filteredWordsRepository;
  private final FilteringBatchService filteringBatchService;
  private final Map<String, Integer> filteredWords = new ConcurrentHashMap<>();
  public Trie filter;

  @PostConstruct
  public void initFilter() {
    filter = Trie.builder()
        .ignoreCase()
        .ignoreOverlaps()
        .addKeywords(buildTrie())
        .build();
  }

  public void filterWords(String words) {

    boolean hasConsecutiveNumber = CONSECUTIVE_NUMBER.matcher(words).find();
    boolean hasPhoneNumber = PHONE_NUMBER.matcher(words).find();

    if (hasConsecutiveNumber || hasPhoneNumber) {
      throw new CustomException(ErrorCode.CONSECUTIVE_NUMBERS_NOT_ALLOWED);
    }

    String temp = WHITE_SPACE.matcher(words).replaceAll("");
    String finalResult1 = KEEP_ONLY_PATTERN.matcher(words).replaceAll("");
    String finalResult2 = FILTER_ALL_PATTERN.matcher(temp).replaceAll("");

    Emit emit1 = filter.firstMatch(finalResult1);
    Emit emit2 = filter.firstMatch(finalResult2);
    if (emit1 != null) {
      filteredWords.compute(emit1.getKeyword(), (em, value) -> value == null ? 1 : value + 1);
      throw new CustomException(ErrorCode.BAD_WORDS_FILTER);
    }

    if (emit2 != null) {
      filteredWords.compute(emit2.getKeyword(), (em, value) -> value == null ? 1 : value + 1);
      throw new CustomException(ErrorCode.BAD_WORDS_FILTER);
    }
  }

  public List<String> buildTrie() {
    return filterWordsRepository.findAll().stream().map(FilterWords::getWord).toList();
  }

  public void insertWords(List<String> word) {

    List<FilterWords> list =
        word.stream().map(i -> FilterWords.builder().word(i).build()).toList();

    filterWordsRepository.saveAll(list);
    List<String> wordList = buildTrie();
    filter = Trie.builder()
        .ignoreCase()
        .ignoreOverlaps()
        .addKeywords(wordList)
        .build();
  }

  public List<TopFilteredWords> getFilterWordsStatistics() {
    List<FilteredWordStat> words = filteredWordsRepository.getTopFilteredWords();

    return words.stream()
        .map((i) -> new TopFilteredWords(i.getWord(), i.getCount()))
        .toList();
  }

  @Scheduled(cron = "0 30 5 * * *")
  public void cleanupMap() {
    filteringBatchService.insertAllFilteredWords(this.filteredWords);
    filteredWords.clear();
  }
}
