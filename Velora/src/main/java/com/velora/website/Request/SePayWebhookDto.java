package com.velora.website.Request;

import lombok.Data;

@Data
public class SePayWebhookDto {
    private Long id;
    private String gateway;
    private String transactionDate; 
    private String accountNo;
    private Double amountIn;
    private Double amountOut;
    private Double transferAmount; // Nhận biến tiền vào chính xác từ SePay
    private String transferType;   // "in" hoặc "out"
    private String code;
    private String content;
    private String referenceNumber;
    private String body;
    private Integer accumulated;
    private String subAccount;
}