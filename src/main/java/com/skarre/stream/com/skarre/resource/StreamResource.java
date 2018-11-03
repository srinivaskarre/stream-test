package com.skarre.stream.com.skarre.resource;

import com.skarre.stream.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


import java.io.IOException;

@RestController
@RequestMapping("/api")
public class StreamResource {

    @Autowired
    private StreamService streamService;

    @GetMapping("/stream")
    public void testStreamOncePlease()
    {
        try {
            streamService.streamDataFromMetup();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/stream/test")
    public void testStream(){

        streamService.testStreamWithRx();


    }

    @GetMapping(value = "/stream/testSse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testWithSse(){
        return streamService.testWithSse();
    }
}
