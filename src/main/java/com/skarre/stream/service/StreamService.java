package com.skarre.stream.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import rx.Observable;
import rx.Subscriber;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StreamService {
    private static final String QUEUE_NAME="test_queue";

    private ConcurrentHashMap<SseEmitter,SseEmitter> sseMap=new ConcurrentHashMap<>();
    Channel channel=null;

    public String streamDataFromMetup() throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpPost = new HttpGet("http://stream.meetup.com/2/open_events");
        System.out.println("before request:");
        CloseableHttpResponse response = client.execute(httpPost);
        System.out.println("After request:"+response);
        HttpEntity entity=response.getEntity();
        InputStream is=entity.getContent();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[12];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
            System.out.print(result);
        }
        // StandardCharsets.UTF_8.name() > JDK 7
        String res= result.toString("UTF-8");


        return res;

    }

    public void testStreamWithRx() {
        Observable<String> observer = createObservable();
        observer.subscribe(s->{
            System.out.println("Response in observable--");
            System.out.println(s);
            postDataToMessageQueue(s);
            sendDataToSseListerners(s);

        });


    }

    private void sendDataToSseListerners(String s) {
        for(SseEmitter sseEmitter:sseMap.values())
        {
            try {
                sseEmitter.send(s);
            }
            catch (Exception e) {
                if(e instanceof IllegalStateException || e instanceof IOException){
                    sseMap.remove(sseEmitter);
                    System.out.println("Map after connection fail--"+sseMap);
                }
                else {
                    e.printStackTrace();
                }
            }
        }
    }

    private void postDataToMessageQueue(String s) {
        try {
            if(channel==null)
            {
                configureRabbitMsgQueue();
            }
            channel.basicPublish("",QUEUE_NAME,null,s.getBytes());

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void configureRabbitMsgQueue() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            connectionFactory.setConnectionTimeout(100000);
            Connection connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Observable<String> createObservable(){
        return Observable.create(subscriber -> requestMeetUpForData(subscriber));
    }

    private void requestMeetUpForData(Subscriber<? super String> subscriber) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet httpPost = new HttpGet("http://stream.meetup.com/2/open_events");
            System.out.println("before request:");
            CloseableHttpResponse response = client.execute(httpPost);
            System.out.println("After request:" + response);
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();
            //ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                result.write(buffer, 0, length);
                subscriber.onNext(result.toString("UTF-8"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public SseEmitter testWithSse() {
        SseEmitter sseEmitter=new SseEmitter(60*1000L);
        sseMap.put(sseEmitter,sseEmitter);

        return  sseEmitter;

    }
}
