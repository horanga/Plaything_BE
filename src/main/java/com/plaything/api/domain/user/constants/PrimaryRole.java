package com.plaything.api.domain.user.constants;

import java.util.Arrays;

public enum PrimaryRole {
    TOP("FT", "MT", "TOP"),
    BOTTOM("FS", "MS", "BTM"),
    SWITCH("FSW", "MSW", "SW"),
    OTHER("F_ETC", "M_ETC", "ETC");

    final String female;

    final String male;

    final String etc;

    PrimaryRole(String female, String male, String etc) {
        this.female = female;
        this.male = male;
        this.etc = etc;
    }

    public String getPrimaryRole(PrimaryRole primaryRole, Gender gender) {
        PrimaryRole[] values = PrimaryRole.values();

        return Arrays.stream(values).filter(i -> i.equals(primaryRole))
                .map(i -> {
                            if (gender.equals(Gender.M)) {
                                return i.male;
                            }

                            if (gender.equals(Gender.F)){
                                return i.female;
                            }

                            return i.etc;
                            }).findFirst().get();
    }
}

