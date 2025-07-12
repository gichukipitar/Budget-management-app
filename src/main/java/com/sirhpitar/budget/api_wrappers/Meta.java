package com.sirhpitar.budget.api_wrappers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Meta {
    private int statusCode;
    private ApiResponseStatus status;
    private String message;
    private String timestamp;
}
