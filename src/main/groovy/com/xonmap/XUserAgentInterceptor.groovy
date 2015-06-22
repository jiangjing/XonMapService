package com.xonmap

import org.springframework.http.MediaType

import java.util.Map
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class XUserAgentInterceptor implements ClientHttpRequestInterceptor {
    Map headers

    XUserAgentInterceptor(Map headers){
        this.headers = headers
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders httpHeaders = request.getHeaders()
        headers?.each { key, value ->
            httpHeaders.add(key, value)
        }

        httpHeaders.setContentType(MediaType.APPLICATION_JSON)

        return execution.execute(request, body)
    }
}
