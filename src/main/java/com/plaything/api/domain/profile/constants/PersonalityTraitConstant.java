package com.plaything.api.domain.profile.constants;

import static com.plaything.api.domain.profile.constants.PrimaryRole.BOTTOM;
import static com.plaything.api.domain.profile.constants.PrimaryRole.TOP;

import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;

public enum PersonalityTraitConstant {

  HUNTER(TOP),
  PREY(BOTTOM),
  DADDY_MOMMY(TOP),
  LITTLE(BOTTOM),
  MASTER_MISTRESS(TOP),
  SLAVE(BOTTOM),
  OWNER(TOP),
  PET(BOTTOM),
  RIGGER(TOP),
  ROPE_BUNNY(BOTTOM),
  SPANKER(TOP),
  SPANKEE(BOTTOM),
  BRAT_TAMER(TOP),
  BRAT(BOTTOM),
  BOSS(TOP),
  SERVANT(BOTTOM),
  SADIST(TOP),
  MASOCHIST(BOTTOM),
  DEGRADER(TOP),
  DEGRADEE(BOTTOM),
  DOMINANT(TOP),
  SUBMISSIVE(BOTTOM);

  private final PrimaryRole primaryRole;

  PersonalityTraitConstant(PrimaryRole primaryRole) {
    this.primaryRole = primaryRole;
  }

  public void validateRoleCompatibility(PrimaryRole primaryRole) {
    if (this.primaryRole != primaryRole) {
      throw new CustomException(ErrorCode.ROLE_MISMATCH);
    }
  }
}
