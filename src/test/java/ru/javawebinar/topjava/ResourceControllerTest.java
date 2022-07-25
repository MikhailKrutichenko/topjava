package ru.javawebinar.topjava;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javawebinar.topjava.web.AbstractControllerTest;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourceControllerTest extends AbstractControllerTest {

    @Test
    void resourceTest() throws Exception {
        perform(MockMvcRequestBuilders.get("/resources/css/style.css"))
                .andDo(print())
                .andExpectAll(status().isOk(), content().contentType("text/css;charset=UTF-8"));
    }
}
