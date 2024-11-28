package com.plaything.api.domain.filtering.service;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.domain.repository.entity.filter.FilterWords;
import com.plaything.api.domain.repository.repo.filter.FilterWordsRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Service
public class FilteringService {

    private final FilterWordsRepository filterWordsRepository;

    public Trie filter;

    private static final Pattern WHITE_SPACE = Pattern.compile("\\s");

    private static final Pattern FILTER_ALL_PATTERN = Pattern.compile(
            // 1) 한글 사이의 모든 문자(자음/모음/영문자/특수문자/숫자) 제거
            "(?<=[가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ])[^가-힣a-zA-Z]+(?=[가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ])|" +
                    // 2) 단어 앞뒤의 특수문자/숫자 제거 (자음/모음은 유지)
                    "^[^가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ]+|[^가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ]+$|" +
                    "\\s"
    );

    private static final Pattern KEEP_ONLY_PATTERN = Pattern.compile("[^ㄱ-ㅎ]");

    @PostConstruct
    public void initFilter() {
        filter = Trie.builder()
                .ignoreCase()
                .ignoreOverlaps()
                .addKeywords(buildTrie())
                .build();
    }

    public void filterWords(String words) {

        String temp = WHITE_SPACE.matcher(words).replaceAll("");
        String finalResult1 = KEEP_ONLY_PATTERN.matcher(words).replaceAll("");
        String finalResult2 = FILTER_ALL_PATTERN.matcher(temp).replaceAll("");

        Emit emit1 = filter.firstMatch(finalResult1);
        Emit emit2 = filter.firstMatch(finalResult2);
        if (emit1 != null || emit2 != null) {
            throw new CustomException(ErrorCode.BAD_WORDS_FILTER);
        }
    }

    public List<String> buildTrie() {
        return filterWordsRepository.findAll().stream().map(FilterWords::getWord).toList();
    }

    public void insertWords(String word) {
        FilterWords filterWords = FilterWords.builder().word(word).build();
        filterWordsRepository.save(filterWords);

        buildTrie();
    }


}
