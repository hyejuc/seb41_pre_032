package seb41_pre_32.back.restdocs.question;


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
import seb41_pre_32.back.question.controller.QuestionController;
import seb41_pre_32.back.question.dto.QuestionPatchDto;
import seb41_pre_32.back.question.dto.QuestionPostDto;
import seb41_pre_32.back.question.entity.Question;
import seb41_pre_32.back.question.service.QuestionService;
import seb41_pre_32.back.tag.entity.QuestionTag;
import seb41_pre_32.back.tag.entity.Tag;
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

@WebMvcTest(controllers = QuestionController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = WebMvcConfigurer.class)})
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureRestDocs
public class QuestionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private QuestionService questionService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser
    public void postTest() throws Exception {
        // given
        QuestionPostDto questionPostDto = QuestionPostDto
                .builder()
                .title("??????")
                .contents("??????")
                .attempt("?????? ?????? ?????? ??????")
                .taglist(List.of("??????"))
                .build();

        String content = objectMapper.writeValueAsString(questionPostDto);

        Question question = Question.builder()
                .id(1L)
                .title(questionPostDto.getTitle())
                .contents(questionPostDto.getContents())
                .attempt(questionPostDto.getAttempt())
                .build();

        Tag tag = new Tag("??????");
        QuestionTag questionTag = QuestionTag.builder()
                .question(question)
                .tag(tag)
                .build();

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
                .contents("????????????")
                .build();

        List<Answer> answers = List.of(answer);
        List<QuestionTag> tags = List.of(questionTag);

        question.addTags(tags);
        question.addUser(user);
        answers.forEach(a -> a.addQuestion(question));

        given(questionService.createQuestion(Mockito.any(QuestionPostDto.class), Mockito.any(AuthInfo.class))).willReturn(question);

        // when
        ResultActions actions = mockMvc.perform(post("/api/questions")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(content)
        );

        // then
        actions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(question.getQuestionId()))
                .andExpect(jsonPath("$.title").value(question.getTitle()))
                .andExpect(jsonPath("$.contents").value(question.getContents()))
                .andExpect(jsonPath("$.attempt").value(question.getAttempt()))
                .andExpect(jsonPath("$.tags[0].tagName").value(tag.getTagName()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.answerCount").value(answers.size()))
                .andDo(document("create-question",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        requestFields(
                                List.of(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("attempt").type(JsonFieldType.STRING).description("?????? ????????? ??????").optional(),
                                        fieldWithPath("taglist").type(JsonFieldType.ARRAY).description("?????? ?????????").optional()
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("attempt").type(JsonFieldType.STRING).description("?????? ?????? ????????? ??????"),
                                        fieldWithPath("tags").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("tags[].tagName").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("answerCount").type(JsonFieldType.NUMBER).description("?????? ??????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void updateTest() throws Exception {
        // given
        QuestionPatchDto questionPatchDto = QuestionPatchDto
                .builder()
                .title("?????? ??????")
                .contents("?????? ??????")
                .attempt("?????? ?????? ?????? ?????? ??????")
                .build();

        String content = objectMapper.writeValueAsString(questionPatchDto);

        Question question = Question.builder()
                .id(1L)
                .title(questionPatchDto.getTitle())
                .contents(questionPatchDto.getContents())
                .attempt(questionPatchDto.getAttempt())
                .build();

        Tag tag = new Tag("??????");
        QuestionTag questionTag = QuestionTag.builder()
                .question(question)
                .tag(tag)
                .build();

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
                .contents("????????????")
                .build();

        List<Answer> answers = List.of(answer);
        List<QuestionTag> tags = List.of(questionTag);

        question.addTags(tags);
        question.addUser(user);
        answers.forEach(a -> a.addQuestion(question));

        given(questionService.updateQuestion(Mockito.any(QuestionPatchDto.class), Mockito.anyLong(), Mockito.any(AuthInfo.class))).willReturn(question);

        // when
        Long questionId = question.getQuestionId();
        ResultActions actions = mockMvc.perform(patch("/api/questions/{questionId}", questionId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .content(content)
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(question.getQuestionId()))
                .andExpect(jsonPath("$.title").value(question.getTitle()))
                .andExpect(jsonPath("$.contents").value(question.getContents()))
                .andExpect(jsonPath("$.attempt").value(question.getAttempt()))
                .andExpect(jsonPath("$.tags[0].tagName").value(tag.getTagName()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.answerCount").value(answers.size()))
                .andDo(document("update-question",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("questionId").description("?????? ?????????")
                        ),
                        requestFields(
                                List.of(
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ?????? ??????"),
                                        fieldWithPath("attempt").type(JsonFieldType.STRING).description("?????? ?????? ????????? ??????").optional()
                                )
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("attempt").type(JsonFieldType.STRING).description("?????? ????????? ??????"),
                                        fieldWithPath("tags").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("tags[].tagName").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("answerCount").type(JsonFieldType.NUMBER).description("?????? ??????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void getQuestionTest() throws Exception {
        // given
        Long questionId = 1L;
        Question question = Question.builder()
                .id(questionId)
                .title("????????????")
                .contents("????????????")
                .attempt("????????????")
                .build();

        Tag tag = new Tag("??????");
        QuestionTag questionTag = QuestionTag.builder()
                .question(question)
                .tag(tag)
                .build();

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
                .contents("????????????")
                .build();

        List<Answer> answers = List.of(answer);
        List<QuestionTag> tags = List.of(questionTag);

        question.addTags(tags);
        question.addUser(user);
        answers.forEach(a -> a.addQuestion(question));

        given(questionService.findQuestion(Mockito.anyLong())).willReturn(question);

        // when
        ResultActions actions = mockMvc.perform(get("/api/questions/{questionId}", question.getQuestionId())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(question.getQuestionId()))
                .andExpect(jsonPath("$.title").value(question.getTitle()))
                .andExpect(jsonPath("$.contents").value(question.getContents()))
                .andExpect(jsonPath("$.attempt").value(question.getAttempt()))
                .andExpect(jsonPath("$.tags[0].tagName").value(tag.getTagName()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.answerCount").value(answers.size()))
                .andDo(document("get-question",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("questionId").description("?????? ?????????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("attempt").type(JsonFieldType.STRING).description("?????? ????????? ??????"),
                                        fieldWithPath("tags").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("tags[].tagName").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("answerCount").type(JsonFieldType.NUMBER).description("?????? ??????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void getQuestionsTest() throws Exception {
        // given
        Question question1 = Question.builder()
                .id(1L)
                .title("?????? ??????1")
                .contents("?????? ??????1")
                .attempt("?????? ??????1")
                .build();

        Question question2 = Question.builder()
                .id(2L)
                .title("?????? ??????2")
                .contents("?????? ??????2")
                .attempt("?????? ??????2")
                .build();

        Tag tag1 = new Tag("??????1");
        QuestionTag questionTag1 = QuestionTag.builder()
                .question(question1)
                .tag(tag1)
                .build();

        Tag tag2 = new Tag("??????2");
        QuestionTag questionTag2 = QuestionTag.builder()
                .question(question2)
                .tag(tag2)
                .build();

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

        Answer answer1 = Answer.builder()
                .id(1L)
                .contents("????????????")
                .build();

        Answer answer2 = Answer.builder()
                .id(2L)
                .contents("????????????2")
                .build();

        List<Answer> answers1 = List.of(answer1);
        List<Answer> answers2 = List.of(answer2);

        List<QuestionTag> tags1 = List.of(questionTag1);
        List<QuestionTag> tags2 = List.of(questionTag2);

        question1.addTags(tags1);
        question2.addTags(tags2);

        question1.addUser(user);
        question2.addUser(user);

        answers1.forEach(a -> a.addQuestion(question1));
        answers2.forEach(a -> a.addQuestion(question2));
        List<Question> questions = List.of(question1, question2);

        PageImpl<Question> questionPage = new PageImpl<>(questions,
                PageRequest.of(0, 10, Sort.by("questionId")), 2);

        given(questionService.findQuestions(Mockito.anyInt(), Mockito.anyInt())).willReturn(questionPage);

        // when
        ResultActions actions = mockMvc.perform(get("/api/questions")
                .param("page", "1")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(question1.getQuestionId()))
                .andExpect(jsonPath("$.data[0].title").value(question1.getTitle()))
                .andExpect(jsonPath("$.data[0].contents").value(question1.getContents()))
                .andExpect(jsonPath("$.data[0].attempt").value(question1.getAttempt()))
                .andExpect(jsonPath("$.data[0].tags[0].tagName").value(tag1.getTagName()))
                .andExpect(jsonPath("$.data[0].user.id").value(user.getId()))
                .andExpect(jsonPath("$.data[0].user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.data[0].user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data[0].user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.data[0].user.reputation").value(0))
                .andExpect(jsonPath("$.data[0].user.location").value("??????"))
                .andExpect(jsonPath("$.data[0].user.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.data[0].answerCount").value(question1.getAnswerList().size()))
                .andDo(document("get-questions",
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
                                        fieldWithPath("data[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].attempt").type(JsonFieldType.STRING).description("?????? ????????? ??????"),
                                        fieldWithPath("data[].tags").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("data[].tags[].tagName").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("data[].user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("data[].user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("data[].user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("data[].user.profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("data[].user.reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("data[].user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("data[].user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("data[].likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("data[].dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("data[].reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("data[].createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("data[].updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("data[].answerCount").type(JsonFieldType.NUMBER).description("?????? ??????"),
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
        Long questionId = 1L;
        doNothing().when(questionService).deleteQuestion(Mockito.anyLong(), Mockito.any(AuthInfo.class));

        // when
        ResultActions actions = mockMvc.perform(delete("/api/questions/{questionId}", questionId)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()));

        // then
        actions.andExpect(status().isNoContent())
                .andDo(document("delete-question",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("questionId").description("?????? ?????????")
                        )
                ));
    }

    @Test
    @WithMockUser
    public void likeTest() throws Exception {
        // given
        Long questionId = 1L;

        Question question = Question.builder()
                .id(1L)
                .title("?????? ??????")
                .contents("?????? ??????")
                .attempt("?????? ??????")
                .build();

        Tag tag = new Tag("??????");
        QuestionTag questionTag = QuestionTag.builder()
                .question(question)
                .tag(tag)
                .build();

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
                .contents("????????????")
                .build();

        List<Answer> answers = List.of(answer);
        List<QuestionTag> tags = List.of(questionTag);

        question.addTags(tags);
        question.addUser(user);
        answers.forEach(a -> a.addQuestion(question));

        question.updateLikeCount();
        given(questionService.likeQuestion(Mockito.anyLong())).willReturn(question);

        // when
        ResultActions actions = mockMvc.perform(
                patch("/api/questions/{questionId}/likes", questionId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(question.getQuestionId()))
                .andExpect(jsonPath("$.title").value(question.getTitle()))
                .andExpect(jsonPath("$.contents").value(question.getContents()))
                .andExpect(jsonPath("$.attempt").value(question.getAttempt()))
                .andExpect(jsonPath("$.tags[0].tagName").value(tag.getTagName()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.likeCount").value(question.getLikeCount()))
                .andExpect(jsonPath("$.dislikeCount").value(question.getDisLikeCount()))
                .andExpect(jsonPath("$.answerCount").value(answers.size()))
                .andDo(document("like-question",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("questionId").description("?????? ?????????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("attempt").type(JsonFieldType.STRING).description("?????? ????????? ??????"),
                                        fieldWithPath("tags").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("tags[].tagName").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("answerCount").type(JsonFieldType.NUMBER).description("?????? ??????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void dislikeTest() throws Exception {
        // given
        Long questionId = 1L;
        Question question = Question.builder()
                .id(1L)
                .title("?????? ??????")
                .contents("?????? ??????")
                .attempt("?????? ??????")
                .build();

        Tag tag = new Tag("??????");
        QuestionTag questionTag = QuestionTag.builder()
                .question(question)
                .tag(tag)
                .build();

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
                .contents("????????????")
                .build();

        List<Answer> answers = List.of(answer);
        List<QuestionTag> tags = List.of(questionTag);

        question.addTags(tags);
        question.addUser(user);
        answers.forEach(a -> a.addQuestion(question));

        question.updateDisLikeCount();
        given(questionService.dislikeQuestion(Mockito.anyLong())).willReturn(question);

        // when
        ResultActions actions = mockMvc.perform(
                patch("/api/questions/{questionId}/dislikes", questionId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
        );

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(question.getQuestionId()))
                .andExpect(jsonPath("$.title").value(question.getTitle()))
                .andExpect(jsonPath("$.contents").value(question.getContents()))
                .andExpect(jsonPath("$.attempt").value(question.getAttempt()))
                .andExpect(jsonPath("$.tags[0].tagName").value(tag.getTagName()))
                .andExpect(jsonPath("$.user.id").value(user.getId()))
                .andExpect(jsonPath("$.user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.user.reputation").value(0))
                .andExpect(jsonPath("$.user.location").value("??????"))
                .andExpect(jsonPath("$.user.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.likeCount").value(question.getLikeCount()))
                .andExpect(jsonPath("$.dislikeCount").value(question.getDisLikeCount()))
                .andExpect(jsonPath("$.answerCount").value(answers.size()))
                .andDo(document("dislike-question",
                        Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                        Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                        pathParameters(
                                parameterWithName("questionId").description("?????? ?????????")
                        ),
                        responseFields(
                                List.of(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("attempt").type(JsonFieldType.STRING).description("?????? ????????? ??????"),
                                        fieldWithPath("tags").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("tags[].tagName").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("user.profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("user.reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("answerCount").type(JsonFieldType.NUMBER).description("?????? ??????")
                                )
                        )
                ));
    }

    @Test
    @WithMockUser
    public void getQuestionsOrderByReputationTest() throws Exception {
        // given
        Question question1 = Question.builder()
                .id(1L)
                .title("?????? ??????1")
                .contents("?????? ??????1")
                .attempt("?????? ??????1")
                .build();

        Question question2 = Question.builder()
                .id(2L)
                .title("?????? ??????2")
                .contents("?????? ??????2")
                .attempt("?????? ??????2")
                .build();

        Tag tag1 = new Tag("??????1");
        QuestionTag questionTag1 = QuestionTag.builder()
                .question(question1)
                .tag(tag1)
                .build();

        Tag tag2 = new Tag("??????2");
        QuestionTag questionTag2 = QuestionTag.builder()
                .question(question2)
                .tag(tag2)
                .build();

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

        Answer answer1 = Answer.builder()
                .id(1L)
                .contents("????????????")
                .build();

        Answer answer2 = Answer.builder()
                .id(2L)
                .contents("????????????2")
                .build();

        List<Answer> answers1 = List.of(answer1);
        List<Answer> answers2 = List.of(answer2);

        List<QuestionTag> tags1 = List.of(questionTag1);
        List<QuestionTag> tags2 = List.of(questionTag2);

        question1.addTags(tags1);
        question2.addTags(tags2);

        question1.addUser(user);
        question2.addUser(user);

        answers1.forEach(a -> a.addQuestion(question1));
        answers2.forEach(a -> a.addQuestion(question2));

        question1.updateLikeCount();
        List<Question> questions = List.of(question1, question2);

        PageImpl<Question> questionPage = new PageImpl<>(questions,
                PageRequest.of(0, 10, Sort.by("reputation").descending()), 2);

        given(questionService.findQuestionsByLikes(Mockito.anyInt(), Mockito.anyInt())).willReturn(questionPage);

        // when
        ResultActions actions = mockMvc.perform(get("/api/questions/reputation")
                .param("page", "1")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));

        // then
        actions.andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(question1.getQuestionId()))
                .andExpect(jsonPath("$.data[0].title").value(question1.getTitle()))
                .andExpect(jsonPath("$.data[0].contents").value(question1.getContents()))
                .andExpect(jsonPath("$.data[0].attempt").value(question1.getAttempt()))
                .andExpect(jsonPath("$.data[0].tags[0].tagName").value(tag1.getTagName()))
                .andExpect(jsonPath("$.data[0].user.id").value(user.getId()))
                .andExpect(jsonPath("$.data[0].user.displayName").value(user.getDisplayName()))
                .andExpect(jsonPath("$.data[0].user.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data[0].user.profileUrl").value("basic.url"))
                .andExpect(jsonPath("$.data[0].user.reputation").value(0))
                .andExpect(jsonPath("$.data[0].user.location").value("??????"))
                .andExpect(jsonPath("$.data[0].user.role").value(user.getRole().getValue()))
                .andExpect(jsonPath("$.data[0].answerCount").value(question1.getAnswerList().size()))
                .andDo(document("get-questions",
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
                                        fieldWithPath("data[].title").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].contents").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].attempt").type(JsonFieldType.STRING).description("?????? ????????? ??????"),
                                        fieldWithPath("data[].tags").type(JsonFieldType.ARRAY).description("?????? ?????????"),
                                        fieldWithPath("data[].tags[].tagName").type(JsonFieldType.STRING).description("????????????"),
                                        fieldWithPath("data[].user").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("data[].user.id").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("data[].user.displayName").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.email").type(JsonFieldType.STRING).description("?????????"),
                                        fieldWithPath("data[].user.profileUrl").type(JsonFieldType.STRING).description("????????? ????????? ??????"),
                                        fieldWithPath("data[].user.reputation").type(JsonFieldType.NUMBER).description("??????"),
                                        fieldWithPath("data[].user.location").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.role").type(JsonFieldType.STRING).description("?????? ??????"),
                                        fieldWithPath("data[].user.answers").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("data[].user.questions").type(JsonFieldType.NULL).description("?????? ?????? ?????????"),
                                        fieldWithPath("data[].likeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("data[].dislikeCount").type(JsonFieldType.NUMBER).description("?????? ????????? ???"),
                                        fieldWithPath("data[].reputation").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("data[].createdDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("data[].updatedDate").type(JsonFieldType.NULL).description("?????? ?????????"),
                                        fieldWithPath("data[].answerCount").type(JsonFieldType.NUMBER).description("?????? ??????"),
                                        fieldWithPath("pageInfo").type(JsonFieldType.OBJECT).description("????????? ??????"),
                                        fieldWithPath("pageInfo.page").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("pageInfo.size").type(JsonFieldType.NUMBER).description("?????? ????????? ?????????"),
                                        fieldWithPath("pageInfo.totalElements").type(JsonFieldType.NUMBER).description("?????? ?????????"),
                                        fieldWithPath("pageInfo.totalPages").type(JsonFieldType.NUMBER).description("?????? ?????????")
                                )
                        )
                ));
    }

}