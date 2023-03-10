package seb41_pre_32.back.restdocs.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import seb41_pre_32.back.answer.entity.Answer;
import seb41_pre_32.back.auth.presentation.dto.AuthInfo;
import seb41_pre_32.back.question.entity.Question;
import seb41_pre_32.back.tag.entity.QuestionTag;
import seb41_pre_32.back.tag.entity.Tag;
import seb41_pre_32.back.user.controller.UserController;
import seb41_pre_32.back.user.dto.UserPatchRequest;
import seb41_pre_32.back.user.dto.UserPostRequest;
import seb41_pre_32.back.user.entity.Role;
import seb41_pre_32.back.user.entity.User;
import seb41_pre_32.back.user.service.UserService;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfigurer.class)})
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    public void createTest() throws Exception {
        // given
        UserPostRequest userPostRequest = UserPostRequest
                .builder()
                .displayName("testUser")
                .password("abc1234")
                .email("test1234@gmail.com")
                .build();

        String content = objectMapper.writeValueAsString(userPostRequest);

        User savedUser = User.builder()
                .userId(1L)
                .password(userPostRequest.getPassword())
                .username(userPostRequest.getDisplayName())
                .email(userPostRequest.getEmail())
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        given(userService.createUser(Mockito.any(UserPostRequest.class))).willReturn(savedUser);

        // when
        ResultActions actions = mockMvc.perform(post("/api/users")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(content)
        );

        // then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.displayName").value(userPostRequest.getDisplayName()))
                .andExpect(jsonPath("$.email").value(userPostRequest.getEmail()))
                .andExpect(jsonPath("$.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.reputation").value(0))
                .andExpect(jsonPath("$.location").value("??????"))
                .andExpect(jsonPath("$.role").value(savedUser.getRole().getValue()))
                .andDo(document("create-user",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        requestFields(
                                List.of(
                                        fieldWithPath("displayName").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("password").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("?????????")
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void updateTest() throws Exception {
        // given
        Long userId = 1L;

        UserPatchRequest userPatchRequest = UserPatchRequest.builder()
                .displayName("updatedName")
                .profileUrl("updatedUrl")
                .location("??????")
                .build();

        AuthInfo authInfo = AuthInfo.builder()
                .userId(userId)
                .email("test1234@gmail.com")
                .displayName("originName")
                .role("USER")
                .build();

        String content = objectMapper.writeValueAsString(userPatchRequest);

        User updatedUser = User.builder()
                .userId(authInfo.getUserId())
                .password("1111")
                .username(userPatchRequest.getDisplayName())
                .email(authInfo.getEmail())
                .profileUrl(userPatchRequest.getProfileUrl())
                .reputation(0)
                .location(userPatchRequest.getLocation())
                .role(Role.USER)
                .build();

        given(userService.updateUser(
                Mockito.anyLong(),
                Mockito.any(UserPatchRequest.class),
                Mockito.any(AuthInfo.class)))
                .willReturn(updatedUser);

        // when
        ResultActions actions = mockMvc.perform(patch("/api/users/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(content)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.displayName").value(userPatchRequest.getDisplayName()))
                .andExpect(jsonPath("$.email").value(authInfo.getEmail()))
                .andExpect(jsonPath("$.profileUrl").value(userPatchRequest.getProfileUrl()))
                .andExpect(jsonPath("$.reputation").value(0))
                .andExpect(jsonPath("$.location").value(userPatchRequest.getLocation()))
                .andExpect(jsonPath("$.role").value(authInfo.getRole()))
                .andDo(document("update-user",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("?????? ?????????")
                        ),
                        requestFields(
                                List.of(
                                        fieldWithPath("displayName").type(JsonFieldType.STRING).description("????????? ?????????"),
                                        fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("location").type(JsonFieldType.STRING).description("????????? ??????")
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void getUserTest() throws Exception {
        // given
        Long userId = 1L;

        Answer answer = Answer.builder()
                .id(1L)
                .contents("????????????")
                .build();
        List<Answer> answers = List.of(answer);

        Question question = Question.builder()
                .id(1L)
                .title("????????????")
                .contents("????????????")
                .attempt("????????????")
                .build();

        Tag tag = new Tag("??????1");

        QuestionTag questionTag = QuestionTag.builder()
                .question(question)
                .tag(tag)
                .build();
        List<QuestionTag> tags = List.of(questionTag);
        question.addTags(tags);
        List<Question> questions = List.of(question);

        User user = User.builder()
                .userId(userId)
                .password("1111")
                .username("userA")
                .email("userA@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        answers.forEach(a -> a.addUser(user));
        questions.forEach(q -> q.addUser(user));

        given(userService.findUser(Mockito.anyLong())).willReturn(user);

        // when
        ResultActions actions = mockMvc.perform(get("/api/users/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.profileUrl").value(user.getProfileUrl()))
                .andExpect(jsonPath("$.reputation").value(0))
                .andExpect(jsonPath("$.location").value(user.getLocation()))
                .andExpect(jsonPath("$.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.answers[0].id").value(answers.get(0).getAnswerId()))
                .andExpect(jsonPath("$.answers[0].contents").value(answers.get(0).getContents()))
                .andExpect(jsonPath("$.questions[0].id").value(questions.get(0).getQuestionId()))
                .andExpect(jsonPath("$.questions[0].contents").value(questions.get(0).getContents()))
                .andDo(document("get-user",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("?????? ?????????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("answers").type(JsonFieldType.ARRAY).description("?????? ?????? ?????????"),
                                        fieldWithPath("answers[].id").type(JsonFieldType.NUMBER).description("?????? ?????? ?????????"),
                                        fieldWithPath("answers[].contents").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("answers[].likeCount").type(JsonFieldType.NUMBER).description("?????? ?????? ????????? ???"),
                                        fieldWithPath("answers[].dislikeCount").type(JsonFieldType.NUMBER).description("?????? ?????? ????????? ???"),
                                        fieldWithPath("answers[].createdDate").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("answers[].updatedDate").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("answers[].user").type(JsonFieldType.NULL).description("?????? ?????? ??????"),
                                        fieldWithPath("questions").type(JsonFieldType.ARRAY).description("?????? ?????? ?????????"),
                                        fieldWithPath("questions[].id").type(JsonFieldType.NUMBER).description("?????? ?????? ?????????"),
                                        fieldWithPath("questions[].title").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("questions[].contents").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("questions[].attempt").type(JsonFieldType.STRING).description("?????? ?????? ????????????"),
                                        fieldWithPath("questions[].likeCount").type(JsonFieldType.NUMBER).description("?????? ?????? ????????? ???"),
                                        fieldWithPath("questions[].dislikeCount").type(JsonFieldType.NUMBER).description("?????? ?????? ????????? ???"),
                                        fieldWithPath("questions[].reputation").type(JsonFieldType.NUMBER).description("?????? ?????? ??????"),
                                        fieldWithPath("questions[].answerCount").type(JsonFieldType.NUMBER).description("?????? ?????? ?????? ???"),
                                        fieldWithPath("questions[].tags").type(JsonFieldType.ARRAY).description("?????? ?????? ?????? ?????????"),
                                        fieldWithPath("questions[].tags[0].tagName").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("questions[].user").type(JsonFieldType.NULL).description("?????? ?????? ??????"),
                                        fieldWithPath("questions[].createdDate").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("questions[].updatedDate").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void getUsersTest() throws Exception {
        // given
        User user1 = User.builder()
                .userId(1L)
                .password("1111")
                .username("userA")
                .email("userA@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .userId(2L)
                .password("1111")
                .username("userB")
                .email("userB@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        List<User> users = List.of(user1, user2);
        PageImpl<User> userPage = new PageImpl<>(users,
                PageRequest.of(0, 10, Sort.by("id")), 2);

        given(userService.findUsers(Mockito.anyInt(), Mockito.anyInt())).willReturn(userPage);

        // when
        ResultActions actions = mockMvc.perform(get("/api/users")
                .param("page", "1")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(user1.getId()))
                .andExpect(jsonPath("$.data[1].id").value(user2.getId()))
                .andExpect(jsonPath("$.data[0].displayName").value(user1.getDisplayName()))
                .andExpect(jsonPath("$.data[1].displayName").value(user2.getDisplayName()))
                .andExpect(jsonPath("$.data[0].email").value(user1.getEmail()))
                .andExpect(jsonPath("$.data[1].email").value(user2.getEmail()))
                .andExpect(jsonPath("$.data[0].profileUrl").value(user1.getProfileUrl()))
                .andExpect(jsonPath("$.data[1].profileUrl").value(user2.getProfileUrl()))
                .andExpect(jsonPath("$.data[0].reputation").value(0))
                .andExpect(jsonPath("$.data[1].reputation").value(0))
                .andExpect(jsonPath("$.data[0].location").value(user1.getLocation()))
                .andExpect(jsonPath("$.data[1].location").value(user2.getLocation()))
                .andExpect(jsonPath("$.data[0].role").value(user1.getRole().getValue()))
                .andExpect(jsonPath("$.data[1].role").value(user2.getRole().getValue()))
                .andExpect(jsonPath("$.pageInfo.page").value(userPage.getNumber()+1))
                .andExpect(jsonPath("$.pageInfo.size").value(userPage.getSize()))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(userPage.getTotalElements()))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(userPage.getTotalPages()))
                .andDo(document("get-users",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        requestParameters(
                                parameterWithName("page").description("????????? ??????"),
                                parameterWithName("size").description("????????? ??????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("???????????????"),
                                        fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("data[].displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("data[].profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("data[].reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("data[].location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("data[].questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("?????? ????????? ?????????"),
                                        fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("?????? ?????????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void deleteTest() throws Exception {
        // given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(Mockito.anyLong(), Mockito.any(AuthInfo.class));

        // when
        ResultActions actions = mockMvc.perform(
                delete("/api/users/{userId}", userId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()));

        // then
        actions.andExpect(status().isNoContent())
                .andDo(document("delete-user",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("userId").description("?????? ?????????")
                        )
                ));
    }

}