package com.velora.website.Request;

import lombok.Data;

@Data
public class SepayWebhookRequest {
    private Long id;
    private String gateway;       
    private String transactionDate;
    private String accountNo;      
    private Double transferAmount; 
    private String code;
    private String content;        
    private String transferType;   // "in" (tiền vào) hoặc "out" (tiền ra)
}