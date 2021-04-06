package codes.monkey.cafe.test

import feign.Client
import feign.Request
import feign.Response
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.net.URLDecoder

class MockMvcFeignClient(val mockMvc: MockMvc) : Client {

    override fun execute(request: Request, options: Request.Options?): Response {
        // MockMvc path to controller doesn't decode the + char to spaces
        val decodedUrl = URLDecoder.decode(request.url(), "UTF-8")
        val method = FeignRequestUtils.convertRequestMethod(request)
        val body = request.body()
        val httpHeaders = FeignRequestUtils.convertRequestHeaders(request)

        val mvcResult = mockMvc.perform(
                MockMvcRequestBuilders.request(method, decodedUrl)
                        .content(body)
                        .headers(httpHeaders))
                .andReturn()
        val r2 = mockMvc.perform { MockMvcRequestBuilders.asyncDispatch(mvcResult).buildRequest(it) }.andReturn()
        val resp = r2.response
        return convertResponse(request, resp)
    }

    private fun convertResponse(request: Request, resp: MockHttpServletResponse): Response {
        return Response.builder()
                .request(request)
                .status(resp.status)
                .body(resp.contentAsByteArray)
//                .headers(resp.headerNames.stream()
//                        .collect(Collectors.toMap(Function.identity(), Function { name: T? -> resp.getHeaders(name) })))
                .build()
    }
}