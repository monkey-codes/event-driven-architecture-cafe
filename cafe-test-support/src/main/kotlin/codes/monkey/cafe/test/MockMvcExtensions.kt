package codes.monkey.cafe.test

import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

fun MockMvc.asyncDispatch(statusMatcher: ResultMatcher = MockMvcResultMatchers.status().is2xxSuccessful, callback: () -> MockHttpServletRequestBuilder): ResultActions =
        perform {
            callback().buildRequest(it)
        }.andExpect(statusMatcher)
                .andReturn().let {
                    perform { sc -> MockMvcRequestBuilders.asyncDispatch(it).buildRequest(sc) }
                }.andExpect(statusMatcher)