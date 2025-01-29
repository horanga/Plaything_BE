package com.plaything.api.domain.key.controller;

import com.plaything.api.domain.key.service.AdLogServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class PointKeyLogController {

  private final AdLogServiceV1 adLogServiceV1;
}
