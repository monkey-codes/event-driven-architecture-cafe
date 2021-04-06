package codes.monkey.cafe.test

import feign.Request
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import java.util.ArrayList

object FeignRequestUtils {
    /**
     * Convert Feign [Request] headers collection to Spring [HttpHeaders] object
     *
     * @param request Feign request to convert
     * @return Converted HTTP headers (as a Spring object)
     */
    fun convertRequestHeaders(request: Request): HttpHeaders {
        val springHeaders = HttpHeaders()
        request.headers().forEach { header, vals -> springHeaders.put(header, ArrayList<String>(vals)) }
        return springHeaders
    }

    /**
     * Convert Feign [Request] request method to Spring [HttpMethod] object
     *
     * @param request Feign request to convert
     * @return Converted HTTP Method (as a Spring object)
     */
    fun convertRequestMethod(request: Request): HttpMethod {
        return HttpMethod.valueOf(request.method())
    }
}