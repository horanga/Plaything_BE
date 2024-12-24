package com.plaything.api.domain.filtering.service;

import com.plaything.api.common.exception.CustomException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class FilteringServiceTest {

    @Autowired
    private FilteringService filteringService;

    @DisplayName("금칙어가 포함된 메시지를 보내면 실패한다.")
    @Test
    public void test1() {

        List<String> list = List.of("섹스", "sex", "ㅅㅅ");

        for (String word : list) {
            assertThatThrownBy(() -> filteringService.filterWords(word))
                    .isInstanceOf(CustomException.class).hasMessage("금지된 단어가 포함됐습니다");
        }
    }

    @DisplayName("숫자로 우회해도 금칙어를 걸러낸다")
    @Test
    public void test2() {

        List<String> list = List.of(
                "섹1스",
                "섹111 스",
                "섹 1111스",
                "섹스1111",
                "섹 스111",
                "111섹스",
                "1 섹 스",
                "1sex",
                "1 sex",
                "1s ex",
                "s111ex",
                "s1111 ex",
                "se1x",
                "se1 x",
                "s e1x",
                "sex1",
                "sex 1 ",
                "1ㅅㅅ",
                "1 ㅅㅅ",
                "1 ㅅ ㅅ",
                "ㅅ 1 ㅅ",
                "ㅅ1ㅅ",
                "ㅅㅅ1");

        for (String word : list) {
            assertThatThrownBy(() -> filteringService.filterWords(word))
                    .isInstanceOf(CustomException.class).hasMessage("금지된 단어가 포함됐습니다");
        }
    }

    @DisplayName("단어 사이에 특수문자가 끼어도 금칙어를 걸러낸다")
    @Test
    public void test3() {

        List<String> list = List.of(
                "!섹스",
                "! 섹스",
                "![]]섹스",
                "섹!스",
                "섹!;';[[스",
                "섹스!",
                "*섹스",
                "섹(스",
                "섹)스",
                "섹스-",
                "섹스-][[;",
                "!ㅅㅅ",
                "ㅅ!ㅅ",
                "ㅅㅅ!",
                "~sex",
                "@sex",
                "#sex!",
                "`1234567890-se{[]]x]||';l/"
        );

        for (String word : list) {
            assertThatThrownBy(() -> filteringService.filterWords(word))
                    .isInstanceOf(CustomException.class).hasMessage("금지된 단어가 포함됐습니다");
        }
    }

    @DisplayName("단어 사이에 자음/모음이 끼어도 금칙어를 걸러낸다")
    @Test
    public void test4() {

        List<String> list = List.of(
                "!ㅅㅅ",
                "밥밥ㅅㅅ",
                "ㅅ!!밥ㅅ",
                "ㅅ밥밥ㅅ",
                "ㅅㅅ밥밥",
                "ㅅ11!ㅅ",
                "ㅅㅅ!",
                "ㅅadㅅ",
                "a섹ㅅㅅ",
                "섻ㅅㅅ"
        );

        for (String word : list) {
            assertThatThrownBy(() -> filteringService.filterWords(word))
                    .isInstanceOf(CustomException.class).hasMessage("금지된 단어가 포함됐습니다");
        }
    }

    @DisplayName("금칙어가 없는 단어는 걸러내지 않는다.")
    @Test
    public void test5() {

        List<String> list = List.of(
                "!ㅅ안녕",
                "안녕!ㅅ",
                "안!ㅅ녕",
                "!hi",
                "hi!",
                "!ㅎ!ㅇ!",
                "!ㅎㅇ"
        );

        for (String word : list) {
            filteringService.filterWords(word);
        }
    }

//    @Test
//    void testx() {
//
//        List<String> messages = generateTestInputs(5000); // 1000개의 다른 테스트 케이스
//        int iterations = 10;
//        long[] executionTimes = new long[iterations];
//
//        for (int i = 0; i < iterations; i++) {
//            long startTime = System.nanoTime();
//
//            for (String chat : messages) {
//                try {
//                    filteringService.filterWords(chat);
//                } catch (CustomException e) {
//                    // 예외는 무시하고 계속 진행
//                }
//
//            }
//
//            executionTimes[i] = System.nanoTime() - startTime;
//        }
//
//        // 평균 계산
//        double averageTime = Arrays.stream(executionTimes).average().orElse(0) / 1000000.0;  // nano -> ms
//
//        // 최소, 최대 시간
//        double minTime = Arrays.stream(executionTimes).min().orElse(0) / 1000000.0;
//        double maxTime = Arrays.stream(executionTimes).max().orElse(0) / 1000000.0;
//
//        System.out.println("평균 실행 시간: " + averageTime + "ms");
//        System.out.println("최소 실행 시간: " + minTime + "ms");
//        System.out.println("최대 실행 시간: " + maxTime + "ms");
//        System.out.println("메시지당 평균 처리 시간: " + (averageTime / messages.size()) + "ms");
//    }
//
//
//    private List<String> generateTestInputs(int count) {
//        List<String> baseInputs = Arrays.asList(
//                "안녕하세요ㅋㅋㅎㅎ",
//                "섹!스~관련#단어",
//                "ㅅㅅ테스트123ABC",
//                "특수@#$문자!섞인~문장",
//                "일반적인 대화문장"
//        );
//
//        Random random = new Random();
//        List<String> testInputs = new ArrayList<>();
//
//        for (int i = 0; i < count; i++) {
//            // 기본 문장 선택
//            String base = baseInputs.get(random.nextInt(baseInputs.size()));
//
//            // 랜덤하게 변형
//            StringBuilder modified = new StringBuilder(base);
//            // 랜덤 위치에 특수문자 삽입
//            modified.insert(random.nextInt(modified.length()), "!@#$%^&*".charAt(random.nextInt(8)));
//            // 랜덤 위치에 숫자 삽입
//            modified.insert(random.nextInt(modified.length()), String.valueOf(random.nextInt(10)));
//            // 랜덤 위치에 자음/모음 삽입
//            modified.insert(random.nextInt(modified.length()), "ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ".charAt(random.nextInt(14)));
//
//            testInputs.add(modified.toString());
//        }
//        return testInputs;
//    }


}