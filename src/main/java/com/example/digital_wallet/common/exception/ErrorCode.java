package com.example.digital_wallet.common.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999,"uncategorized exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key",HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed",HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003,"Username must be at least {min} characters",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1004,"Password must be at least {min} character",HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005,"User not existed",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006,"Unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007,"Unauthorization",HttpStatus.FORBIDDEN),
    INVALID_DOB(1008,"Your age must be at least {min}",HttpStatus.BAD_REQUEST),
    WALLET_NOT_EXISTED(1009,"Wallet not existed",HttpStatus.BAD_REQUEST),
    OPTIMISTIC_LOCK_CONFLICT(1011,"Wallet was modified by another transaction",HttpStatus.CONFLICT),
    CANNOT_TRANSFER_TO_SELF(1010,"Can't transfer to selft",HttpStatus.BAD_REQUEST),
    INSUFFICIENT_BALANCE(1012,"Insufficient current balance",HttpStatus.BAD_REQUEST),
    TRANSACTION_PROCESSING(1013,"The transaction is being processed.",HttpStatus.CONFLICT),
    WALLET_NOT_ACTIVE(1014,"Wallet not active.",HttpStatus.BAD_REQUEST)


    ;


    int code;
    String message;
    HttpStatus httpStatus;



}
