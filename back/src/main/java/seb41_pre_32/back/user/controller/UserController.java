package seb41_pre_32.back.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import seb41_pre_32.back.common.dto.MultiResponse;
import seb41_pre_32.back.user.domain.User;
import seb41_pre_32.back.user.dto.*;
import seb41_pre_32.back.user.service.UserService;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity join(@RequestBody @Valid final UserPostRequest userPostRequest) {
        return new ResponseEntity<>(
                UserResponseDto.of(userService.createUser(userPostRequest)),
                HttpStatus.CREATED);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity update(@PathVariable("userId") final Long userId,
                                 @RequestBody @Valid final UserPatchRequest userPatchRequest) {
        return new ResponseEntity<>(
                UserResponseDto.of(userService.updateUser(userId, userPatchRequest)),
                HttpStatus.OK);
    }

    @GetMapping("/{userId}")
    public ResponseEntity getUser(@PathVariable("userId") Long userId) {
        return new ResponseEntity<>(
                UserResponseDto.of(userService.findUser(userId)),
                HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<MultiResponse> getUsers(@RequestParam("page") int page,
                                                  @RequestParam("size") int size) {

        Page<User> users = userService.findUsers(page - 1, size);
        List<UserResponseDto> userResponseDtos = users.getContent()
                .stream()
                .map(UserResponseDto::of)
                .collect(Collectors.toList());

        return new ResponseEntity<>(new MultiResponse<>(userResponseDtos, users), HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable("userId") final Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}