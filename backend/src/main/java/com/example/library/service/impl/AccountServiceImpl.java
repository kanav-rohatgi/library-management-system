package com.example.library.service.impl;

import com.example.library.dto.AccountDto.AccountView;
import com.example.library.dto.AccountDto.LoginInput;
import com.example.library.dto.AccountDto.LoginView;
import com.example.library.dto.AccountDto.NewAccountInput;
import com.example.library.dto.AccountDto.PasswordInput;
import com.example.library.entity.LibraryUser;
import com.example.library.entity.Role;
import com.example.library.entity.Student;
import com.example.library.repository.LibraryUserRepository;
import com.example.library.repository.StudentRepository;
import com.example.library.security.JwtTokens;
import com.example.library.service.AccountService;
import com.example.library.service.LibraryException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
    private final LibraryUserRepository users;
    private final StudentRepository students;
    private final PasswordEncoder encoder;
    private final JwtTokens tokens;
    private final String dummyHash;

    public AccountServiceImpl(LibraryUserRepository users, StudentRepository students,
                              PasswordEncoder encoder, JwtTokens tokens) {
        this.users = users;
        this.students = students;
        this.encoder = encoder;
        this.tokens = tokens;
        this.dummyHash = encoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public LoginView login(LoginInput input) {
        if (exceedsBcryptLimit(input.password())) {
            throw LibraryException.unauthorized();
        }
        LibraryUser user = users.findByUsername(normalize(input.username())).orElse(null);
        boolean passwordMatches = encoder.matches(input.password(), user == null ? dummyHash : user.getPasswordHash());
        if (!passwordMatches || user == null || !user.canSignIn()) {
            throw LibraryException.unauthorized();
        }
        Long studentId = user.getStudent() == null ? null : user.getStudent().getId();
        return new LoginView(tokens.issue(user), user.getUsername(), user.getRole(), studentId);
    }

    @Override
    public List<AccountView> librarians() {
        return users.findAllByRoleOrderByUsername(Role.LIBRARIAN).stream().map(AccountView::from).toList();
    }

    @Override
    @Transactional
    public AccountView createLibrarian(NewAccountInput input) {
        return createAccount(input, Role.LIBRARIAN, null);
    }

    @Override
    @Transactional
    public AccountView updateLibrarianPassword(Long id, PasswordInput input) {
        LibraryUser user = librarian(id);
        user.changePassword(encode(input.password()));
        return AccountView.from(user);
    }

    @Override
    @Transactional
    public void deleteLibrarian(Long id) {
        users.delete(librarian(id));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public AccountView createStudentAccount(Long studentId, NewAccountInput input) {
        Student student = students.lockActive(studentId)
                .orElseThrow(() -> LibraryException.missing("Student not found"));
        if (users.findByStudentId(studentId).isPresent()) {
            throw LibraryException.conflict("Student already has an account");
        }
        return createAccount(input, Role.STUDENT, student);
    }

    private AccountView createAccount(NewAccountInput input, Role role, Student student) {
        String username = normalize(input.username());
        if (users.findByUsername(username).isPresent()) {
            throw LibraryException.conflict("Username is already registered");
        }
        return AccountView.from(users.save(new LibraryUser(username, encode(input.password()), role, student)));
    }

    private LibraryUser librarian(Long id) {
        return users.findByIdAndRole(id, Role.LIBRARIAN)
                .orElseThrow(() -> LibraryException.missing("Librarian not found"));
    }

    private String encode(String password) {
        if (exceedsBcryptLimit(password)) {
            throw LibraryException.invalid("Password must contain at most 72 UTF-8 bytes");
        }
        return encoder.encode(password);
    }

    private boolean exceedsBcryptLimit(String password) {
        return password.getBytes(StandardCharsets.UTF_8).length > 72;
    }

    private String normalize(String username) {
        return username.strip().toLowerCase(Locale.ROOT);
    }
}
