package seb41_pre_32.back.restdocs.answer;


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
import seb41_pre_32.back.answer.controller.AnswerController;
import seb41_pre_32.back.answer.dto.AnswerPatchDto;
import seb41_pre_32.back.answer.dto.AnswerPostDto;
import seb41_pre_32.back.answer.entity.Answer;
import seb41_pre_32.back.answer.service.AnswerService;
import seb41_pre_32.back.auth.presentation.dto.AuthInfo;
import seb41_pre_32.back.question.entity.Question;
import seb41_pre_32.back.user.entity.Role;
import seb41_pre_32.back.user.entity.User;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnswerController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfigurer.class)})
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
public class AnswerControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AnswerService answerService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    public void postTest() throws Exception {
        // given
        AnswerPostDto answerPostDto = AnswerPostDto.builder()
                .questionId("1")
                .contents("?????? ??????")
                .build();

        String content = objectMapper.writeValueAsString(answerPostDto);

        User user = User.builder()
                .userId(1L)
                .password("1111")
                .username("userA")
                .email("userA@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        Question question = Question.builder()
                .id(1L)
                .title("??????")
                .contents("?????? ??????")
                .attempt("?????? ????????? ??????")
                .build();

        Answer answer = Answer.builder()
                .id(1L)
                .contents(answerPostDto.getContents())
                .likeCount(3)
                .disLikeCount(4)
                .build();

        answer.addQuestion(question);
        answer.addUser(user);

        given(answerService.createAnswer(Mockito.any(AnswerPostDto.class), Mockito.any(AuthInfo.class))).willReturn(answer);

        // when
        ResultActions actions = mockMvc.perform(post("/api/answers")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(content)
        );

        // then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(answer.getAnswerId()))
                .andExpect(jsonPath("$.contents").value(answer.getContents()))
                .andExpect(jsonPath("$.likeCount").value(answer.getLikeCount()))
                .andExpect(jsonPath("$.dislikeCount").value(answer.getDisLikeCount()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andDo(document("create-answer",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        requestFields(
                                List.of(
                                        fieldWithPath("questionId").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????")
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("?????? ????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void updateTest() throws Exception {
        // given
        AnswerPatchDto answerPatchDto = new AnswerPatchDto("?????? ?????? ??????");
        String content = objectMapper.writeValueAsString(answerPatchDto);

        User user = User.builder()
                .userId(1L)
                .password("1111")
                .username("userA")
                .email("userA@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        Question question = Question.builder()
                .id(1L)
                .title("??????")
                .contents("?????? ??????")
                .attempt("?????? ????????? ??????")
                .build();

        Answer answer = Answer.builder()
                .id(1L)
                .contents(answerPatchDto.getContents())
                .likeCount(3)
                .disLikeCount(4)
                .build();

        answer.addUser(user);
        answer.addQuestion(question);

        given(answerService.updateAnswer(Mockito.anyLong(), Mockito.any(AuthInfo.class), Mockito.any(AnswerPatchDto.class))).willReturn(answer);

        // when
        Long answerId = answer.getAnswerId();
        ResultActions actions = mockMvc.perform(patch("/api/answers/{answerId}", answerId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(content)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(answer.getAnswerId()))
                .andExpect(jsonPath("$.contents").value(answer.getContents()))
                .andExpect(jsonPath("$.likeCount").value(answer.getLikeCount()))
                .andExpect(jsonPath("$.dislikeCount").value(answer.getDisLikeCount()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andDo(document("update-answer",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("answerId").description("?????? ?????????")
                        ),
                        requestFields(
                                List.of(
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ?????? ??????")
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("?????? ????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }


    @Test
    @WithMockUser
    public void getAnswerTest() throws Exception {
        // given
        Long answerId = 1L;

        User user = User.builder()
                .userId(1L)
                .password("1111")
                .username("userA")
                .email("userA@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        Answer answer = Answer.builder()
                .id(1L)
                .contents("?????? ??????")
                .likeCount(3)
                .disLikeCount(4)
                .build();
        answer.addUser(user);

        given(answerService.getAnswer(Mockito.anyLong())).willReturn(answer);

        // when
        ResultActions actions = mockMvc.perform(get("/api/answers/{answerId}", answerId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(answer.getAnswerId()))
                .andExpect(jsonPath("$.contents").value(answer.getContents()))
                .andExpect(jsonPath("$.likeCount").value(answer.getLikeCount()))
                .andExpect(jsonPath("$.dislikeCount").value(answer.getDisLikeCount()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andDo(document("get-answer",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("answerId").description("?????? ?????????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("?????? ????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }


    @Test
    @WithMockUser
    public void getAnswersTest() throws Exception {
        // given
        Long questionId = 1L;

        Question question = Question.builder()
                .id(questionId)
                .title("??????")
                .contents("?????? ??????")
                .attempt("?????? ????????? ??????")
                .build();

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
                .password("2222")
                .username("user2")
                .email("user2@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        Answer answer1 = Answer.builder()
                .id(1L)
                .contents("?????? ??????")
                .likeCount(3)
                .disLikeCount(4)
                .build();

        answer1.addQuestion(question);
        answer1.addUser(user1);

        Answer answer2 = Answer.builder()
                .id(2L)
                .contents("?????? ??????2")
                .likeCount(12)
                .disLikeCount(12)
                .build();

        answer2.addQuestion(question);
        answer2.addUser(user2);

        List<Answer> answers = List.of(answer1, answer2);
        PageImpl<Answer> answerPage = new PageImpl<>(answers,
                PageRequest.of(0, 5, Sort.by("updatedDate")), 2);

        given(answerService.getAnswers(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt())).willReturn(answerPage);

        // when
        ResultActions actions = mockMvc.perform(get("/api/answers/questions/{questionId}", questionId)
                .param("page", "1")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(answer1.getAnswerId()))
                .andExpect(jsonPath("$.data[0].contents").value(answer1.getContents()))
                .andExpect(jsonPath("$.data[0].likeCount").value(answer1.getLikeCount()))
                .andExpect(jsonPath("$.data[0].dislikeCount").value(answer1.getDisLikeCount()))
                .andExpect(jsonPath("$.data[0].user.id").value(user1.getId()))
                .andExpect(jsonPath("$.data[0].user.displayName").value(user1.getDisplayName()))
                .andExpect(jsonPath("$.data[0].user.email").value(user1.getEmail()))
                .andExpect(jsonPath("$.data[0].user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.data[0].user.reputation").value(0))
                .andExpect(jsonPath("$.data[0].user.location").value("??????"))
                .andExpect(jsonPath("$.data[0].user.role").value(user1.getRole().getValue()))
                .andExpect(jsonPath("$.pageInfo.page").value(answerPage.getNumber() + 1))
                .andExpect(jsonPath("$.pageInfo.size").value(answerPage.getSize()))
                .andExpect(jsonPath("$.pageInfo.totalElements").value(answerPage.getTotalElements()))
                .andExpect(jsonPath("$.pageInfo.totalPages").value(answerPage.getTotalPages()))
                .andDo(document("get-answers",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("questionId").description("?????? ?????? ?????????")
                        ),
                        requestParameters(
                                parameterWithName("page").description("????????? ??????"),
                                parameterWithName("size").description("????????? ??????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("data").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("data[].id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("data[].contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("data[].dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("data[].createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("data[].updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("data[].user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("data[].user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("data[].user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.email").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("data[].user.profileUrl").type(JsonFieldType.STRING).description("?????? ????????? ????????? ??????"),
                                        fieldWithPath("data[].user.reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("data[].user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("data[].user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
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
        Long answerId = 1L;
        doNothing().when(answerService).deleteAnswer(Mockito.anyLong(), Mockito.any(AuthInfo.class));

        // when
        ResultActions actions = mockMvc.perform(
                delete("/api/answers/{answerId}", answerId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()));

        // then
        actions.andExpect(status().isNoContent())
                .andDo(document("delete-answer",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("answerId").description("?????? ?????????")
                        )
                ));
    }

    @Test
    @WithMockUser
    public void likeTest() throws Exception {
        // given
        Long answerId = 1L;

        User user = User.builder()
                .userId(1L)
                .password("1111")
                .username("userA")
                .email("userA@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        Question question = Question.builder()
                .id(1L)
                .title("??????")
                .contents("?????? ??????")
                .attempt("?????? ????????? ??????")
                .build();

        Answer answer = Answer.builder()
                .id(1L)
                .contents("?????? ??????")
                .likeCount(0)
                .disLikeCount(0)
                .build();

        answer.addUser(user);
        answer.addQuestion(question);
        answer.updateLikeCount();

        given(answerService.likeAnswer(Mockito.anyLong())).willReturn(answer);

        // when
        ResultActions actions = mockMvc.perform(patch("/api/answers/{answerId}/likes", answerId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(answer.getAnswerId()))
                .andExpect(jsonPath("$.contents").value(answer.getContents()))
                .andExpect(jsonPath("$.likeCount").value(answer.getLikeCount()))
                .andExpect(jsonPath("$.dislikeCount").value(answer.getDisLikeCount()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andDo(document("like-answer",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("answerId").description("?????? ?????????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("?????? ????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void dislikeTest() throws Exception {
        // given
        Long answerId = 1L;

        User user = User.builder()
                .userId(1L)
                .password("1111")
                .username("userA")
                .email("userA@gmail.com")
                .profileUrl("basic.url")
                .reputation(0)
                .location("??????")
                .role(Role.USER)
                .build();

        Question question = Question.builder()
                .id(1L)
                .title("??????")
                .contents("?????? ??????")
                .attempt("?????? ????????? ??????")
                .build();

        Answer answer = Answer.builder()
                .id(1L)
                .contents("?????? ??????")
                .likeCount(0)
                .disLikeCount(0)
                .build();

        answer.addUser(user);
        answer.addQuestion(question);
        answer.updateDisLikeCount();

        given(answerService.dislikeAnswer(Mockito.anyLong())).willReturn(answer);

        // when
        ResultActions actions = mockMvc.perform(patch("/api/answers/{answerId}/dislikes", answerId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(answer.getAnswerId()))
                .andExpect(jsonPath("$.contents").value(answer.getContents()))
                .andExpect(jsonPath("$.likeCount").value(answer.getLikeCount()))
                .andExpect(jsonPath("$.dislikeCount").value(answer.getDisLikeCount()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andDo(document("dislike-answer",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("answerId").description("?????? ?????????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????? ?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("?????? ????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????")
                                )
                        )
                ));
    }
}