package com.plaything.api.domain.matching.constants;

import java.util.concurrent.TimeUnit;

public class MatchingConstants {

    public static final String MATCHING_CANDIDATE_REDIS_KEY = "::matchingRequest";
    public static final String MATCHING_LIST_REDIS_KEY = "::matchingList";
    public static final String COUNT_REDIS_KEY = "::count";
    public static final String LAST_PROFILE_ID_REDIS_KEY = "::lastProfileId";
    public static final String HIDE_PROFILE_KEY = ":hideProfile";

    public static final int MAX_SKIP_COUNT = 50;
    public static final int BLOCK_DURATION = 8;
    public static final int EXPIRATION_DATE_SKIP_COUNT = 20;
    public static final int EXPIRATION_DATE_PROFILE_ID = 20;

    public static final String KEYWORD_DUMMY_CACHE = "dummy";

    public static final int CACHE_DURATION_DAY = 1;
    public static final TimeUnit CACHE_DURATION_UNIT_HOUR = TimeUnit.HOURS;
    public static final TimeUnit CACHE_DURATION_UNIT_DAYS = TimeUnit.DAYS;
}
