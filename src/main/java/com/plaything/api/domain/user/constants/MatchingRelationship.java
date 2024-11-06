package com.plaything.api.domain.user.constants;

import com.plaything.api.domain.repository.entity.user.User;

import java.util.Arrays;

import static com.plaything.api.domain.user.constants.PersonalityTraitConstant.*;

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


    private PersonalityTraitConstant bottom;

    private PersonalityTraitConstant top;

    MatchingRelationship(PersonalityTraitConstant first, PersonalityTraitConstant top) {
        this.bottom = first;
        this.top = top;
    }

    public static PersonalityTraitConstant getPartner(User user) {
        PersonalityTraitConstant trait = user.getProfile().getPrimaryTrait();
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
