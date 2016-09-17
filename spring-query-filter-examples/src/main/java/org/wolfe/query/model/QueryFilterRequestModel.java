package org.wolfe.query.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.validator.constraints.NotEmpty;
import org.wolfe.query.QueryParamOperator;

import javax.validation.constraints.NotNull;

/**
 * Object's must be standard POJO's with setter's for any fields
 * that need to be populated.
 */
public class QueryFilterRequestModel {

    /**
     * JSR-303 constraints are validated.
     */
    @NotNull
    private String id;

    /**
     * We can validate the @QueryParamOperator on the
     * key property itself.
     */
    @NotEmpty
    @QueryParamOperator(allowed = "=")
    private String email;

    private float balance;

    /**
     * We can also validate the @QueryParamOperator on the
     * operator property too.
     */
    @QueryParamOperator(allowed = {">", "<"})
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
