package com.example.library.service;

import com.example.library.dto.AccountDto.AccountView;
import com.example.library.dto.AccountDto.LoginInput;
import com.example.library.dto.AccountDto.LoginView;
import com.example.library.dto.AccountDto.NewAccountInput;
import com.example.library.dto.AccountDto.PasswordInput;

import java.util.List;

public interface AccountService {
    LoginView login(LoginInput input);

    List<AccountView> librarians();

    AccountView createLibrarian(NewAccountInput input);

    AccountView updateLibrarianPassword(Long id, PasswordInput input);

    void deleteLibrarian(Long id);

    AccountView createStudentAccount(Long studentId, NewAccountInput input);
}
