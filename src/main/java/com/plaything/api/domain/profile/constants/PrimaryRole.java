package com.plaything.api.domain.profile.constants;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import java.util.Arrays;

public enum PrimaryRole {
  TOP("FT", "MT", "TOP"),
  BOTTOM("FB", "MB", "BTM"),
  SWITCH("FSW", "MSW", "SW"),
  ETC("F_ETC", "M_ETC", "ETC");

  final String female;

  final String male;

  final String etc;

  PrimaryRole(String female, String male, String etc) {
    this.female = female;
    this.male = male;
    this.etc = etc;
  }

  public String getPrimaryRole(Gender gender) {
    PrimaryRole[] values = PrimaryRole.values();

    return Arrays.stream(values).filter(i -> i.equals(this))
        .map(i -> {
          if (gender.equals(Gender.M)) {
            return i.male;
          }

          if (gender.equals(Gender.F)) {
            return i.female;
          }

          return i.etc;
        }).findFirst().get();
  }

  public PrimaryRole getOpposite() {
    if (this.equals(TOP)) {
      return BOTTOM;
    }

    if (this.equals(BOTTOM)) {
      return TOP;
    }

    throw new CustomException(ErrorCode.NOT_EXIST_OPPOSITE_PRIMARY_PARTNER);
  }
}

