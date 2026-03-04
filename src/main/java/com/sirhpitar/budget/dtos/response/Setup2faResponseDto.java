package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Setup2faResponseDto {
    private String otpAuthUrl; // we can convert to QR on frontend
}