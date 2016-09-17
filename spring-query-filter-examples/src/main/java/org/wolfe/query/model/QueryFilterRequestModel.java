package org.wolfe.query.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotEmpty;
import org.wolfe.query.QueryParamOperator;

import javax.validation.constraints.NotNull;

public class QueryFilterRequestModel {

    @NotNull
    private String id;

    @NotEmpty
    private String email;

    @QueryParamOperator(allowed = {">", "<"})
    private float balance;

    private String balanceOperator;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public String getBalanceOperator() {
        return balanceOperator;
    }

    public void setBalanceOperator(String balanceOperator) {
        this.balanceOperator = balanceOperator;
    }
}
