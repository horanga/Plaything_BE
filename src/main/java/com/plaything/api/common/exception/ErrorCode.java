package com.plaything.api.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode implements CodeInterFace {

  SUCCESS("SUCCESS", HttpStatus.OK),

  // USER 가입 or 로그인
  USER_ALREADY_EXISTS("회원이 이미 존재합니다", HttpStatus.CONFLICT),
  USER_SAVED_FAILED("회원가입에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
  NOT_EXIST_USER("존재하지 않는 회원입니다", HttpStatus.NOT_FOUND),
  MIS_MATCH_PASSWORD("잘못된 비밀번호입니다", HttpStatus.UNAUTHORIZED),
  AUTHORIZATION_FAIL("인증에 실패했습니다", HttpStatus.UNAUTHORIZED),

  //JWT 토큰
  TOKEN_IS_INVALID("유효하지 않은 토큰입니다", HttpStatus.UNAUTHORIZED),
  ACCESS_TOKEN_IS_NOT_EXPIRED("ACCESS_TOKEN_IS_NOT_EXPIRED", HttpStatus.BAD_REQUEST),
  TOKEN_IS_EXPIRED("토큰이 만료됐습니다", HttpStatus.UNAUTHORIZED),


  //프로필 사진 업로드
  EXTENSION_IS_INVALID("허용되지 않은 파일 확장자입니다", HttpStatus.BAD_REQUEST),
  CONTENT_TYPE_IS_INVALID("이미지 파일 형식이 아닙니다", HttpStatus.BAD_REQUEST),
  IMAGE_SIZE_EXCEEDED("프로필 사진의 크기가 제한 크기를 초과했습니다", HttpStatus.BAD_REQUEST),
  IMAGE_COUNT_EXCEEDED("프로필 사진은 최대 3개까지만 등록 가능합니다", HttpStatus.BAD_REQUEST),
  IMAGE_SAVED_FAILED("IMAGE_SAVED_FAILED", HttpStatus.INTERNAL_SERVER_ERROR),
  IMAGE_REQUIRED("프로필 사진을 필수로 등록해야 합니다", HttpStatus.BAD_REQUEST),
  MAIN_IMAGE_REQUIRED("프로필 사진 중에 메인 사진을 골라야 합니다", HttpStatus.BAD_REQUEST),
  MAIN_IMAGE_COUNT_EXCEEDED("메인 사진은 하나만 골라야 합니다", HttpStatus.BAD_REQUEST),
  NOT_EXIST_MAIN_PHOTO("요청자의 메인 프로필 사진이 없어 매칭이 불가능합니다", HttpStatus.UNPROCESSABLE_ENTITY),
  INVALID_IMAGE_UPDATE_REQUEST("변경할 이미지 정보가 없습니다", HttpStatus.BAD_REQUEST),


  //프로필 조회 or 등록
  NOT_EXIST_PROFILE("프로필이 존재하지 않는 회원입니다", HttpStatus.NOT_FOUND),
  NOT_EXIST_OPPOSITE_PRIMARY_PARTNER("반대되는 대표성향이 존재하지 않습니다", HttpStatus.NOT_FOUND),
  NOT_EXIST_PROFILE_RECORD("NOT EXIST PROFILE RECORD", HttpStatus.NOT_FOUND),
  NOT_AUTHORIZED_PROFILE("부적절한 프로필로 비활성화된 회원입니다", HttpStatus.FORBIDDEN),
  PROFILE_REGISTER_FAILED("PROFILE_REGISTER_FAILED", HttpStatus.INTERNAL_SERVER_ERROR),
  PROFILE_ALREADY_EXIST("PROFILE ALREADY EXIST", HttpStatus.CONFLICT),
  NICKNAME_ALREADY_EXISTS("이미 등록된 닉네임입니다", HttpStatus.CONFLICT),

  //프로필 성향
  NOT_EXIST_PRIMARY_TRAIT("대표 성향이 존재하지 않은 회원입니다", HttpStatus.NOT_FOUND),
  TRAITS_NOT_INCLUDE_PRIMARY("대표성향을 선택하지 않았습니다", HttpStatus.BAD_REQUEST),
  ROLE_MISMATCH("대표성향과 일치하지 않는 세부성향입니다", HttpStatus.BAD_REQUEST),
  PRIMARY_TRAIT_ALREADY_EXIST("대표성향은 두 개 이상일 수 없습니다", HttpStatus.CONFLICT),


  //매칭
  MATCHING_FAIL_WITHOUT_IMAGE("등록된 프로필 사진이 없어 매칭 요청이 실패합니다", HttpStatus.UNPROCESSABLE_ENTITY),
  MATCHING_FAIL_WITH_BAN_PROFILE("계정이 정지되어 매칭이 불가능합니다", HttpStatus.FORBIDDEN),
  NOT_EXIST_MATCHING("매칭 정보가 존재하지 않습니다", HttpStatus.NOT_FOUND),

  //광고
  AD_VIEW_TIME_NOT_EXPIRED("AD_VIEW_TIME_NOT_EXPIRED", HttpStatus.TOO_MANY_REQUESTS),
  POINT_KEY_SAVED_FAILED("POINT KEY SAVED FAILED", HttpStatus.INTERNAL_SERVER_ERROR),

  //포인트 키 사용
  NOT_EXIST_AVAILABLE_POINT_KEY("사용가능한 포인트 키가 존재하지 않습니다", HttpStatus.NOT_FOUND),

  //로그
  LOG_SAVED_FAILED("LOG SAVED FAILED", HttpStatus.INTERNAL_SERVER_ERROR),

  //알림
  NOTIFICATION_SAVED_FAILED("알림 전송에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

  //메시지
  MESSAGE_CREATION_FAILED("MESSAGE CREATION FAILED", HttpStatus.INTERNAL_SERVER_ERROR),

  //요청
  TRANSACTION_ALREADY_PROCESSED("이미 처리된 요청입니다", HttpStatus.CONFLICT),
  TRANSACTION_ID_REQUIRED("요청 식별값을 보내야합니다", HttpStatus.BAD_REQUEST),

  //채팅방
  NOT_EXIST_CHATROOM("채팅방이 존재하지 않습니다", HttpStatus.NOT_FOUND),
  NOT_AUTHORIZED_CHAT_ROOM_USER("채팅방에 속한 사용자가 아닙니다", HttpStatus.FORBIDDEN),
  PARTNER_ALREADY_LEAVE("상대방이 채팅방을 떠났습니다", HttpStatus.CONFLICT),
  CHAT_ROOM_IS_OVER("이미 종료된 채팅방입니다", HttpStatus.GONE),
  TOO_MANY_CHAT_RATE("짧은 시간에 너무 많은 채팅 메시지를 보냈습니다", HttpStatus.TOO_MANY_REQUESTS),
  NOT_AUTHORIZED_SUBSCRIBE("채널 구독 권한이 없습니다", HttpStatus.FORBIDDEN),
  NOT_AUTHORIZED_USER("메시지 발신자가 세션 회원과 일치하지 않습니다", HttpStatus.FORBIDDEN),
  NOT_MATCHING_PARTNER("매칭된 파트너가 아닙니다", HttpStatus.FORBIDDEN),
  NOT_EXIST_MATCHING_PARTNER("매칭 파트너가 없습니다", HttpStatus.NOT_FOUND),
  NOT_CONNECTED_STOMP("STOMP에 채팅 연결이 된 회원이 아닙니다", HttpStatus.FORBIDDEN),
  NOT_AUTHORIZED_CHAT("인증 토큰이 없어 처리가 불가능합니다", HttpStatus.UNAUTHORIZED),
  CONNECTION_ALREADY_EXIST("이미 웹소켓 연결이 된 상태입니다", HttpStatus.CONFLICT),

  //메시지 검사
  BAD_WORDS_FILTER("금지된 단어가 포함됐습니다", HttpStatus.BAD_REQUEST),

  //중복 요청
  DUPLICATE_TRANSACTION_REQUEST("중복된 요청입니다", HttpStatus.CONFLICT),
  CONSECUTIVE_NUMBERS_NOT_ALLOWED("연속된 숫자는 입력할 수 없습니다", HttpStatus.BAD_REQUEST);

  private final String message;
  private final HttpStatus httpStatus;
}
