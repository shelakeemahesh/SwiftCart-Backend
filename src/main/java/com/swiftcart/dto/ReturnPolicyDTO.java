package com.swiftcart.dto;

import java.util.List;

public class ReturnPolicyDTO {
    private int daysAllowed;
    private List<String> conditions;

    public ReturnPolicyDTO() {}

    public ReturnPolicyDTO(int daysAllowed, List<String> conditions) {
        this.daysAllowed = daysAllowed;
        this.conditions = conditions;
    }

    public int getDaysAllowed() { return daysAllowed; }
    public void setDaysAllowed(int daysAllowed) { this.daysAllowed = daysAllowed; }

    public List<String> getConditions() { return conditions; }
    public void setConditions(List<String> conditions) { this.conditions = conditions; }
}
