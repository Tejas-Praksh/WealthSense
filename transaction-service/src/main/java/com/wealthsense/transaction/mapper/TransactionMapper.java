package com.wealthsense.transaction.mapper;

import com.wealthsense.transaction.domain.Account;
import com.wealthsense.transaction.domain.Transaction;
import com.wealthsense.transaction.dto.AccountDto;
import com.wealthsense.transaction.dto.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    TransactionResponse toResponse(Transaction transaction);

    @Mapping(target = "active", source = "active")
    AccountDto toAccountDto(Account account);
}
