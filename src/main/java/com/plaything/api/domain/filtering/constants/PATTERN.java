package com.plaything.api.domain.filtering.constants;

import java.util.regex.Pattern;

public class PATTERN {

  public static final Pattern WHITE_SPACE = Pattern.compile("\\s");

  public static final Pattern FILTER_ALL_PATTERN = Pattern.compile(
      // 1) 한글 사이의 모든 문자(자음/모음/영문자/특수문자/숫자) 제거
      "(?<=[가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ])[^가-힣a-zA-Z]+(?=[가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ])|" +
          // 2) 단어 앞뒤의 특수문자/숫자 제거 (자음/모음은 유지)
          "^[^가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ]+|[^가-힣a-zA-Zㄱ-ㅎㅏ-ㅣ]+$|" +
          "\\s"
  );

  public static final Pattern KEEP_ONLY_PATTERN = Pattern.compile("[^ㄱ-ㅎ]");

  public static final Pattern PHONE_NUMBER = Pattern.compile("01[0-9]{1}-[0-9]{3,4}-[0-9]{4}");

  public static final Pattern CONSECUTIVE_NUMBER = Pattern.compile("\\d{5,}");


}
