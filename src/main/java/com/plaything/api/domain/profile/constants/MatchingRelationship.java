package com.plaything.api.domain.profile.constants;

import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.BOSS;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.BRAT;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.BRAT_TAMER;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.DADDY_MOMMY;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.DEGRADEE;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.DEGRADER;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.DOMINANT;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.HUNTER;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.LITTLE;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.MASOCHIST;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.MASTER_MISTRESS;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.OWNER;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.PET;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.PREY;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.RIGGER;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.ROPE_BUNNY;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.SADIST;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.SERVANT;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.SLAVE;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.SPANKEE;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.SPANKER;
import static com.plaything.api.domain.profile.constants.PersonalityTraitConstant.SUBMISSIVE;

import com.plaything.api.domain.repository.entity.profile.Profile;
import java.util.Arrays;

public enum MatchingRelationship {

  ONE(MASOCHIST, SADIST),
  TWO(DEGRADEE, DEGRADER),
  THREE(SLAVE, MASTER_MISTRESS),
  FOUR(SPANKEE, SPANKER),
  FIVE(PREY, HUNTER),
  SIX(ROPE_BUNNY, RIGGER),
  SEVEN(BRAT, BRAT_TAMER),
  EIGHT(PET, OWNER),
  NINE(LITTLE, DADDY_MOMMY),
  TEN(SERVANT, BOSS),
  ELEVEN(SUBMISSIVE, DOMINANT);


  private final PersonalityTraitConstant bottom;

  private final PersonalityTraitConstant top;

  MatchingRelationship(PersonalityTraitConstant first, PersonalityTraitConstant top) {
    this.bottom = first;
    this.top = top;
  }

  public static PersonalityTraitConstant getPartner(Profile profile) {
    PersonalityTraitConstant trait = profile.getPrimaryTrait();
    MatchingRelationship[] values = MatchingRelationship.values();
    return Arrays.stream(values).filter(i ->
            i.bottom.equals(trait) || i.top.equals(trait))
        .map(i -> {
          if (i.bottom.equals(trait)) {
            return i.top;
          } else {
            return i.bottom;
          }
        }).findFirst().get();
  }
}
