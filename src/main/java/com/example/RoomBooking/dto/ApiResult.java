package com.example.RoomBooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {
    private boolean succeeded;
    private T result;
    private List<String> errors;

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, data, null);
    }

    public static <T> ApiResult<T> fail(List<String> errors) {
        return new ApiResult<>(false, null, errors);
    }

    public static <T> ApiResult<T> fail(String error) {
        return new ApiResult<>(false, null, List.of(error));
    }
}
