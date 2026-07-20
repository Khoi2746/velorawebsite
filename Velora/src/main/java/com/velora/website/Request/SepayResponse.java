package com.velora.website.Request;

import lombok.Data;

@Data
public class SepayResponse {
    private boolean success;

    // Constructor không tham số bắt buộc cho Jackson JSON
    public SepayResponse() {
    }

    // Constructor có tham số để khởi tạo nhanh
    public SepayResponse(boolean success) {
        this.success = success;
    }
}